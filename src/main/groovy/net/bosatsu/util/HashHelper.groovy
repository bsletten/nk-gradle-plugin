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

import java.security.MessageDigest

class HashHelper {
	int KB = 1024
	int MB = 1024*KB

	String hashFile(String hashFunction, File file) {
		String retValue = null
		
		if(file == null || !file.exists() || !file.isFile() || !file.canRead()) {
			throw new IllegalArgumentException("Bad file: ${file}")
		}
		
		def messageDigest = MessageDigest.getInstance(hashFunction)
		
		file.eachByte(MB) { byte[] buf, int bytesRead ->
			messageDigest.update(buf, 0, bytesRead)
		}
		
		retValue = new BigInteger(1, messageDigest.digest()).toString(16)
		retValue
	}
}