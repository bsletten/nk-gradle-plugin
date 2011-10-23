package net.bosatsu.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class NKModuleBuilder extends DefaultTask { 
  @TaskAction
  def buildModules() { 
    println "Building modules"
  }
}