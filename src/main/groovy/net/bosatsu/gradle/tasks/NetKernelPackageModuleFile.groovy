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

import org.gradle.api.DefaultTask
import groovy.xml.MarkupBuilder

class NetKernelPackageModuleFile extends DefaultTask {
    def File moduleFile

    def packageName
    def packageVersion
    def packageDescription
    
    NetKernelPackageModuleFile() {
        moduleFile = project.file("${project.buildDir}/tmp/${name}/module.xml") 
    }
    
    @org.gradle.api.tasks.TaskAction
    def writeFile() {
        def tmpDir = project.file("${project.buildDir}/tmp/${name}")
        tmpDir.mkdirs()
        
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        
        xml.module(version: '2.0') {
            meta {
                // This is not a typo. We need to avoid calling the
                // identity method. There is probably a better way to
                // do this but for now we emit a different name and then
                // string replace it.
                
                ideentity {
                    def name = packageName.toLowerCase().replaceAll(' ', '_')
                    uri( "urn:user:created:package:$name" )
                    version( packageVersion )
                }
                
                info {
                    name( packageName )
                    description( packageDescription )
                }
            }
            
            system()
            
            rootspace {
                fileset {
                    regex('res:/(module\\.(xml|signature)|manifest.xml|modules/.*?|etc/system/.*?)')
                }
            }
        }
        
        moduleFile.write('<?xml version="1.0" encoding="UTF-8"?>\n')
        moduleFile.write(writer.toString().replaceAll('ideentity', 'identity'))
    }
}