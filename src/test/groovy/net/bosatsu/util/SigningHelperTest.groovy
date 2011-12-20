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

class SigningHelperTest {
	def SigningHelper helper
	File f
	File fs
	
	@Before
	void setUp()
	{
		helper = new SigningHelper()
		URL url = this.getClass().getResource('/test.jar')
		f = new File(url.getFile())
		url = this.getClass().getResource('/test.jks')
		fs = new File(url.getFile())
	}
	
	@Test
	void testSigning() {
		String signature = helper.signFileSignature(f, 'testkey', 't3stpassw0rd', fs)
		println signature
		assertNotNull "We got back a signature", signature
	}
	
	@Test
	void testVerify() {
		String signature = "64a6e4c7b33359dce0ebe9e0ad069eb0f0838c2f990cdc1df9218b95475ab7d545a5648945769e594b1cba546bb00ed5b0321c8ecd8fe77eec08d203166aee0684c5f1108a8c86e27507c6aa58fb03d226befc3bc31e47b4a741e9f3e866f635de7f95937da0cfb1a6058e8b77ac495220173449f95c4d6efc41f641344f567ef439cdfa0804427a5b62743186f9d2896a2536f1cd4527446f3238eb47a747b31ffc9e877a6cea32db3877baab0503dc2a0d7f4cb3edd8b963907ce4e574f51e853a13c975f64e0721805b592af5f52fe55d4b34899300f62ed3ef4b246a246355abbc961939f0f3e35aaede553e16d883f49eb855345c62afc08d05aa7fbe2e"
		boolean verification = helper.verifySignature(f, signature, 'testkey', 't3stpassw0rd', fs)
		assertTrue "Signature verified", verification
	}
}