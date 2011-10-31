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

package net.bosatsu.util

class JarInfoHelper {
	JarInfo parseJarName(String jarName) {
		if((jarName == null) || (!jarName.endsWith(".jar"))) {
			throw new IllegalArgumentException("Invalid jar file name")
		}
		
		def base = jarName - '.jar'
		def version
		def classifier
		
		if(base.indexOf("-") > 0) {
			def parts = base.split("-")
			
			if(parts[-1] ==~ /(\d+\.)?(\d+\.)?\d+/) {
				version = parts[-1]
				base = base.substring(0, base.lastIndexOf("-"))
			} else if(parts[-2] ==~ /(\d+\.)?(\d+\.)?\d+/) {
				// b-v-c
				// b1-b2-v-c
				classifier = parts[-1]
				version = parts[-2]
				base = parts[0..(parts.length - 3)].join('-')
			} else {
				// We aren't matching a pattern we are expecting, we'll just
				// try to do something reasonable
				classifier = parts[-1]
				base = base.substring(0, base.lastIndexOf("-"))
			}
		}
		
		def retValue = new JarInfo()
		retValue.base = base
		retValue.version = version
		retValue.classifier = classifier
		
		retValue
	}
}