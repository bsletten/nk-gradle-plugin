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

import java.io.InputStream
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.Signature
import java.security.cert.Certificate
import java.security.cert.CertificateFactory

class SigningHelper {
	int KB = 1024
	int MB = 1024*KB
	
	def hashHelper = new HashHelper()
	
	String signFileSignature(File f, String keyId, String password, File keystore) {
		String retValue = null
		
		try {
			def ks = KeyStore.getInstance(KeyStore.getDefaultType())		
			ks.load(new FileInputStream(keystore), password.toCharArray())
			def pw = new KeyStore.PasswordProtection(password.toCharArray())
			def keyEntry = ks.getEntry(keyId, pw)
			def pk = keyEntry.getPrivateKey()
			def signature = Signature.getInstance("SHA1withRSA")
			signature.initSign(pk)
			f.eachByte(MB) { byte[] buf, int bytesRead ->
				signature.update(buf, 0, bytesRead)
			}
			
			retValue = hashHelper.toHexString(signature.sign())
		} catch(Throwable t) {
			t.printStackTrace()
		}
		
		return retValue
	}
	
	// This method is only used for signing NetKernel modules because of a legacy
	// bug in terms of how they signed things.
	String signModule(InputStream bis, String keyId, String password, File keystore) {
		String retValue = null
		
		try {
			def ks = KeyStore.getInstance(KeyStore.getDefaultType())		
			ks.load(new FileInputStream(keystore), password.toCharArray())
			def pw = new KeyStore.PasswordProtection(password.toCharArray())
			def keyEntry = ks.getEntry(keyId, pw)
			def pk = keyEntry.getPrivateKey()
			def signature = Signature.getInstance("SHA1withRSA")
			signature.initSign(pk)
			
			// NOTE: If you look closely, this is not correct, but it is 
			// how legacy signing was done w/ NetKernel, so we need the ability
			// to replicate it to be compatible with its code signing checks.
		    int a=0;
        	while((a=bis.available())>0)
        	{	byte[] b=new byte[256];
        		bis.read(b);
        		signature.update(b);
        	}
        		
         retValue = hashHelper.toHexString(signature.sign())

		} catch(Throwable t) {
			t.printStackTrace()
		}

		return retValue
	}
	
	boolean verifySignature(File f, String sigString, String keyId, String password, File keystore) {
		boolean retValue = false
		
		try {
			def ks = KeyStore.getInstance(KeyStore.getDefaultType())		
			ks.load(new FileInputStream(keystore), password.toCharArray())
			def pw = new KeyStore.PasswordProtection(password.toCharArray())
			def cert = ks.getCertificate(keyId)
			
			byte [] sig = new BigInteger(sigString,16).toByteArray()

			def signature = Signature.getInstance("SHA1withRSA")
			signature.initVerify(cert)
			
			f.eachByte(MB) { byte[] buf, int bytesRead ->
				signature.update(buf, 0, bytesRead)
			}
			
			retValue = signature.verify(sig)
		} catch(Throwable t) {
			t.printStackTrace()
		}		
		
		return retValue
	}
	
	String extractCert(File keystore, String keyId, String password) {
    	String retValue = false
	
    	try {
    		def ks = KeyStore.getInstance(KeyStore.getDefaultType())		
    		ks.load(new FileInputStream(keystore), password.toCharArray())
    		def pw = new KeyStore.PasswordProtection(password.toCharArray())
    		def cert = ks.getCertificate(keyId)
    		retValue = cert.getEncoded().encodeBase64().toString()
    	} catch(Throwable t) {
    		t.printStackTrace()
    	}		
	
    	return retValue	
	}
}