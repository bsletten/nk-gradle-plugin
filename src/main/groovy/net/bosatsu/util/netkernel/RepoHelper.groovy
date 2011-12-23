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
    
    boolean verifyRepoHashFiles(def repo, def ks, def ksUser, def ksPassword) {
        def retValue = false
        def hashesFile = new File("${repo}/hashes.xml")
        def hashesSigFile = new File("${repo}/hashes.sig")
        
        println "${hashesFile}"
        println "${hashesSigFile}"
        
        if(hashesFile.exists() && hashesSigFile.exists()) {
            def hashesSignature = signHelper.signFileSignature(hashesFile, ksUser, ksPassword, ks)
            
            if(hashesSignature.equals(hashesSigFile.getText())) {
                retValue = true
            } else {
                println "Generated signature of ${hashesFile} does not match contents of ${hashesSigFile}"
            }
        }
        
        println "${repo} : ${retValue}"
        retValue
    }
    
    boolean verifyRepoPackageFiles(def repo, def ks, def ksUser, def ksPassword) {
        def hashesFile = new File("${repo}/hashes.xml")
        def hashes = new XmlSlurper().parse(hashesFile)
        def retValue = true
        
        hashes.hash.each { h ->
            // Short circuit on failure. Doesn't seem to be a clean
            // way to stop otherwise.
            
            println "Checking: ${h.file}"
            
            if(retValue) {
                def f = new File("${repo}/${h.file}")
                
                // Check the SHA-256 hash

                retValue = hashHelper.hashFile("SHA-256", f).equals(h.sha256.text())
                
                // If we are still valid, check the MD5 hash

                if(retValue) {
                    retValue = hashHelper.hashFile("MD5", f).equals(h.md5.text())
                    
                    if(!retValue) {
                        println "MD5 hash check failed for ${f} in ${hashesFile}"
                    }
                } else {
                    println "SHA-256 hash check failed for ${f} in ${hashesFile}"
                }
                
                // If we are still valid, check the package files themselves
                
                def mainRepoDir = repo.parentFile.parentFile.parentFile
                
                if(retValue) {
                    def packages = new XmlSlurper().parse(f)
                    
                    packages.package.each { p ->
                        def filename = p.filename.text()
                        def filepath = p.filepath.text()
                        
                        def packageFile = new File("${mainRepoDir}/${filepath}/${filename}")
                        def packageSignature = signHelper.signFileSignature(packageFile, ksUser, ksPassword, ks)
                        
                        retValue = packageSignature.equals(p.trust.signature.text())
                        
                        if(retValue) {
                            retValue = hashHelper.hashFile("SHA-256", packageFile).equals(p.trust.sha256.text())

                            if(retValue) {
                                retValue = hashHelper.hashFile("MD5", packageFile).equals(p.trust.md5.text())
                                
                                if(!retValue) {
                                    println "MD5 hash check failed for ${packageFile} in ${f}"
                                }
                            } else {
                                println "SHA-256 hash check failed for ${packageFile} in ${f}"
                            }
                        } else {
                            println "Generated signature of ${packageFile} does not match contents of ${f}" 
                        }
                    }
                }
             }
        }
        
        retValue
    }
    
    boolean verifySpecificRepo(def repo, def ks, def ksUser, def ksPassword) {
        boolean retValue = true
        
        verifyRepoHashFiles(repo, ks, ksUser, ksPassword) && verifyRepoPackageFiles(repo, ks, ksUser, ksPassword)
    }
    
    def verifyRepository(def netKernelRepoDir, def ks, def ksUser, def ksPassword) {
    
        def brokenRepos = []

        // Find all the named repositories
        def reposDir = new File("${netKernelRepoDir}/netkernel").listFiles()
        
        // Check every version of every named repository
        reposDir.each { r ->
            def repoVersionDir = new File("${r}").listFiles()
            def invalid = repoVersionDir.findAll { !verifySpecificRepo( it, ks, ksUser, ksPassword ) }
            
            invalid.each { i ->
                brokenRepos << i
            }
        }
        
        brokenRepos
    }
}