import org.gradle.internal.hash.HashUtil

plugins {
    base
    id("org.sonarqube") version "3.4.0.2513"
}

repositories {
    mavenCentral()
}

tasks {

    val samplesWrappers by registering {
        doLast {
            val wrapperFiles = wrapper.get().run {
                listOf(scriptFile, batchScript, jarFile, propertiesFile).associateBy { it.name }
            }
            val hashes = wrapperFiles.mapValues { HashUtil.sha256(it.value) }
            file("samples").walk().filter { it.isFile && it.name in wrapperFiles }.forEach { sampleWrapperFile ->
                wrapperFiles.getValue(sampleWrapperFile.name).let { wrapperFile ->
                    if (HashUtil.sha256(sampleWrapperFile) != hashes.getValue(sampleWrapperFile.name)) {
                        logger.lifecycle("Updating ${sampleWrapperFile.relativeTo(rootDir)}")
                        wrapperFile.copyTo(sampleWrapperFile, overwrite = true)
                    }
                }
            }
        }
    }

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        finalizedBy(samplesWrappers)
    }
}
