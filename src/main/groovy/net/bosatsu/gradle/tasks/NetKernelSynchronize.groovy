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

class NetKernelSynchronize extends DefaultTask {

   static def SLEEP_TIME = 10

   @org.gradle.api.tasks.TaskAction
   void synchronize() {
      println "Synchronizing local NetKernel instance.."
      project.appositeHelper.synchronize()
      println "Waiting for $SLEEP_TIME seconds to let NetKernel reindex.."
      sleep SLEEP_TIME * 1000
   }
}