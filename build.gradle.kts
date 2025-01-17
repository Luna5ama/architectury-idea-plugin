import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.intellij") version "1.7.0"
    kotlin("jvm") version "1.7.10"
    id("org.jmailen.kotlinter") version "3.11.1"
}

// Corresponds to IDEA 2022.1, see KotlinVersion class in ideaIC/3rd-party-rt.jar
val kotlinVersion = "1.6.10"
val kotlinLanguageVersion = kotlinVersion.substringBeforeLast('.')

group = "dev.architectury"
version = "1.6.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib", kotlinVersion))
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2022.1")
    plugins.set(listOf("java", "Kotlin"))
}

tasks {
    jar {
        from("COPYING", "COPYING.LESSER")
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
        kotlinOptions {
            apiVersion = kotlinLanguageVersion
            languageVersion = kotlinLanguageVersion
        }
    }

    patchPluginXml {
        sinceBuild.set("221")
        untilBuild.set("222.*")
    }
}

kotlinter {
    disabledRules = arrayOf(
        "filename",
        "argument-list-wrapping",
        "trailing-comma",
    )
}
