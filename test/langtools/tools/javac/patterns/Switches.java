/*
 * Copyright (c) 2017, 2019, Oracle and/or its affiliates. All rights reserved.
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

import java.util.function.Function;

/*
 * @test
 * @bug 9999999
 * @summary XXX
 * @compile --enable-preview -source ${jdk.version} Switches.java
 * @run main/othervm --enable-preview Switches
 */
public class Switches {
    public static void main(String... args) {
        new Switches().run();
    }

    void run() {
        run(this::typeTestPatternSwitchTest);
        run(this::typeTestPatternSwitchExpressionTest);
        run(this::testBooleanSwitchExpression);
    }

    void run(Function<Object, Integer> mapper) {
        assertEquals(2, mapper.apply("2"));
        assertEquals(3, mapper.apply("3"));
        assertEquals(8, mapper.apply(new StringBuilder("4")));
        assertEquals(2, mapper.apply(2));
        assertEquals(3, mapper.apply(3));
        assertEquals(-1, mapper.apply(2.0));
        assertEquals(-1, mapper.apply(new Object()));
        try {
            mapper.apply(null);
            throw new AssertionError("Expected a NullPointerException, but got nothing.");
        } catch (NullPointerException ex) {
            //OK
        }
    }

    int typeTestPatternSwitchTest(Object o) {
        switch (o) {
            case String s: return Integer.parseInt(s.toString());
            case CharSequence s: return 2 * Integer.parseInt(s.toString());
            case Integer i: return i;
            case Object x: return -1;
            default: return -2; //TODO - needed?
        }
    }

    int typeTestPatternSwitchExpressionTest(Object o) {
        return switch (o) {
            case String s -> Integer.parseInt(s.toString());
            case CharSequence s -> { yield 2 * Integer.parseInt(s.toString()); }
            case Integer i -> i;
            case Object x -> -1;
            default -> -2; //TODO - needed?
        };
    }

    int testBooleanSwitchExpression(Object o) {
        Object x;
        if (switch (o) {
            case String s -> (x = s) != null;
            default -> false;
        }) {
            return Integer.parseInt(x.toString());
        } else if (switch (o) {
            case CharSequence s -> {
                x = s;
                yield true;
            }
            default -> false;
        }) {
            return 2 * Integer.parseInt(x.toString());
        }
        return typeTestPatternSwitchTest(o);
    }

    void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("Expected: " + expected + ", but got: " + actual);
        }
    }
}
