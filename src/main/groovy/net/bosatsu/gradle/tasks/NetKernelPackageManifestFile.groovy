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

import net.bosatsu.gradle.NKModuleHelper

class NetKernelPackageManifestFile extends DefaultTask {
	def File manifestFile
	
	def moduleHelper = new NKModuleHelper()
	
	def nonceTaskName
	def packageName
	def packageVersion
	def packageDescription
	def packageModules
	
	def NetKernelPackageManifestFile() {
		manifestFile = project.file("${project.buildDir}/tmp/${name}/manifest.xml")
	}
	
	@org.gradle.api.tasks.TaskAction
	def writeFile() {
		def tmpDir = project.file("${project.buildDir}/tmp/${name}")
		tmpDir.mkdirs()
	
		def writer = new StringWriter()
		def xml = new MarkupBuilder(writer)
	
		def modules = []
	
		if(packageModules != null) {
			packageModules.each { p ->
				modules << moduleHelper.getModuleInfo(project.file("${p}/module.xml"))
			}
		} else {
			if(project.subprojects.size() == 0) {
				modules << moduleHelper.getModuleInfo(project.file('module.xml'))
			} else {
				project.subprojects.each { s ->
					modules << moduleHelper.getModuleInfo(s.file('module.xml'))
				}
			}
		}
		
		def nonce = project.tasks."$nonceTaskName".nonce
	
		xml.manifest() {
			name(packageName)
			description(packageDescription)
			version(packageVersion)
		
			modules.each { m ->
				def u = m.meta.identity.uri.text()
				def v = m.meta.identity.version.text()
		
				module {
					uri(u)
					version(v)
					runlevel(5)
					def shapedURI = u.replaceAll(':', '.')
					source("modules/$shapedURI-$v-${nonce}.jar")
					expand(true)
				}
			}
		}
	
		manifestFile.write('<?xml version="1.0" encoding="UTF-8"?>\n')
		manifestFile.write(writer.toString())
	}
}