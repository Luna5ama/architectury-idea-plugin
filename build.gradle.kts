plugins {
    id("org.jetbrains.intellij") version "1.10.0"
    kotlin("jvm") version "1.7.0"
    id("org.jmailen.kotlinter") version "3.11.1"
}

// Corresponds to IDEA 2022.1, see KotlinVersion class in ideaIC/3rd-party-rt.jar
val kotlinVersion = "1.7.0"
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
    version.set("2022.3")
    plugins.set(listOf("java", "Kotlin"))
}

tasks {
    jar {
        from("COPYING", "COPYING.LESSER")
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
            apiVersion = kotlinLanguageVersion
            languageVersion = kotlinLanguageVersion
        }
    }

    patchPluginXml {
        sinceBuild.set("223")
        untilBuild.set("223.*")
    }
}

kotlinter {
    disabledRules = arrayOf(
        "filename",
        "argument-list-wrapping",
        "trailing-comma",
    )
}
