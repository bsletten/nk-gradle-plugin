package net.bosatsu.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin

class NetKernelPlugin implements Plugin<Project> { 
  void apply(Project t) { 
    t.task('nkmodule', type: NKModuleBuilder)
    t.task('nkgreeting', type: NKGreeting)
  }
}