/*
 * Copyright (c) 2013, 2017, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.svm.core.posix.headers.linux;

import static org.graalvm.nativeimage.c.function.CFunction.Transition.NO_TRANSITION;

import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.Platforms;
import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CConst;

import com.oracle.svm.core.posix.PosixStat.stat;
import com.oracle.svm.core.posix.headers.PosixDirectives;
import com.oracle.svm.shared.Uninterruptible;

// Checkstyle: stop

/**
 * Definitions manually translated from the C header file sys/stat.h.
 */
@CContext(PosixDirectives.class)
public class LinuxStat {

    public static class NoTransitions {
        @CFunction(value = "__fstat64_time64", transition = NO_TRANSITION)
        @Platforms(Platform.LINUX_ARM32.class)
        private static native int fstat_time64(int fd, stat buf);

        @CFunction(value = "fstat", transition = NO_TRANSITION)
        private static native int fstat_default(int fd, stat buf);

        @Uninterruptible(reason = "Called from uninterruptible code.", mayBeInlined = true)
        public static int fstat(int fd, stat buf) {
            if (Platform.includedIn(Platform.LINUX_ARM32.class)) {
                return fstat_time64(fd, buf);
            }
            return fstat_default(fd, buf);
        }

        @CFunction(value = "__lstat64_time64", transition = NO_TRANSITION)
        @Platforms(Platform.LINUX_ARM32.class)
        private static native int lstat_time64(@CConst CCharPointer path, stat buf);

        @CFunction(value = "lstat", transition = NO_TRANSITION)
        private static native int lstat_default(@CConst CCharPointer path, stat buf);

        @Uninterruptible(reason = "Called from uninterruptible code.", mayBeInlined = true)
        public static int lstat(@CConst CCharPointer path, stat buf) {
            if (Platform.includedIn(Platform.LINUX_ARM32.class)) {
                return lstat_time64(path, buf);
            }
            return lstat_default(path, buf);
        }
    }
}
