/*
 * Copyright (c) 2026, 2026, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
package com.oracle.svm.core.arm32;

import java.nio.ByteOrder;
import java.util.EnumSet;
import java.util.List;

import jdk.vm.ci.code.Architecture;
import jdk.vm.ci.code.CPUFeatureName;
import jdk.vm.ci.code.Register;
import jdk.vm.ci.code.Register.RegisterCategory;
import jdk.vm.ci.meta.JavaKind;
import jdk.vm.ci.meta.PlatformKind;

/**
 * Represents the 32-bit ARM (ARMv7-A, AArch32 / ARM state) architecture.
 *
 * Unlike {@code jdk.vm.ci.aarch64.AArch64} and {@code jdk.vm.ci.riscv64.RISCV64}, this class is
 * not supplied by the JDK's {@code jdk.internal.vm.ci} module.
 * It is used purely as target description data for ahead-of-time compilation.
 *
 * The 32 single-precision registers {@code s0}-{@code s31} are not modelled.
 */
public class ARM32 extends Architecture {

    public static final int INSTRUCTION_SET_STATE_BIT = 0b1;

    public static final RegisterCategory CPU = new RegisterCategory("CPU");

    // General purpose core registers, AAPCS names.
    public static final Register r0 = new Register(0, 0, "r0", CPU);
    public static final Register r1 = new Register(1, 1, "r1", CPU);
    public static final Register r2 = new Register(2, 2, "r2", CPU);
    public static final Register r3 = new Register(3, 3, "r3", CPU);
    public static final Register r4 = new Register(4, 4, "r4", CPU);
    public static final Register r5 = new Register(5, 5, "r5", CPU);
    public static final Register r6 = new Register(6, 6, "r6", CPU);
    public static final Register r7 = new Register(7, 7, "r7", CPU);
    public static final Register r8 = new Register(8, 8, "r8", CPU);
    public static final Register r9 = new Register(9, 9, "r9", CPU);
    public static final Register r10 = new Register(10, 10, "r10", CPU);
    public static final Register r11 = new Register(11, 11, "r11", CPU);
    public static final Register r12 = new Register(12, 12, "r12", CPU);

    public static final Register sp = new Register(13, 13, "sp", CPU);
    public static final Register lr = new Register(14, 14, "lr", CPU);
    public static final Register pc = new Register(15, 15, "pc", CPU);

    // AAPCS aliases of the registers declared above.

    public static final Register sb = r9;
    // Frame pointer in ARM state.
    public static final Register fp = r11;
    public static final Register ip = r12;

    public static final Register r13 = sp;
    public static final Register r14 = lr;
    public static final Register r15 = pc;

    // @formatter:off
    public static final List<Register> cpuRegisters = List.of(
        r0,  r1,  r2,  r3,  r4,  r5,  r6,  r7,
        r8,  r9,  r10, r11, r12, sp,  lr,  pc
    );
    // @formatter:on

    public static final RegisterCategory FPU = new RegisterCategory("FPU");

    // VFP double precision registers. The baseline guarantees d0-d15; d16-d31 exist only on
    // implementations with CPUFeature.VFPV3_D32.
    public static final Register d0 = new Register(16, 0, "d0", FPU);
    public static final Register d1 = new Register(17, 1, "d1", FPU);
    public static final Register d2 = new Register(18, 2, "d2", FPU);
    public static final Register d3 = new Register(19, 3, "d3", FPU);
    public static final Register d4 = new Register(20, 4, "d4", FPU);
    public static final Register d5 = new Register(21, 5, "d5", FPU);
    public static final Register d6 = new Register(22, 6, "d6", FPU);
    public static final Register d7 = new Register(23, 7, "d7", FPU);
    public static final Register d8 = new Register(24, 8, "d8", FPU);
    public static final Register d9 = new Register(25, 9, "d9", FPU);
    public static final Register d10 = new Register(26, 10, "d10", FPU);
    public static final Register d11 = new Register(27, 11, "d11", FPU);
    public static final Register d12 = new Register(28, 12, "d12", FPU);
    public static final Register d13 = new Register(29, 13, "d13", FPU);
    public static final Register d14 = new Register(30, 14, "d14", FPU);
    public static final Register d15 = new Register(31, 15, "d15", FPU);
    public static final Register d16 = new Register(32, 16, "d16", FPU);
    public static final Register d17 = new Register(33, 17, "d17", FPU);
    public static final Register d18 = new Register(34, 18, "d18", FPU);
    public static final Register d19 = new Register(35, 19, "d19", FPU);
    public static final Register d20 = new Register(36, 20, "d20", FPU);
    public static final Register d21 = new Register(37, 21, "d21", FPU);
    public static final Register d22 = new Register(38, 22, "d22", FPU);
    public static final Register d23 = new Register(39, 23, "d23", FPU);
    public static final Register d24 = new Register(40, 24, "d24", FPU);
    public static final Register d25 = new Register(41, 25, "d25", FPU);
    public static final Register d26 = new Register(42, 26, "d26", FPU);
    public static final Register d27 = new Register(43, 27, "d27", FPU);
    public static final Register d28 = new Register(44, 28, "d28", FPU);
    public static final Register d29 = new Register(45, 29, "d29", FPU);
    public static final Register d30 = new Register(46, 30, "d30", FPU);
    public static final Register d31 = new Register(47, 31, "d31", FPU);

