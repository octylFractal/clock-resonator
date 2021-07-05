plugins {
    java
    application
    id("com.google.osdetector") version "1.7.0"
}
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.eclipse.jgit:org.eclipse.jgit:5.12.0.202106070339-r")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(16))
}

application {
    applicationName = "clock-resonator"
    mainModule.set("net.octyl.clockresonator.app")
}

repositories {
    maven {
        name = "Sonatype Snapshots"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    mavenLocal {
        content {
            includeGroup("com.cronutils")
        }
    }
}

configurations.all {
    resolutionStrategy {
        dependencySubstitution {
            val injectApi = libs.jakarta.injectApi.get()
            substitute(module("javax.inject:javax.inject:1"))
                .using(
                    module(
                        "${injectApi.module.group}:${injectApi.module.name}:${injectApi.versionConstraint.requiredVersion}"
                    )
                )
        }
    }
    // This is just noise for us, we don't need it
    exclude(group = "com.google.guava", module = "listenablefuture")
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.checkerframework.qual)

    annotationProcessor(libs.dagger.compiler)

    implementation(platform(libs.log4j.bom))
    implementation(libs.log4j.api)
    runtimeOnly(libs.log4j.core)
    runtimeOnly(libs.log4j.slf4jImpl)

    implementation(libs.dagger.core)
    implementation(libs.guava)

    implementation(libs.directories)

    implementation(platform(libs.jackson.bom))
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.datatype.jdk8)

    implementation(platform(libs.reactor.bom))
    implementation(libs.reactor.core)

    implementation(libs.cronutils)

    for (lib in listOf(libs.javafx.base, libs.javafx.controls, libs.javafx.graphics)) {
        implementation(lib)
        implementation(variantOf(lib) {
            classifier(
                when (osdetector.os) {
                    "osx" -> "mac"
                    "windows" -> "win"
                    else -> "linux"
                }
            )
        })
    }

    implementation(libs.controlsfx)

    implementation(platform(libs.ikonli.bom))
    implementation(libs.ikonli.javafx)
    implementation(libs.ikonli.fontawesome5)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    testImplementation(libs.truth) {
        exclude(group = "junit")
    }
}

tasks.compileJava {
    options.javaModuleMainClass.set("net.octyl.clockresonator.app.ClockResonator")
    val git = org.eclipse.jgit.api.Git.open(project.rootDir)
    val gitHash = git.repository.newObjectReader().abbreviate(
        git.repository.resolve("HEAD")
    ).name()
    var version = "${project.version}+$gitHash"
    if (!git.status().call().isClean) {
        version += "-dirty"
    }
    options.javaModuleVersion.set(version)
}

tasks.test {
    useJUnitPlatform()
}
