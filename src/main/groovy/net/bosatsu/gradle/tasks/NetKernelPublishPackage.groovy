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

import java.io.ByteArrayInputStream
import org.gradle.api.tasks.Copy
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.MarkupBuilder
import java.security.MessageDigest

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
            def packageNode = createPackageNode( packageDir, packageFile, packageDef, ks, ksUser, ksPassword )

            project.repoHelper.finalizePublishAction(packageDir, packageFile, packageDef, packageNode, ks, ksUser, ksPassword, fileList)
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
    
    def createPackageDigest(def packageRepoFile) {
        
    	def messageDigest = MessageDigest.getInstance("MD5")
        def zf = project.zipTree(packageRepoFile).collect{ it.absolutePath }.sort()
        
        zf.each { z ->
            def f = project.file(z)
    		project.hashHelper.hashIntoDigest(messageDigest, f)
        }
        
        messageDigest.digest()
    }
    
    def createPackageNode(def packageDir, def packageFile, def packageDef, def keyStore, def keyStoreUser, def keyStorePassword) {
    
        def sw = new StringWriter()
        def xml = new MarkupBuilder(sw)
        
        def packageRepoFile = new File("${packageDir}/${packageFile}")
        
        xml.package() {
            name(packageDef['name'])

            packagedescr('Description')
            runlevel(5)
            section()
            maintainer()
            www()
            license()
            version(packageDef['version'])
            versiondescr()
            size((int) Math.floor(packageRepoFile.length() / 1000))
                        
            filename(packageFile)
            def firstLetter = packageFile.substring(0,1).toUpperCase()
            filepath("packages/${firstLetter}/")
            
            trust {
                // TODO: Change the order of this to ks, ksuser, kspass to be consistent
                def packageDigest = createPackageDigest(packageRepoFile)
                def bis = new ByteArrayInputStream(packageDigest)
                def sig = project.signHelper.signModule(bis, keyStoreUser, keyStorePassword, keyStore)
                signature(sig)
                md5(project.hashHelper.hashFile("MD5", packageRepoFile))
                sha256(project.hashHelper.hashFile("SHA-256", packageRepoFile))
            }
            
            dependencies()
        }
        
        new XmlParser().parseText(sw.toString())
    }
}