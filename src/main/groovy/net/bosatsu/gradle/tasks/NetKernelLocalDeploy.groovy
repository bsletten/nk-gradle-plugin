/*
 * Copyright 2011 Brian Sletten
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.bosatsu.gradle.tasks

import org.gradle.api.tasks.Copy
import org.gradle.api.GradleException

class NetKernelLocalDeploy extends Copy {

    def nkdaemondir
    
    NetKernelLocalDeploy() {
    }
    
    void initialize() {
        def nkProperties = project.file("${project.netKernelRootDir}/etc/kernel.properties").eachLine { line ->
            if(line ==~ /netkernel.init.modulesdir=(.*)/ ) {
               def daemonFileProp = line.substring(26)
               if(daemonFileProp[0] == File.separatorChar) {
                   nkdaemondir = project.file(daemonFileProp)
               } else {
                   nkdaemondir = project.file("${project.netKernelRootDir}/${daemonFileProp}")
               }
            }
        }
        
        if(nkdaemondir == null) {
            throw new GradleException("Missing netkernel.init.modulesdir property in etc/kernel.properties")
        }
        
        from "${project.buildDir}/daemon"
        include "*.xml"
        into nkdaemondir
    }
}