    // @formatter:off
    public static final List<Register> fpuRegistersD16 = List.of(
        d0,  d1,  d2,  d3,  d4,  d5,  d6,  d7,
        d8,  d9,  d10, d11, d12, d13, d14, d15
    );

    public static final List<Register> fpuRegistersD32 = List.of(
        d0,  d1,  d2,  d3,  d4,  d5,  d6,  d7,
        d8,  d9,  d10, d11, d12, d13, d14, d15,
        d16, d17, d18, d19, d20, d21, d22, d23,
        d24, d25, d26, d27, d28, d29, d30, d31
    );

    public static final List<Register> valueRegistersD16 = List.of(
        r0,  r1,  r2,  r3,  r4,  r5,  r6,  r7,
        r8,  r9,  r10, r11, r12, sp,  lr,  pc,

        d0,  d1,  d2,  d3,  d4,  d5,  d6,  d7,
        d8,  d9,  d10, d11, d12, d13, d14, d15
    );

    public static final List<Register> valueRegistersD32 = List.of(
        r0,  r1,  r2,  r3,  r4,  r5,  r6,  r7,
        r8,  r9,  r10, r11, r12, sp,  lr,  pc,

        d0,  d1,  d2,  d3,  d4,  d5,  d6,  d7,
        d8,  d9,  d10, d11, d12, d13, d14, d15,
        d16, d17, d18, d19, d20, d21, d22, d23,
        d24, d25, d26, d27, d28, d29, d30, d31
    );

    /** The full architectural register file, independent of the selected features. */
    public static final List<Register> allRegisters = valueRegistersD32;
    // @formatter:on

    /**
     * Optional ARMv7 features, named after the corresponding Linux {@code AT_HWCAP} /
     * {@code AT_HWCAP2} bits.
     */
    public enum CPUFeature implements CPUFeatureName {
        /** VFPv3 floating point with the 16 double register baseline file ({@code d0}-{@code d15}). */
        VFPV3,
        /** VFPv3 with the full 32 double register file ({@code d0}-{@code d31}). */
        VFPV3_D32,
        VFPV4,
        /** Advanced SIMD (NEON). */
        NEON,
        THUMB2,
        /** Hardware {@code SDIV}/{@code UDIV} in ARM state. */
        IDIVA,
        /** Hardware {@code SDIV}/{@code UDIV} in Thumb state. */
        IDIVT,
        LPAE,
        EVTSTRM,
        AES,
        PMULL,
        SHA1,
        SHA2,
        CRC32,
    }

    private final EnumSet<CPUFeature> features;

    public ARM32(EnumSet<CPUFeature> features) {
        super("arm", ARM32Kind.DWORD, ByteOrder.LITTLE_ENDIAN, true, allRegisters, 0, 0, 0);
        this.features = features;
    }

    @Override
    public EnumSet<CPUFeature> getFeatures() {
        return features;
    }

    @Override
    public List<Register> getAvailableValueRegisters() {
        if (features.contains(CPUFeature.VFPV3_D32)) {
            return valueRegistersD32;
        }
        return valueRegistersD16;
    }

    @Override
    public PlatformKind getPlatformKind(JavaKind javaKind) {
        switch (javaKind) {
            case Boolean:
            case Byte:
                return ARM32Kind.BYTE;
            case Short:
            case Char:
                return ARM32Kind.WORD;
            case Int:
            case Object:
                return ARM32Kind.DWORD;
            case Long:
                return ARM32Kind.QWORD;
            case Float:
                return ARM32Kind.SINGLE;
            case Double:
                return ARM32Kind.DOUBLE;
            default:
                return null;
        }
    }

    @Override
    public boolean canStoreValue(RegisterCategory category, PlatformKind platformKind) {
        ARM32Kind kind = (ARM32Kind) platformKind;
        if (kind.isInteger()) {
            // Current design can't express split-register storing. 32bit can't store 64bit in one register
            return category.equals(CPU) && kind.getSizeInBytes() <= ARM32Kind.DWORD.getSizeInBytes();
        } else if (kind.isFP()) {
            return category.equals(FPU);
        }
        return false;
    }

    @Override
    public ARM32Kind getLargestStorableKind(RegisterCategory category) {
        if (category.equals(CPU)) {
            return ARM32Kind.DWORD;
        } else if (category.equals(FPU)) {
            // A VFP d register is 64 bits wide even though the core registers are 32 bits.
            return ARM32Kind.DOUBLE;
        } else {
            return null;
        }
    }
}
