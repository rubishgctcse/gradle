/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.ide.visualstudio.fixtures;

import org.gradle.nativeplatform.fixtures.AvailableToolChains;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.version.VswhereVersionLocator;
import org.gradle.test.fixtures.file.TestFile;
import org.gradle.util.VersionNumber;

import java.io.File;

public class MSBuildVersionLocator {
    private final VswhereVersionLocator vswhereLocator;

    public MSBuildVersionLocator(VswhereVersionLocator vswhereLocator) {
        this.vswhereLocator = vswhereLocator;
    }

    public File getMSBuildInstall(AvailableToolChains.InstalledToolChain toolChain) {
        VersionNumber vsVersion;
        if (toolChain instanceof AvailableToolChains.InstalledVisualCpp) {
            AvailableToolChains.InstalledVisualCpp visualCpp = (AvailableToolChains.InstalledVisualCpp) toolChain;
            vsVersion = visualCpp.getVersion();
        } else {
            vsVersion = VersionNumber.version(15);
        }

        File vswhere = vswhereLocator.getVswhereInstall();
        if (vswhere == null) {
            throw new IllegalStateException("vswhere tool is required to be installed");
        }

        TestFile installDir = new TestFile(new TestFile(vswhere).exec("-version", String.format("[%s.0,%s.0)", vsVersion.getMajor(), vsVersion.getMajor() + 1), "-products", "*", "-requires", "Microsoft.Component.MSBuild", "-property", "installationPath").getOut().trim());

        // TODO: Remove the hardcoded version, see https://github.com/Microsoft/vswhere/issues/74
        TestFile msbuild = installDir.file("MSBuild/" + vsVersion.getMajor() + ".0/Bin/MSBuild.exe");
        if (!msbuild.exists()) {
            throw new IllegalStateException(String.format("This test requires MSBuild %s to be installed. Expected it to be installed at %s.", vsVersion.getMajor(), msbuild));
        }
        return msbuild;
    }
}
