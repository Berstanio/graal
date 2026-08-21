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
package com.oracle.svm.core.graal.arm32;

import static com.oracle.svm.core.arm32.ARM32.*;
import static com.oracle.svm.shared.util.VMError.shouldNotReachHereUnexpectedInput;
import static com.oracle.svm.shared.util.VMError.unsupportedFeature;
import static com.oracle.svm.shared.util.VMError.unsupportedPlatform;

import java.util.ArrayList;
import java.util.List;

import org.graalvm.nativeimage.Platform;

import com.oracle.svm.core.ReservedRegisters;
import com.oracle.svm.core.arm32.ARM32;
import com.oracle.svm.core.config.ObjectLayout;
import com.oracle.svm.core.graal.code.SubstrateCallingConvention;
import com.oracle.svm.core.graal.code.SubstrateCallingConventionKind;
import com.oracle.svm.core.graal.code.SubstrateCallingConventionType;
import com.oracle.svm.core.graal.meta.SubstrateRegisterConfig;
import com.oracle.svm.shared.util.VMError;

import jdk.vm.ci.code.CallingConvention;
import jdk.vm.ci.code.CallingConvention.Type;
import jdk.vm.ci.code.Register;
import jdk.vm.ci.code.RegisterAttributes;
import jdk.vm.ci.code.StackSlot;
import jdk.vm.ci.code.TargetDescription;
import jdk.vm.ci.code.ValueKindFactory;
import jdk.vm.ci.meta.AllocatableValue;
import jdk.vm.ci.meta.JavaKind;
import jdk.vm.ci.meta.JavaType;
import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.PlatformKind;
import jdk.vm.ci.meta.Value;
import jdk.vm.ci.meta.ValueKind;

/**
 * AAPCS (Procedure Call Standard for the ARM Architecture, hard-float / VFP variant).
 */
public class SubstrateARM32RegisterConfig implements SubstrateRegisterConfig {

    private final TargetDescription target;
    private final int nativeParamsStackOffset;
    private final List<Register> generalParameterRegs;
    private final List<Register> fpParameterRegs;
    private final List<Register> allocatableRegs;
    private final List<Register> calleeSaveRegisters;
    private final List<RegisterAttributes> attributesMap;
    private final MetaAccessProvider metaAccess;

    @SuppressWarnings("this-escape")
    public SubstrateARM32RegisterConfig(ConfigKind config, MetaAccessProvider metaAccess, TargetDescription target, boolean preserveFramePointer) {
        this.target = target;
        this.metaAccess = metaAccess;

        generalParameterRegs = List.of(r0, r1, r2, r3);
        /*
         * Hard-float AAPCS passes floating point arguments in the VFP registers; only the double
         * registers are modelled, so a float uses the low half of the corresponding d register.
         */
        fpParameterRegs = List.of(d0, d1, d2, d3, d4, d5, d6, d7);

        nativeParamsStackOffset = 0;

        ArrayList<Register> regs = new ArrayList<>(target.arch.getAvailableValueRegisters());
        regs.remove(sp); // r13, the stack pointer
        regs.remove(pc); // r15, the program counter

        regs.remove(ReservedRegisters.singleton().getThreadRegister());
        regs.remove(ReservedRegisters.singleton().getHeapBaseRegister());
        regs.remove(ReservedRegisters.singleton().getCodeBaseRegister());
        if (preserveFramePointer)
            regs.remove(fp); // r11, the frame pointer in ARM mode
        allocatableRegs = List.copyOf(regs);

        switch (config) {
            case NORMAL:
                calleeSaveRegisters = List.of();
                break;

            case NATIVE_TO_JAVA:
                calleeSaveRegisters = List.of(sp, r4, r5, r6, r7, r8, r9, r10, r11,
                                d8, d9, d10, d11, d12, d13, d14, d15);
                break;

            default:
                throw shouldNotReachHereUnexpectedInput(config); // ExcludeFromJacocoGeneratedReport
        }

        attributesMap = RegisterAttributes.createMap(this, ARM32.allRegisters);
    }

    @Override
    public Register getReturnRegister(JavaKind kind) {
        switch (kind) {
            case Boolean:
            case Byte:
            case Char:
            case Short:
            case Int:
            case Object:
                return r0;
            case Long:
                throw VMError.intentionallyUnimplemented(); // ExcludeFromJacocoGeneratedReport
            case Float:
            case Double:
                return d0;
            case Void:
                return null;
            default:
                throw VMError.shouldNotReachHereUnexpectedInput(kind); // ExcludeFromJacocoGeneratedReport
        }
    }

    @Override
    public List<Register> getAllocatableRegisters() {
        return allocatableRegs;
    }

