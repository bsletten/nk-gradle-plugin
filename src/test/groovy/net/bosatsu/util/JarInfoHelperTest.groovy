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

class JarInfoHelperTest {
	def JarInfoHelper helper
	
	@Before
	void setUp()
	{
		helper = new JarInfoHelper()
	}
	
	@Test
	void testNullName() {
		try {
			helper.parseJarName(null)
			assert false, 'We should throw an exception on a non-jar name'
		} catch(all) {
			assert all in IllegalArgumentException
		}
	}
	
	@Test
	void testNotJar() {
		try {
			helper.parseJarName('dogfood')
			assert false, 'We should throw an exception on a non-jar name'
		} catch(all) {
			assert all in IllegalArgumentException
		}
	}
	
	@Test
	void testNoVersion() {
		def jarInfo = helper.parseJarName('dogfood.jar')
		assertNotNull "We got back a JarInfo object", jarInfo
		assertEquals jarInfo.base, 'dogfood'
		assertNull jarInfo.version
		assertNull jarInfo.classifier
	}
	
	@Test
	void testConventionalVersion() {
		def jarInfo = helper.parseJarName('dogfood-1.0.0.jar')
		assertNotNull "We got back a JarInfo object", jarInfo
		assertEquals jarInfo.base, 'dogfood'
		assertEquals jarInfo.version, '1.0.0'
		assertNull jarInfo.classifier
		
		jarInfo = helper.parseJarName('dogfood-1.0.jar')
		assertNotNull "We got back a JarInfo object", jarInfo
		assertEquals jarInfo.base, 'dogfood'
		assertEquals jarInfo.version, '1.0'
		assertNull jarInfo.classifier
		
		jarInfo = helper.parseJarName('dogfood-1.jar')
		assertNotNull "We got back a JarInfo object", jarInfo
		assertEquals jarInfo.base, 'dogfood'
		assertEquals jarInfo.version, '1'
		assertNull jarInfo.classifier	
	}
	
	@Test
	void testNKVersion() {
		def jarInfo = helper.parseJarName('urn.com.ten60.core.layer0-1.81.57.jar')
		assertNotNull "We got back a JarInfo object", jarInfo
		assertEquals jarInfo.base, 'urn.com.ten60.core.layer0'
		assertEquals jarInfo.version, '1.81.57'
		assertNull jarInfo.classifier
	}
	
	@Test
	void testClassifierVersion() {
		def jarInfo = helper.parseJarName('dogfood-1.0.0-beta1.jar')
		assertNotNull "We got back a JarInfo object", jarInfo
		assertEquals 'dogfood', jarInfo.base
		assertEquals '1.0.0', jarInfo.version
		assertEquals 'beta1', jarInfo.classifier
	
		jarInfo = helper.parseJarName('dogfood-1.0-beta1.jar')
		assertNotNull "We got back a JarInfo object", jarInfo
		assertEquals 'dogfood', jarInfo.base
		assertEquals '1.0', jarInfo.version
		assertEquals 'beta1', jarInfo.classifier
	
		jarInfo = helper.parseJarName('dogfood-1-beta1.jar')
		assertNotNull "We got back a JarInfo object", jarInfo
		assertEquals 'dogfood', jarInfo.base
		assertEquals '1', jarInfo.version
		assertEquals 'beta1', jarInfo.classifier
	}
	
	@Test
	void testComplexClassifierVersion() {
		def jarInfo = helper.parseJarName('dogfood-api-1.0.0-beta1.jar')
		assertNotNull "We got back a JarInfo object", jarInfo
		assertEquals 'dogfood-api', jarInfo.base
		assertEquals '1.0.0', jarInfo.version
		assertEquals 'beta1', jarInfo.classifier
	
		jarInfo = helper.parseJarName('dogfood-api-1.0-beta1.jar')
		assertNotNull "We got back a JarInfo object", jarInfo
		assertEquals 'dogfood-api', jarInfo.base
		assertEquals '1.0', jarInfo.version
		assertEquals 'beta1', jarInfo.classifier
	
		jarInfo = helper.parseJarName('dogfood-api-1-beta1.jar')
		assertNotNull "We got back a JarInfo object", jarInfo
		assertEquals 'dogfood-api', jarInfo.base
		assertEquals '1', jarInfo.version
		assertEquals 'beta1', jarInfo.classifier
	}
	
	@Test
	void testAWSClassifierVersion() {
		def jarInfo = helper.parseJarName('aws-java-sdk-1.2.5-javadoc.jar')
		assertNotNull "We got back a JarInfo object", jarInfo
		assertEquals 'aws-java-sdk', jarInfo.base
		assertEquals '1.2.5', jarInfo.version
		assertEquals 'javadoc', jarInfo.classifier
	}
}