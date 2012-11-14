package net.bosatsu.util.netkernel

import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.gradle.api.GradleException;



class AppositeHelper {

   HttpClient httpClient = new DefaultHttpClient()
   def baseUrl

   AppositeHelper(def netkernelbaseuri) {
      this.baseUrl = "$netkernelbaseuri/tools/apposite/unattended/v1"
   }


   def synchronize() {
      HttpGet httpGet = new HttpGet("$baseUrl/synchronize")
      HttpResponse response = httpClient.execute(httpGet)
      println response.entity.content.text

      if(response.statusLine.statusCode != 200) {
         throw new GradleException("Problem occured calling NetKernel to synchronize apposite repositories.")
      }
   }

   def isInstalled(String packageName) {
      isInstalled(packageName, null)
   }

   def isInstalled(String packageName, String packageVersion) {
      HttpGet httpGet = new HttpGet("$baseUrl/installed?match=$packageName")
      HttpResponse response = httpClient.execute(httpGet)

      if(response.statusLine.statusCode == 200) {
         def responseXml = new XmlSlurper().parse(response.entity.content)

         def row = responseXml.row.find { it.NAME.text() == packageName }

         if(packageVersion) {
            row?.INSTALLED.text() == "true" && row?.VP.text() == packageVersion
         } else {
            row?.INSTALLED.text() == "true"
         }
      } else {
         throw new GradleException("Problem occured calling NetKernel to check status of package $packageName")
      }
   }

   def install(String packageName, String packageVersion, int attempts, int interval) {
      performChangeAndWait("install", packageName, packageVersion, attempts, interval)
   }

   def update(String packageName, String packageVersion, int attempts, int interval) {
      performChangeAndWait("update", packageName, packageVersion, attempts, interval)
   }

   private def performChangeAndWait(String action, String packageName, String packageVersion, int attempts, int interval) {
      HttpGet httpGet = new HttpGet("$baseUrl/change?$action=$packageName")
      HttpResponse response = httpClient.execute(httpGet)
      println response.entity.content.text

      if(response.statusLine.statusCode == 200) {

         boolean installed = false

         while(!installed && attempts) {
            sleep(interval)
            if(isInstalled(packageName, packageVersion)) {
               println "Package [name: $packageName, version: $packageVersion] has been commissioned."
               return
            }
            attempts--
         }

         throw new GradleException("Package [name: $packageName, version: $packageVersion] could not be verified.")
      } else {
         throw new GradleException("Error occurred calling NetKernel to $action package $packageName")
      }
   }
}