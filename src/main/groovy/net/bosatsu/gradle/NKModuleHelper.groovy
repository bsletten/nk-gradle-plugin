package net.bosatsu.gradle

class NKModuleHelper {
	
	def getModuleInfo(def moduleFile) {
		def moduleInfo = new XmlSlurper().parse(moduleFile)
	}
	
	def getModuleArchiveName(def moduleFile) {
		def moduleInfo = getModuleInfo(moduleFile)
		
		def moduleName = moduleInfo.meta.identity.uri.text()
		def moduleVersion = moduleInfo.meta.identity.version.text()
		def fileName = moduleName.replaceAll(':', '.')
		
		"${fileName}-${moduleVersion}.jar"
	}
}