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

import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.Platforms;
import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CFunction.Transition;
import org.graalvm.nativeimage.c.function.CLibrary;
import org.graalvm.nativeimage.c.struct.AllowNarrowingCast;
import org.graalvm.nativeimage.c.struct.CField;
import org.graalvm.nativeimage.c.struct.CStruct;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.word.PointerBase;

@CLibrary(value = "libchelper", requireStatic = true)
public class ARM32LibCHelper {

    @Platforms(Platform.ARM32.class)
    @CFunction(transition = Transition.NO_TRANSITION)
    public static native void determineCPUFeatures(CPUFeatures features);

    @Platforms(Platform.ARM32.class)
    @CFunction(transition = Transition.NO_TRANSITION)
    public static native int checkCPUFeatures(CCharPointer cCharPointer);

    @Platforms(Platform.ARM32.class)
    @CFunction(transition = Transition.NO_TRANSITION)
    public static native int checkCPUFeaturesOrExit(CCharPointer buildtimeCPUFeatureMask, CCharPointer errorMessage);

    // Checkstyle: stop
    @CStruct
    @CContext(ARM32LibCHelperDirectives.class)
    public interface CPUFeatures extends PointerBase {
        @AllowNarrowingCast
        @CField
        boolean fVFPV3();

        @AllowNarrowingCast
        @CField
        boolean fVFPV3_D32();

        @AllowNarrowingCast
        @CField
        boolean fVFPV4();

        @AllowNarrowingCast
        @CField
        boolean fNEON();

        @AllowNarrowingCast
        @CField
        boolean fTHUMB2();

        @AllowNarrowingCast
        @CField
        boolean fIDIVA();

        @AllowNarrowingCast
        @CField
        boolean fIDIVT();

        @AllowNarrowingCast
        @CField
        boolean fLPAE();

        @AllowNarrowingCast
        @CField
        boolean fEVTSTRM();

        @AllowNarrowingCast
        @CField
        boolean fAES();

        @AllowNarrowingCast
        @CField
        boolean fPMULL();

        @AllowNarrowingCast
        @CField
        boolean fSHA1();

        @AllowNarrowingCast
        @CField
        boolean fSHA2();

        @AllowNarrowingCast
        @CField
        boolean fCRC32();
    }
    // Checkstyle: resume
}