    @Override
    public List<Register> getCalleeSaveRegisters() {
        return calleeSaveRegisters;
    }

    @Override
    public List<Register> getCallerSaveRegisters() {
        return getAllocatableRegisters();
    }

    @Override
    public boolean areAllAllocatableRegistersCallerSaved() {
        return true;
    }

    @Override
    public List<RegisterAttributes> getAttributesMap() {
        return attributesMap;
    }

    @Override
    public List<Register> getCallingConventionRegisters(Type t, JavaKind kind) {
        throw VMError.intentionallyUnimplemented(); // ExcludeFromJacocoGeneratedReport
    }

    private int javaStackParameterAssignment(ValueKindFactory<?> valueKindFactory, AllocatableValue[] locations, int index, JavaKind kind, int currentStackOffset, boolean isOutgoing) {
        /* All parameters within Java are assigned slots of at least one word. */
        ValueKind<?> valueKind = valueKindFactory.getValueKind(kind.getStackKind());
        int alignment = Math.max(valueKind.getPlatformKind().getSizeInBytes(), target.wordSize);
        locations[index] = StackSlot.get(valueKind, currentStackOffset, !isOutgoing);
        return currentStackOffset + alignment;
    }

    @Override
    public CallingConvention getCallingConvention(Type t, JavaType returnType, JavaType[] parameterTypes, ValueKindFactory<?> valueKindFactory) {
        SubstrateCallingConventionType type = (SubstrateCallingConventionType) t;

        if (type.customABI())
            throw unsupportedFeature("ARM32: custom calling conventions are not yet supported.");

        if (!Platform.includedIn(Platform.LINUX.class))
            throw unsupportedPlatform();

        boolean isEntryPoint = type.nativeABI() && !type.outgoing;

        AllocatableValue[] locations = new AllocatableValue[parameterTypes.length];

        int currentGeneral = 0;
        int currentFP = 0;

        /*
         * We have to reserve a slot between return address and outgoing parameters for the
         * deoptimized frame (eager deoptimization), or the original return address (lazy
         * deoptimization). Exception: calls to native methods.
         */
        int currentStackOffset = (type.nativeABI() ? nativeParamsStackOffset : target.wordSize);

        JavaKind[] kinds = new JavaKind[locations.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            JavaKind kind = ObjectLayout.getCallSignatureKind(isEntryPoint, parameterTypes[i], metaAccess, target);
            kinds[i] = kind;

            Register register = null;
            if (type.kind == SubstrateCallingConventionKind.ForwardReturnValue) {
                throw VMError.intentionallyUnimplemented();
            } else {
                switch (kind) {
                    case Byte:
                    case Boolean:
                    case Short:
                    case Char:
                    case Int:
                    case Object:
                        if (currentGeneral < generalParameterRegs.size()) {
                            register = generalParameterRegs.get(currentGeneral++);
                        }
                        break;
                    case Long:
                        /*
                        * We can't model 64bit values in a single register. So we just don't. The resulting locations are
                        * never consumed (see below)
                        * */
                        break;
                    case Float:
                    case Double:
                        if (currentFP < fpParameterRegs.size()) {
                            register = fpParameterRegs.get(currentFP++);
                        }
                        break;
                    default:
                        throw shouldNotReachHereUnexpectedInput(kind); // ExcludeFromJacocoGeneratedReport
                }
            }
            if (register != null) {
                locations[i] = register.asValue(valueKindFactory.getValueKind(isEntryPoint ? kind : kind.getStackKind()));
            } else {
                if (type.nativeABI()) {
                    ValueKind<?> valueKind = valueKindFactory.getValueKind(type.outgoing ? kind.getStackKind() : kind);
                    int alignment = Math.max(kind.getByteCount(), target.wordSize);
                    locations[i] = StackSlot.get(valueKind, currentStackOffset, !type.outgoing);
                    currentStackOffset = currentStackOffset + alignment;
                } else {
                    currentStackOffset = javaStackParameterAssignment(valueKindFactory, locations, i, kind, currentStackOffset, type.outgoing);
                }
            }
        }

        // No-one consumes the return value and only `JNICallTrampolineMethod#createCustomCompileFunction` consumes the locations
        // `createCustomCompileFunction` only statically supplies arguments, all known to not be long, so this is safe.
        return new SubstrateCallingConvention(type, kinds, currentStackOffset, Value.ILLEGAL, locations);
    }

    @Override
    public List<Register> filterAllocatableRegisters(PlatformKind kind, List<Register> registers) {
        throw VMError.intentionallyUnimplemented(); // ExcludeFromJacocoGeneratedReport
    }
}
