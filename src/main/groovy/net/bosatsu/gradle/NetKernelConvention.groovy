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

package net.bosatsu.gradle

import org.gradle.api.Project

class NetKernelConvention {
	File netKernelRootDir
	Project p

	def modules
	def relatedProjects = []

	String packageName
	String packageDescription
	String packageVersion
	
	NetKernelConvention(Project p) {
		this.p = p
		
		def overridden = false
		def location
		
		if(p.hasProperty('netkernelroot')) {
			location = p.netkernelroot
		}
		
		if(System.properties.netkernelroot) {
			overridden = netKernelRootDir != null
			location = System.properties.netkernelroot
		}
		
		if(location != null) {
			netKernelRootDir = p.file(location)

			if(netKernelRootDir.exists()) {
				if(overridden) {
					println "Overriding NetKernel installation to: ${location}"
				} else {
					println "Found a NetKernel installation at: ${location}"				
				}
			
			} else {
				println "NetKernel Gradle plugin currently requires you to specify a NetKernel installation directory."
				println 'Please put a gradle.properties file in user.home/.gradle or use: gradle -Dnetkernelhome=<installation>'
			}
		}
	}
	
	def dependsOnNetkernelModule(String moduleName) {
		Project otherProject = p.project(moduleName)

		p.repositories {
			flatDir(name: "${otherProject.projectDir}-lib", dirs: ["${otherProject.projectDir}/lib"])
		}
		
		p.dependencies {
	        compile otherProject
		}
	}
	
	def nkconfig(Closure closure) {
		closure.delegate = this
		closure()
	}
}