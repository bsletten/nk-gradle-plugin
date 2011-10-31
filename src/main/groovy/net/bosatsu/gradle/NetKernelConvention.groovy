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
	
	NetKernelConvention(Project p) {
		this.p = p
		netKernelRootDir = p.file(System.properties.netkernelroot)
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
}