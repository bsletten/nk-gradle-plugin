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

import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

class HashHelperTest {
    def HashHelper helper
	def f

    @Before
    void setUp()
    {
        URL url = this.getClass().getResource('/test.jar')
		f = new File(url.getFile())
        helper = new HashHelper()
    }

	@Test
	void testSha1() {
		def result = helper.hashFile("SHA-1", f)
	}
	
	@Test
	void testSha256() {
		def result = helper.hashFile("SHA-256", f)
	}
	
	@Test
	void testSha512() {
		def result = helper.hashFile("SHA-512", f)
	}
	
	@Test
	void testMD5() {
		def result = helper.hashFile("MD5", f)
	}
}