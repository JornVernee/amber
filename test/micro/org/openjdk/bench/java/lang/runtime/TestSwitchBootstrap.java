/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.openjdk.bench.java.lang.runtime;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class TestSwitchBootstrap {

    private static final MethodType callType = MethodType.methodType(int.class, Types.I.class, int.class);

    private static final MutableCallSite cs = new MutableCallSite(callType);
    private static final MethodHandle target = cs.dynamicInvoker();

    private static final MutableCallSite baselineCs = new MutableCallSite(callType);
    private static final MethodHandle baselinetarget = baselineCs.dynamicInvoker();

    // Using batch size since we really need a per-invocation setup
    // but the measured code is too fast. Using JMH batch size doesn't work
    // since there is no way to do a batch-level setup as well.
    private static final int BATCH_SIZE = 1_000_000;

    @Param({ "5", "10", "25", "50", "100" })
    public int numCases;

    public Types.I[] inputs;

    @Setup(Level.Trial)
    public void setupTrial() throws Throwable {
        cs.setTarget(SwitchBootstraps.typeSwitch(null, null, callType,
                Types.ALL_TYPES.subList(0, numCases).toArray(Class<?>[]::new))
                .dynamicInvoker());

        baselineCs.setTarget(MethodHandles.dropArguments(MethodHandles.constant(int.class, -1), 0, Types.I.class, int.class));

        inputs = new Types.I[BATCH_SIZE];
        Random rand = new Random(0);
        for (int i = 0; i < BATCH_SIZE; i++) {
            inputs[i] = Types.ALL_TYPES_INST.get(rand.nextInt(Types.ALL_TYPES.size()));
        }
    }

    @Benchmark
    @Fork(value = 1)
    public void testSwitch_baseline(Blackhole bh) throws Throwable {
        for (int i = 0; i < inputs.length; i++) {
            bh.consume((int) baselinetarget.invokeExact(inputs[i], 0));
        }
    }

    @Benchmark
    @Fork(value = 3, jvmArgsAppend = { "-Djava.lang.runtime.SwitchBootstraps.TYPE_SWITCH_STRATEGY=ARRAY_LOOP" })
    public void testSwitch_arrayLoop(Blackhole bh) throws Throwable {
        for (int i = 0; i < inputs.length; i++) {
            bh.consume((int) target.invokeExact(inputs[i], 0));
        }
    }

    @Benchmark
    @Fork(value = 3, jvmArgsAppend = { "-Djava.lang.runtime.SwitchBootstraps.TYPE_SWITCH_STRATEGY=IF_ELSE" })
    public void testSwitch_ifElse(Blackhole bh) throws Throwable {
        for (int i = 0; i < inputs.length; i++) {
            bh.consume((int) target.invokeExact(inputs[i], 0));
        }
    }

}

class Types {

    static final List<Class<?>> ALL_TYPES = List.of(
        C0.class,
        C1.class,
        C2.class,
        C3.class,
        C4.class,
        C5.class,
        C6.class,
        C7.class,
        C8.class,
        C9.class,
        C10.class,
        C11.class,
        C12.class,
        C13.class,
        C14.class,
        C15.class,
        C16.class,
        C17.class,
        C18.class,
        C19.class,
        C20.class,
        C21.class,
        C22.class,
        C23.class,
        C24.class,
        C25.class,
        C26.class,
        C27.class,
        C28.class,
        C29.class,
        C30.class,
        C31.class,
        C32.class,
        C33.class,
        C34.class,
        C35.class,
        C36.class,
        C37.class,
        C38.class,
        C39.class,
        C40.class,
        C41.class,
        C42.class,
        C43.class,
        C44.class,
        C45.class,
        C46.class,
        C47.class,
        C48.class,
        C49.class,
        C50.class,
        C51.class,
        C52.class,
        C53.class,
        C54.class,
        C55.class,
        C56.class,
        C57.class,
        C58.class,
        C59.class,
        C60.class,
        C61.class,
        C62.class,
        C63.class,
        C64.class,
        C65.class,
        C66.class,
        C67.class,
        C68.class,
        C69.class,
        C70.class,
        C71.class,
        C72.class,
        C73.class,
        C74.class,
        C75.class,
        C76.class,
        C77.class,
        C78.class,
        C79.class,
        C80.class,
        C81.class,
        C82.class,
        C83.class,
        C84.class,
        C85.class,
        C86.class,
        C87.class,
        C88.class,
        C89.class,
        C90.class,
        C91.class,
        C92.class,
        C93.class,
        C94.class,
        C95.class,
        C96.class,
        C97.class,
        C98.class,
        C99.class
    );

