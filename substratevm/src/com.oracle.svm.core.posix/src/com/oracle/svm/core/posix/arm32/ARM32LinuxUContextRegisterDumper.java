/*
 * Copyright (c) 2026, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.svm.core.posix.arm32;

import static com.oracle.svm.core.RegisterDumper.dumpReg;

import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.Platforms;
import org.graalvm.word.PointerBase;

import com.oracle.svm.core.RegisterDumper;
import com.oracle.svm.core.log.Log;
import com.oracle.svm.core.posix.UContextRegisterDumper;
import com.oracle.svm.core.posix.headers.Signal.mcontext_linux_arm32_t;
import com.oracle.svm.core.posix.headers.Signal.ucontext_t;
import com.oracle.svm.shared.Uninterruptible;
import com.oracle.svm.shared.singletons.AutomaticallyRegisteredImageSingleton;
import com.oracle.svm.shared.singletons.traits.BuiltinTraits.DisallowLayered;
import com.oracle.svm.shared.singletons.traits.BuiltinTraits.RuntimeAccessOnly;
import com.oracle.svm.shared.singletons.traits.BuiltinTraits.SingleLayer;
import com.oracle.svm.shared.singletons.traits.SingletonTraits;

@AutomaticallyRegisteredImageSingleton(RegisterDumper.class)
@Platforms(Platform.LINUX_ARM32_BASE.class)
@SingletonTraits(access = RuntimeAccessOnly.class, layeredCallbacks = SingleLayer.class, other = DisallowLayered.class)
class ARM32LinuxUContextRegisterDumper implements UContextRegisterDumper {

    @Override
    public void dumpRegisters(Log log, ucontext_t uContext, boolean printLocationInfo, boolean allowJavaHeapAccess, boolean allowUnsafeOperations) {
        mcontext_linux_arm32_t sigcontext = uContext.uc_mcontext_linux_arm32();
        dumpReg(log, "R0   ", sigcontext.arm_r0().rawValue(), printLocationInfo, allowJavaHeapAccess, allowUnsafeOperations);
        dumpReg(log, "R1   ", sigcontext.arm_r1().rawValue(), printLocationInfo, allowJavaHeapAccess, allowUnsafeOperations);
        dumpReg(log, "R2   ", sigcontext.arm_r2().rawValue(), printLocationInfo, allowJavaHeapAccess, allowUnsafeOperations);
        dumpReg(log, "R3   ", sigcontext.arm_r3().rawValue(), printLocationInfo, allowJavaHeapAccess, allowUnsafeOperations);
        dumpReg(log, "R4   ", sigcontext.arm_r4().rawValue(), printLocationInfo, allowJavaHeapAccess, allowUnsafeOperations);
        dumpReg(log, "R5   ", sigcontext.arm_r5().rawValue(), printLocationInfo, allowJavaHeapAccess, allowUnsafeOperations);
        dumpReg(log, "R6   ", sigcontext.arm_r6().rawValue(), printLocationInfo, allowJavaHeapAccess, allowUnsafeOperations);
        dumpReg(log, "R7   ", sigcontext.arm_r7().rawValue(), printLocationInfo, allowJavaHeapAccess, allowUnsafeOperations);
        dumpReg(log, "R8   ", sigcontext.arm_r8().rawValue(), printLocationInfo, allowJavaHeapAccess, allowUnsafeOperations);
        dumpReg(log, "R9   ", sigcontext.arm_r9().rawValue(), printLocationInfo, allowJavaHeapAccess, allowUnsafeOperations);
        dumpReg(log, "R10  ", sigcontext.arm_r10().rawValue(), printLocationInfo, allowJavaHeapAccess, allowUnsafeOperations);
        dumpReg(log, "FP   ", sigcontext.arm_fp().rawValue(), printLocationInfo, allowJavaHeapAccess, allowUnsafeOperations);
        dumpReg(log, "IP   ", sigcontext.arm_ip().rawValue(), printLocationInfo, allowJavaHeapAccess, allowUnsafeOperations);
        dumpReg(log, "SP   ", sigcontext.arm_sp().rawValue(), printLocationInfo, allowJavaHeapAccess, allowUnsafeOperations);
        dumpReg(log, "LR   ", sigcontext.arm_lr().rawValue(), printLocationInfo, allowJavaHeapAccess, allowUnsafeOperations);
        dumpReg(log, "PC   ", sigcontext.arm_pc().rawValue(), printLocationInfo, allowJavaHeapAccess, allowUnsafeOperations);
        dumpReg(log, "CPSR ", sigcontext.arm_cpsr().rawValue(), false, false, false);
    }

    @Override
    @Uninterruptible(reason = "Called from uninterruptible code.", mayBeInlined = true)
    public PointerBase getHeapBase(ucontext_t uContext) {
        return (PointerBase) uContext.uc_mcontext_linux_arm32().arm_r10();
    }

    @Override
    @Uninterruptible(reason = "Called from uninterruptible code.", mayBeInlined = true)
    public PointerBase getThreadPointer(ucontext_t uContext) {
        return (PointerBase) uContext.uc_mcontext_linux_arm32().arm_r9();
    }

    @Override
    @Uninterruptible(reason = "Called from uninterruptible code.", mayBeInlined = true)
    public PointerBase getSP(ucontext_t uContext) {
        return (PointerBase) uContext.uc_mcontext_linux_arm32().arm_sp();
    }

    @Override
    @Uninterruptible(reason = "Called from uninterruptible code.", mayBeInlined = true)
    public PointerBase getIP(ucontext_t uContext) {
        return (PointerBase) uContext.uc_mcontext_linux_arm32().arm_pc();
    }
}
