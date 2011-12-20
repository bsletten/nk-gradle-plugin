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

package net.bosatsu.util.netkernel

import groovy.xml.StreamingMarkupBuilder
import groovy.xml.MarkupBuilder

import net.bosatsu.util.HashHelper
import net.bosatsu.util.SigningHelper

class RepoHelper {
    int packageCount
    
    HashHelper hashHelper = new HashHelper()
    SigningHelper signHelper = new SigningHelper()
    
    File repoDir
    
    RepoHelper(File repoDir) {
        this.repoDir = repoDir
    }
    
    boolean repoExists() {
        return repoDir.exists()
    }
    
    boolean createRepository() {
        def repoNKDir = new File("${repoDir}/netkernel")
        def repoPackagesDir = new File("${repoDir}/packages")
        
        repoNKDir.mkdirs() && repoPackagesDir.mkdirs()
    }
    
    def initiatePublishAction(def packageDir, def packageFile, def packageDef) {
        def name = packageDef['repo']
        def version = packageDef['repoversion']
        
        checkRepositoryDir(name, version)
        
        if(packageDef['set'] == null) {
            // TODO: Fail
        }
        
        checkSetDir(name, version, packageDef['set'])
        
        // TODO: Thread safety? File lock?
        packageCount++
    }
    
    def getRepo(def name, def version, def distributionType, def set) {
        def repoFile = new File("${repoDir}/netkernel/${name}/${version}/${distributionType}/${set}/repository.xml")
        new XmlParser().parse(repoFile)
    }
    
    def storeRepo(def node, def name, def version, def distributionType, def set) {
        def repoFile = new File("${repoDir}/netkernel/${name}/${version}/${distributionType}/${set}/repository.xml")
        def writer = new PrintWriter(repoFile)
        writer.print('<?xml version="1.0" encoding="UTF-8"?>\n')
        
        def nodePrinter = new XmlNodePrinter(writer)
        nodePrinter.setPreserveWhitespace(true)
        nodePrinter.print(node)
        writer.close()
    }
    
    def createPackageNode(def root, def packageDir, def packageFile, def packageDef, def keyStore, def keyStoreUser, def keyStorePassword) {
    
        def sw = new StringWriter()
        def xml = new MarkupBuilder(sw)
        
        def packageRepoFile = new File("${packageDir}/${packageFile}")
        
        xml.package() {
            name(packageDef['name'])

            packagedescr('Description')
            runLevel(5)
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
                signature(signHelper.signFileSignature(packageRepoFile, keyStoreUser, keyStorePassword, keyStore))
                md5(hashHelper.hashFile("MD5", packageRepoFile))
                sha256(hashHelper.hashFile("SHA-256", packageRepoFile))
            }
            
            dependencies()
        }
        
        new XmlParser().parseText(sw.toString())
    }
    
    boolean canAddToRepo(def repo, String name) {
        repo.package.size() == 0 || repo.package.findAll{ it.name.text() == name }.size() == 0
    }
    
    def finalizePublishAction(def packageDir, def packageFile, def packageDef, def keyStore, def keyStoreUser, def keyStorePassword, def repositoryFiles) {
        
        def name = packageDef['repo']
        def version = packageDef['repoversion']
        def set = packageDef['set']

        def repo = getRepo(name, version, 'base', set)
        def distributionType = 'base'
        
        if(!canAddToRepo(repo, packageDef['name'])) {
            repo = getRepo(name, version, 'update', set)
            distributionType = 'update'
            
            if(!canAddToRepo(repo, packageDef['name'])) {
                // TODO: Fail
            }
        }
        
        repo.children().add(createPackageNode(repo, packageDir, packageFile, packageDef, keyStore, keyStoreUser, keyStorePassword))

        storeRepo(repo, name, version, distributionType, set)
        
        // TODO: Thread safety? File lock?
        --packageCount
        
        if(packageCount == 0) {
            rehashRepository(packageDef, keyStore, keyStoreUser, keyStorePassword, repositoryFiles)
        }
    }
    
    def checkRepositoryDir(def name, def version) {
        if(name == null) {
            // TODO: FAIL
            println "A particular repository must be specified via the 'repo' attribute in definePackage()"
        }
        
        def repositoryDir = new File("${repoDir}/netkernel/${name}")
        
        if(!repositoryDir.exists()) {
            if(repositoryDir.mkdirs()) {
                println "Created directory: ${repositoryDir}"
            } else {
                // TODO: Fail
            }
        }
        
        if(version == null) {
            // TODO:
            // if(there are versions already under this repository, use the latest)
            // else
            version = "1.0.0"
        }
        
        def repositoryVersionDir = new File("${repositoryDir}/${version}")
        
        if(!repositoryVersionDir.exists()) {
            if(repositoryVersionDir.mkdirs()) {
                println "Created directory: ${repositoryVersionDir}"
                
                def baseDir = new File("${repositoryVersionDir}/base")
                def updateDir = new File("${repositoryVersionDir}/update")
                def securityDir = new File("${repositoryVersionDir}/security")
                
                if(baseDir.mkdirs() && updateDir.mkdirs() && securityDir.mkdirs()) {
                    println "Created directory: ${baseDir}"
                    println "Created directory: ${updateDir}"
                    println "Created directory: ${securityDir}"
                } else {
                    // TODO: Fail
                }
            } else {
                // TODO: Fail
            }
        }
    }
    
    def createEmptyPackagesFile(def setDir) {
    
        def repositoryFile = new File("${setDir}/repository.xml")
        def markupBuilder = new StreamingMarkupBuilder()
        markupBuilder.encoding = "UTF-8"
        def packages = markupBuilder.bind {
            mkp.xmlDeclaration()
            packages {
            }
        }
        
        def writer = new FileWriter(repositoryFile)
        packages.writeTo(writer)
        writer.close()
    }
    
    def createSetDirIfNeeded(def name, def version, def set, def distributionType) {
        def setDir = new File("${repoDir}/netkernel/${name}/${version}/${distributionType}/${set}")
        
        if(!setDir.exists()) {
            if(setDir.mkdirs()) {
                println "Created directory: ${setDir}"
                
                createEmptyPackagesFile(setDir)
            }
        }
    }
    
    def checkSetDir(def name, def version, def set) {
        createSetDirIfNeeded(name, version, set, 'base')
        createSetDirIfNeeded(name, version, set, 'update')
        createSetDirIfNeeded(name, version, set, 'security')
    }
    
    boolean rehashRepository(def packageDef, def keyStore, def keyStoreUser, def keyStorePassword, def repositoryFiles) {
        println "Rehashing the repository"
        
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        
        xml.hashes {
            repositoryFiles.each { f->
				hash {
					def parts = "${f}".split(File.separator)
					int len = parts.length
					def s = "${parts[len-3]}/${parts[len-2]}/${parts[len-1]}"

					file(s)
					md5(hashHelper.hashFile("MD5", f))
	                sha256(hashHelper.hashFile("SHA-256", f))
				}
            }
        }

        def name = packageDef['repo']
        def version = packageDef['repoversion']
		def hashFile = new File("${repoDir}/netkernel/${name}/${version}/hashes.xml")
		def hashSigFile = new File("${repoDir}/netkernel/${name}/${version}/hashes.sig")

		hashFile.write('<?xml version="1.0" encoding="UTF-8"?>\n' + writer.toString())

		def signature = signHelper.signFileSignature(hashFile, keyStoreUser, keyStorePassword, keyStore)
		hashSigFile.write(signature)
    }
}