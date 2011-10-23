package net.bosatsu.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin

class NetKernelPlugin implements Plugin<Project> { 
  def void apply(Project project) { 
    println "NetKernel Module Plugin"
  }
}