package net.bosatsu.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class NKModuleBuilder extends org.gradle.api.DefaultTask { 
  def modules
  def moduleBuildDir = "${project.projectDir}/build/modules"

  NKModuleBuilder() {
	println modules
  }

  @org.gradle.api.tasks.TaskAction
  def buildModules() { 
    modules?.each { module ->
      buildModule(module)
    }
  }

  def buildModule(moduleFile) { 
	println moduleFile - '/module.xml'
	
 /*   ant.nkmodule(destdir: moduleBuildDir, 
                 modulefile: moduleFile) {
      zipfileset(dir: moduleFile - '/module.xml', prefix : '')
    } */
  }

}