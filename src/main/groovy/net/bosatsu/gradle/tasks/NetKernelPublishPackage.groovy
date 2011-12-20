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
import groovy.xml.MarkupBuilder

class NetKernelPublishPackage extends Copy {

    def packageTask
    def packageDir
    def packageFile
    def packageDef
    
    NetKernelPublishPackage() {
        doFirst {
            project.repoHelper.initiatePublishAction(packageDir, packageFile, packageDef)
        }
        
        doLast {
            String ksUser=project.netKernelKeyStoreUser
            String ksPassword=project.netKernelKeyStorePassword
            def ks = project.netKernelRepoKeyStore
            def name = packageDef['repo']
            def version = packageDef['repoversion']
            def repoDir = project.file("${project.netKernelRepoDir}/netkernel/${name}/${version}")
            def fileList = project.fileTree(dir: repoDir, include: '**/repository.xml')
            project.repoHelper.finalizePublishAction(packageDir, packageFile, packageDef, ks, ksUser, ksPassword, fileList)
        }
    }
    
    void initialize() {
        println "Publishing: $name"
        packageFile = project.tasks."${packageTask}".archiveName

        def firstLetter = packageFile.substring(0,1).toUpperCase()

        packageDir = project.file("${project.netKernelRepoDir}/packages/${firstLetter}")
        
        from "${project.buildDir}/packages"
        include packageFile
        into packageDir
    }
}