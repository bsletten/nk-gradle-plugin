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
	
	def HEX_CHAR_TABLE = [
       (byte) '0',
       (byte) '1',
       (byte) '2',
       (byte) '3',
       (byte) '4',
       (byte) '5',
       (byte) '6',
       (byte) '7',
       (byte) '8',
       (byte) '9',
       (byte) 'a',
       (byte) 'b',
       (byte) 'c',
       (byte) 'd',
       (byte) 'e',
       (byte) 'f'
    ]

	String hashFile(String hashFunction, File file) {
		String retValue = null
		
		if(file == null || !file.exists() || !file.isFile() || !file.canRead()) {
			throw new IllegalArgumentException("Bad file: ${file}")
		}
		
		def messageDigest = MessageDigest.getInstance(hashFunction)
		hashIntoDigest(messageDigest, file)
		
        toHexString(messageDigest.digest())
	}
	
	void hashIntoDigest(def digest, File file) {
    	file.eachByte(MB) { byte[] buf, int bytesRead ->
    	    digest.update(buf, 0, bytesRead)
    	}	    
	}
	
	/**
     * Shamelessly stolen from 1060 Research
     */
    private String toHexString(byte[] ba) {
       byte[] hex = new byte[2 * ba.length];
       int index = 0;
       for (byte b : ba) {
          int v = b & 0xFF;
          hex[index++] = HEX_CHAR_TABLE[v >>> 4];
          hex[index++] = HEX_CHAR_TABLE[v & 0xF];
       }
       String result=null;
       try {
          result=new String(hex, "ASCII");
       }
       catch(UnsupportedEncodingException e) {
          //Can't happen but dump it anyway in case...
          e.printStackTrace();
       }
       return result;
    }
}