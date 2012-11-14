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

package net.bosatsu.gradle.tasks

import net.bosatsu.util.netkernel.AppositeHelper

import org.gradle.api.DefaultTask

class NetKernelInstallOrUpdatePackage extends DefaultTask {

   def packageName
   def packageVersion

   @org.gradle.api.tasks.TaskAction
   void installOrUpdate() {
      if(project.appositeHelper.isInstalled(packageName)) {
         println "Package $packageName already installed.  Invoking update."
         project.appositeHelper.update(packageName, packageVersion, 10, 3000)
      } else {
         println "Package $packageName not found.  Invoking install."
         project.appositeHelper.install(packageName, packageVersion, 10, 3000)
      }
   }
}