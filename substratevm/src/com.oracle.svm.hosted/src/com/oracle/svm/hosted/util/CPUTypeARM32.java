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
package com.oracle.svm.hosted.util;

import static com.oracle.svm.core.arm32.ARM32.CPUFeature.IDIVA;
import static com.oracle.svm.core.arm32.ARM32.CPUFeature.IDIVT;
import static com.oracle.svm.core.arm32.ARM32.CPUFeature.NEON;
import static com.oracle.svm.core.arm32.ARM32.CPUFeature.THUMB2;
import static com.oracle.svm.core.arm32.ARM32.CPUFeature.VFPV3;
import static com.oracle.svm.core.arm32.ARM32.CPUFeature.VFPV3_D32;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.Platforms;

import com.oracle.svm.core.arm32.ARM32.CPUFeature;
import com.oracle.svm.core.util.UserError;
import com.oracle.svm.hosted.NativeImageOptions;
import com.oracle.svm.shared.option.SubstrateOptionsParser;
import com.oracle.svm.shared.util.StringUtil;

/**
 * ARM32 CPU types used to implement -march.
 * <p>
 * For reference, see <a href="https://gcc.gnu.org/onlinedocs/gcc/ARM-Options.html">gcc's ARM
 * Options</a>.
 */
public enum CPUTypeARM32 implements CPUType {
    ARMV7_A("armv7-a", InstructionSet.A32, VFPV3, THUMB2),
    ARMV7_A_NEON("armv7-a+neon", InstructionSet.A32, ARMV7_A, VFPV3_D32, NEON),
    ARMV7VE("armv7ve", InstructionSet.A32, ARMV7_A, IDIVA, IDIVT),
    ARMV7VE_NEON("armv7ve+neon", InstructionSet.A32, ARMV7VE, VFPV3_D32, NEON),

    THUMBV7_A("thumbv7-a", InstructionSet.T32, ARMV7_A),
    THUMBV7_A_NEON("thumbv7-a+neon", InstructionSet.T32, ARMV7_A_NEON),
    THUMBV7VE("thumbv7ve", InstructionSet.T32, ARMV7VE),
    THUMBV7VE_NEON("thumbv7ve+neon", InstructionSet.T32, ARMV7VE_NEON),

    // Special symbols
    COMPATIBILITY(NativeImageOptions.MICRO_ARCHITECTURE_COMPATIBILITY, InstructionSet.A32, ARMV7_A);

    public enum InstructionSet {
        A32,
        T32
    }

    private final String name;
    private final CPUTypeARM32 parent;
    private final InstructionSet instructionSet;
    private final EnumSet<CPUFeature> specificFeatures;

    CPUTypeARM32(String cpuTypeName, InstructionSet instructionSet, CPUFeature... features) {
        this(cpuTypeName, instructionSet, null, features);
    }

    CPUTypeARM32(String cpuTypeName, InstructionSet cpuTypeInstructionSet, CPUTypeARM32 cpuTypeParentOrNull, CPUFeature... features) {
        name = cpuTypeName;
        parent = cpuTypeParentOrNull;
        instructionSet = cpuTypeInstructionSet;
        specificFeatures = features.length > 0 ? EnumSet.copyOf(List.of(features)) : EnumSet.noneOf(CPUFeature.class);
        assert parent == null || parent.getFeatures().stream().noneMatch(specificFeatures::contains) : "duplicate features detected but not allowed";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CPUTypeARM32 getParent() {
        return parent;
    }

    @Override
    public String getSpecificFeaturesString() {
        return specificFeatures.stream().map(Enum::name).collect(Collectors.joining(" + "));
    }

    public InstructionSet getInstructionSet() {
        return instructionSet;
    }

    public EnumSet<CPUFeature> getFeatures() {
        if (parent == null) {
            /* A copy: callers (createTarget) add the -H:CPUFeatures values to the result. */
            return EnumSet.copyOf(specificFeatures);
        } else {
            return EnumSet.copyOf(Stream.concat(parent.getFeatures().stream(), specificFeatures.stream()).toList());
        }
    }

    public static String getDefaultName() {
        return ARMV7_A.getName();
    }

    @Platforms(Platform.HOSTED_ONLY.class)
    public static EnumSet<CPUFeature> getSelectedFeatures() {
        return getSelected().getFeatures();
    }

    @Platforms(Platform.HOSTED_ONLY.class)
    public static InstructionSet getSelectedInstructionSet() {
        return getSelected().getInstructionSet();
    }

    @Platforms(Platform.HOSTED_ONLY.class)
    private static CPUTypeARM32 getSelected() {
        String value = NativeImageOptions.MicroArchitecture.getValue();
        if (value == null) {
            value = getDefaultName();
        }
        return typeOfOrFail(value);
    }

    private static CPUTypeARM32 typeOfOrFail(String marchValue) {
        CPUTypeARM32 value = typeOf(marchValue);
        if (value == null) {
            throw UserError.abort("Unsupported architecture '%s'. Please adjust '%s'. On ARM32, only %s are available.",
                            marchValue,
                            SubstrateOptionsParser.commandArgument(NativeImageOptions.MicroArchitecture, marchValue),
                            StringUtil.joinSingleQuoted(CPUType.toNames(values())));
        }
        return value;
    }

    private static CPUTypeARM32 typeOf(String marchValue) {
        for (CPUTypeARM32 value : values()) {
            if (value.name.equals(marchValue)) {
                return value;
            }
        }
        return null;
    }
}
