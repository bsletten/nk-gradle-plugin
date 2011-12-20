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

import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip

class NetKernelPackage extends Zip {
    
    def packageName
    def packageVersion
    def packageDescription
    def modules
    def nonce
    
    NetKernelPackage() {
    }
    
    void initialize() {
        if(modules == null) {
            into('modules') {
                from "${project.buildDir}/modules"
            }
            
            project.subprojects.each { s->
                into('modules') {
                    from "${s.buildDir}/modules"
                }
            }
            
        } else {
            modules.each { m ->
                into('modules') {
                    from "${m}/build/modules"
                }
            }
        }
        
        destinationDir=project.file("${project.buildDir}/packages") 

        def name = packageName.toLowerCase()
        name = name.replaceAll(" ", "_") 

        archiveName="${name}-${packageVersion}.nkp.jar"
        
        nonce = System.currentTimeMillis()
    
        rename { String fileName ->
            fileName.replace('.jar', "-${nonce}.jar")
        }
        
        from project.tasks."nkpackage-${name}-manifest".manifestFile
        from project.tasks."nkpackage-${name}-module".moduleFile
    }
}