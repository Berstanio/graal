/*
 * Copyright (c) 2016, 2019, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.svm.core.posix.headers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.graalvm.nativeimage.Platform;

import com.oracle.svm.hosted.c.DirectivesExtension;
import com.oracle.svm.shared.util.VMError;

public class PosixDirectives implements DirectivesExtension {
    private static final String[] commonLibs = new String[]{
                    "<dlfcn.h>",
                    "<dirent.h>",
                    "<fcntl.h>",
                    "<limits.h>",
                    "<locale.h>",
                    "<pthread.h>",
                    "<pwd.h>",
                    "<semaphore.h>",
                    "<signal.h>",
                    "<errno.h>",
                    "<sys/file.h>",
                    "<sys/mman.h>",
                    "<sys/resource.h>",
                    "<sys/stat.h>",
                    "<sys/time.h>",
                    "<sys/times.h>",
                    "<sys/types.h>",
                    "<sys/utsname.h>",
                    "<time.h>",
                    "<unistd.h>",
    };

    private static final String[] darwinLibs = new String[]{
                    "<Foundation/Foundation.h>",
                    "<mach/mach.h>",
                    "<mach/semaphore.h>",
                    "<mach/mach_time.h>",
                    "<mach-o/dyld.h>",
                    "<sys/sysctl.h>",
                    "<sys/syslimits.h>",
    };

    private static final String[] linuxLibs = new String[]{
                    "<mntent.h>",
    };

    private static final List<String> commonMacros = List.of("_GNU_SOURCE", "_LARGEFILE64_SOURCE", "_DARWIN_USE_64_BIT_INODE");

    private static final List<String> lp32LinuxMacros = List.of("_FILE_OFFSET_BITS 64", "_TIME_BITS 64");

    @Override
    public boolean isInConfiguration() {
        return Platform.includedIn(Platform.LINUX.class) || Platform.includedIn(Platform.DARWIN.class);
    }

    @Override
    public List<String> getHeaderFiles() {
        List<String> result = new ArrayList<>(Arrays.asList(commonLibs));
        if (Platform.includedIn(Platform.LINUX.class)) {
            result.addAll(Arrays.asList(linuxLibs));
        } else if (Platform.includedIn(Platform.DARWIN.class)) {
            result.addAll(Arrays.asList(darwinLibs));
        } else {
            throw VMError.shouldNotReachHere("Unsupported OS");
        }
        return result;
    }

    @Override
    public List<String> getOptions() {
        if (Platform.includedIn(Platform.DARWIN.class)) {
            return Collections.singletonList("-ObjC");
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getMacroDefinitions() {
        List<String> result = new ArrayList<>(commonMacros);
        if (Platform.includedIn(Platform.LINUX_ARM32.class)) {
            result.addAll(lp32LinuxMacros);
        }
        return result;
    }

    @Override
    public List<String> getHeaderSnippet() {
        if (!Platform.includedIn(Platform.LINUX_ARM32.class)) {
            return Collections.emptyList();
        }
        return Arrays.asList(
                        "#include <time.h>",
                        "#include <dirent.h>",
                        "_Static_assert(sizeof(((struct timespec *) 0)->tv_sec) == 8, \"measured time_t is not 64 bit: define _TIME_BITS=64 (needs glibc 2.34+)\");",
                        "_Static_assert(sizeof(((struct dirent *) 0)->d_ino) == 8, \"measured ino_t/off_t are not 64 bit: define _FILE_OFFSET_BITS=64\");");
    }
}
