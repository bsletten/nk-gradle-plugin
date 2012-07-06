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

import groovy.xml.MarkupBuilder
import net.bosatsu.util.SigningHelper

import org.gradle.api.DefaultTask

class NetKernelGenerateRepoConnectionSettings extends DefaultTask {

    def settingsDir
    def archiveFileName
    def SigningHelper signingHelper = new SigningHelper()

    NetKernelGenerateRepoConnectionSettings() {
        def nonce = System.currentTimeMillis()
        settingsDir = project.file("${project.buildDir}/tmp/repo/${nonce}")
        settingsDir.mkdirs()
    }
    
    def checkForProperty(def propName, def defaultValue) {
        def retValue = defaultValue
        
        if(project.hasProperty(propName)) {
            retValue = project."${propName}"
        }
        
        retValue
    }
    
    @org.gradle.api.tasks.TaskAction
    void verifyRepo() {
        String ksUser=project.netKernelKeyStoreUser
        String ksPassword=project.netKernelKeyStorePassword
        def ks = project.netKernelRepoKeyStore
        
        def repoName = checkForProperty('netkernelpubrepo', 'NetKernelRepository')
        def repoVersion = checkForProperty('netkernelpubver', '1.0.0')
        def repoBaseURI = checkForProperty('netkernelpubbaseuri', 'http://localhost/repo/')
        def repoDescr = checkForProperty('netkernelpubdescr', 'A NetKernel Repository')
        def repoType = checkForProperty('netkernelpubtype', null)
        def typedName = repoName
        
        if(repoType != null) {
            typedName = "${repoName}-${repoType}"
        }
        
        def archiveName="apposite-reposettings-${typedName.toLowerCase()}-${repoVersion}.zip"
        
        def settingsFile = project.file("${settingsDir}/apposite-repo-settings.xml")
        def unattendedSettingsFile = project.file("${settingsDir}/unattended-apposite-repo-settings.xml")
        
        def repoDir = project.file("${project.netKernelRepoDir}/netkernel/${repoName}/${repoVersion}")
        
        if(!repoDir.exists()) {
            // TODO: Fail
        }
        
        def fileList = project.fileTree(dir: repoDir, include: '**/repository.xml')
        def sets = [] as Set
        
        fileList.each { f ->
            def parent = f.getParentFile().getName()
            sets << parent
        }
        
        // Write out the apposite settings file
                        
    	def writer = new StringWriter()
		def xml = new MarkupBuilder(writer)

        xml.repository {
            name(repoName)
            descr(repoDescr)
            baseURI(repoBaseURI)
            path("${repoName}/${repoVersion}/")
                
            sets.each { s->
                set(s)
            }
         }
         
         settingsFile.write('<?xml version="1.0" encoding="UTF-8"?>\n')
         settingsFile.append(writer.toString())
         
         // Generate a nicely-formatted certificate
         
         def sb = new StringBuffer()
         sb.append('-----BEGIN CERTIFICATE-----\n')
         String cert = signingHelper.extractCert(ks, ksUser, ksPassword)
         int len = cert.length()
         int idx = 0
         
         while((len - idx) >= 77) {
            sb.append(cert.substring(idx, idx + 76))
            sb.append("\n")
            idx += 76
         }
         
         if(idx < len) {
            sb.append(cert.substring(idx, len))
            sb.append("\n")
         }
         
         sb.append('-----END CERTIFICATE-----\n')
         
         // Write out the certificate file
         
         def certificateFile = project.file("${settingsDir}/repo-public-key.cer")
         certificateFile.write(sb.toString())
         
         // Now write out the unattended settings file w/
         // the certificate embedded
         
         writer = new StringWriter()
         xml = new MarkupBuilder(writer)
         
         xml.repository {
             name(repoName)
             descr(repoDescr)
             baseURI(repoBaseURI)
             path("${repoName}/${repoVersion}/")

             sets.each { s->
                 set(s)
             }
             
           publickey(sb.toString())
         }
          
         unattendedSettingsFile.write('<?xml version="1.0" encoding="UTF-8"?>\n')
         unattendedSettingsFile.append(writer.toString())
    }
}