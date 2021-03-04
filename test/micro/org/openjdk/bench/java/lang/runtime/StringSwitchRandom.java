package org.openjdk.bench.java.lang.runtime;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.spi.ToolProvider;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class StringSwitchRandom {

    private static final MethodType CALL_TYPE = MethodType.methodType(int.class, String.class);
    private static final MutableCallSite CALL_SITE = new MutableCallSite(CALL_TYPE);
    private static final MethodHandle TARGET = CALL_SITE.dynamicInvoker();

    private static final Path ROOT = Path.of("../jmh_out");
    private static final Path CLASS_PATH = ROOT.resolve("classes");
    private static final Path SOURCE_PATH = ROOT.resolve("sources");
    private static final ToolProvider JAVAC = ToolProvider.findFirst("javac").orElseThrow();

    private static final int BATCH_SIZE = 1_000_000;

    @Param({
        "hashMap",
        //"binarySearch", // Binary search is just terrible across the board
        "ifTree",
        "legacy"
    })
    public String strategy;

    @Param({
        "5",
        "10",
        "25",
        "50",
        "100"
    })
    public int numCases;

    @Param({
        "5",
        "10",
        "15",
        "20"
    })
    public int caseLabelLength;

    // TODO test non-Latin1 chars as well
    private static final String caseChars = "abcdefghijklmnopqrstuvwxyz0123456789";

    public String[] inputs;

    @Setup(Level.Trial)
    public void setup() throws Throwable {
        Files.createDirectories(SOURCE_PATH);
        Files.createDirectories(CLASS_PATH);

        String sourceBaseName = "src_" + strategy + "_" + numCases + "_" + caseLabelLength;
        Path source = SOURCE_PATH.resolve(sourceBaseName + ".java");
        String targetMethodName = "javacStringSwitchDesugaringStrategy_" + strategy;

        if (!Files.exists(source)) {
            Files.createFile(source);
        }
        Random caseRandom = new Random(0);
        String[] caseLabels = Stream.generate(() -> genCase(caseRandom))
                .distinct()
                .limit(numCases)
                .toArray(String[]::new);

        AtomicLong caseReturns = new AtomicLong(0);
        String casesString = Arrays.stream(caseLabels)
                .map(label -> "case \"" + label + "\" -> res = " + caseReturns.getAndIncrement() + ";")
                .collect(Collectors.joining("\n            "));

        Files.writeString(
                source,
                """
                public class %s {
                    public static int %s(String switchOn) {
                        int res;
                        switch (switchOn) {
                            %s
                            default -> res = -1;
                        }
                        return res;
                    }
                }
                """.formatted(sourceBaseName, targetMethodName, casesString)
        );

        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        int code = JAVAC.run(printWriter, printWriter,
            "-d", CLASS_PATH.toString(),
            source.toString()
        );
        if (code != 0) {
            throw new IllegalStateException("Unexpected javac exit code: " + code + " : " + writer.toString());
        }

        URLClassLoader loader = URLClassLoader.newInstance(new URL[]{ CLASS_PATH.toUri().toURL() });
        Class<?> targetClass = loader.loadClass(sourceBaseName);
        MethodHandle targetHandle = MethodHandles.lookup().unreflect(targetClass.getMethod(targetMethodName, String.class));

        CALL_SITE.setTarget(targetHandle);

        inputs = new String[BATCH_SIZE];
        Random inputRandom = new Random(0);
        for (int i = 0; i < BATCH_SIZE; i++) {
            // TODO the default case has just as much chance of being hit as other cases,
            // but this might not be representative of real-world scenarios
            // e.g. the default case could be more or less common
            // need to do corpus experiment
            int next = inputRandom.nextInt(caseLabels.length + 1);
            inputs[i] = next == caseLabels.length ? "_" : caseLabels[next];
        }
    }

    private String genCase(Random rand) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < caseLabelLength; i++) {
            sb.append(caseChars.charAt(rand.nextInt(caseChars.length())));
        }
        return sb.toString();
    }

    @Benchmark
    public void testStringSwitch(Blackhole bh) throws Throwable {
        for (int i = 0; i < inputs.length; i++) {
            bh.consume((int) TARGET.invokeExact(inputs[i]));
        }
    }
}