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

import net.bosatsu.util.netkernel.ModuleHelper

class NetKernelPackageDaemonFile extends DefaultTask {
	def moduleHelper = new ModuleHelper()
	
	def packageName
	def packageModules
	def daemonFile
	
	def NetKernelPackageDaemonFile() {
	}
	
	@org.gradle.api.tasks.TaskAction
	def writeFile() {
		def tmpDir = project.file("${project.buildDir}/daemon")
		tmpDir.mkdirs()

		daemonFile = project.file("${project.buildDir}/daemon/${packageName}.xml")
		
		def writer = new StringWriter()
		def xml = new MarkupBuilder(writer)
		
		def projectDir = project.file('.')

		xml.modules() {
		    packageModules.each { pm ->
		        module("file:${projectDir}/${pm}/")
		    }
		}
	
        daemonFile.write('<?xml version="1.0" encoding="UTF-8"?>\n')
        daemonFile.write(writer.toString())
	}
}