    static final List<I> ALL_TYPES_INST = List.of (
        new C0(),
        new C1(),
        new C2(),
        new C3(),
        new C4(),
        new C5(),
        new C6(),
        new C7(),
        new C8(),
        new C9(),
        new C10(),
        new C11(),
        new C12(),
        new C13(),
        new C14(),
        new C15(),
        new C16(),
        new C17(),
        new C18(),
        new C19(),
        new C20(),
        new C21(),
        new C22(),
        new C23(),
        new C24(),
        new C25(),
        new C26(),
        new C27(),
        new C28(),
        new C29(),
        new C30(),
        new C31(),
        new C32(),
        new C33(),
        new C34(),
        new C35(),
        new C36(),
        new C37(),
        new C38(),
        new C39(),
        new C40(),
        new C41(),
        new C42(),
        new C43(),
        new C44(),
        new C45(),
        new C46(),
        new C47(),
        new C48(),
        new C49(),
        new C50(),
        new C51(),
        new C52(),
        new C53(),
        new C54(),
        new C55(),
        new C56(),
        new C57(),
        new C58(),
        new C59(),
        new C60(),
        new C61(),
        new C62(),
        new C63(),
        new C64(),
        new C65(),
        new C66(),
        new C67(),
        new C68(),
        new C69(),
        new C70(),
        new C71(),
        new C72(),
        new C73(),
        new C74(),
        new C75(),
        new C76(),
        new C77(),
        new C78(),
        new C79(),
        new C80(),
        new C81(),
        new C82(),
        new C83(),
        new C84(),
        new C85(),
        new C86(),
        new C87(),
        new C88(),
        new C89(),
        new C90(),
        new C91(),
        new C92(),
        new C93(),
        new C94(),
        new C95(),
        new C96(),
        new C97(),
        new C98(),
        new C99()
    );

    interface I {}
    static class C0 implements I {}
    static class C1 implements I {}
    static class C2 implements I {}
    static class C3 implements I {}
    static class C4 implements I {}
    static class C5 implements I {}
    static class C6 implements I {}
    static class C7 implements I {}
    static class C8 implements I {}
    static class C9 implements I {}
    static class C10 implements I {}
    static class C11 implements I {}
    static class C12 implements I {}
    static class C13 implements I {}
    static class C14 implements I {}
    static class C15 implements I {}
    static class C16 implements I {}
    static class C17 implements I {}
    static class C18 implements I {}
    static class C19 implements I {}
    static class C20 implements I {}
    static class C21 implements I {}
    static class C22 implements I {}
    static class C23 implements I {}
    static class C24 implements I {}
    static class C25 implements I {}
    static class C26 implements I {}
    static class C27 implements I {}
    static class C28 implements I {}
    static class C29 implements I {}
    static class C30 implements I {}
    static class C31 implements I {}
    static class C32 implements I {}
    static class C33 implements I {}
    static class C34 implements I {}
    static class C35 implements I {}
    static class C36 implements I {}
    static class C37 implements I {}
    static class C38 implements I {}
    static class C39 implements I {}
    static class C40 implements I {}
    static class C41 implements I {}
    static class C42 implements I {}
    static class C43 implements I {}
    static class C44 implements I {}
    static class C45 implements I {}
    static class C46 implements I {}
    static class C47 implements I {}
    static class C48 implements I {}
    static class C49 implements I {}
    static class C50 implements I {}
    static class C51 implements I {}
    static class C52 implements I {}
    static class C53 implements I {}
    static class C54 implements I {}
    static class C55 implements I {}
    static class C56 implements I {}
    static class C57 implements I {}
    static class C58 implements I {}
    static class C59 implements I {}
    static class C60 implements I {}
    static class C61 implements I {}
    static class C62 implements I {}
    static class C63 implements I {}
    static class C64 implements I {}
    static class C65 implements I {}
    static class C66 implements I {}
    static class C67 implements I {}
    static class C68 implements I {}
    static class C69 implements I {}
    static class C70 implements I {}
    static class C71 implements I {}
    static class C72 implements I {}
    static class C73 implements I {}
    static class C74 implements I {}
    static class C75 implements I {}
    static class C76 implements I {}
    static class C77 implements I {}
    static class C78 implements I {}
    static class C79 implements I {}
    static class C80 implements I {}
    static class C81 implements I {}
    static class C82 implements I {}
    static class C83 implements I {}
    static class C84 implements I {}
    static class C85 implements I {}
    static class C86 implements I {}
    static class C87 implements I {}
    static class C88 implements I {}
    static class C89 implements I {}
    static class C90 implements I {}
    static class C91 implements I {}
    static class C92 implements I {}
    static class C93 implements I {}
    static class C94 implements I {}
    static class C95 implements I {}
    static class C96 implements I {}
    static class C97 implements I {}
    static class C98 implements I {}
    static class C99 implements I {}
}



