import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.nio.charset.StandardCharsets

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.gradle.plugin.idea-ext")
    id("net.fabricmc.fabric-loom")
    id("net.kyori.blossom")
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
    implementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    implementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")
    implementation("net.fabricmc:fabric-language-kotlin:${providers.gradleProperty("fabric_kotlin_version").get()}")
}

tasks.processResources {
    val version = version
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        expand("version" to version)
    }
}


fun getCurrentBranchName(): String {
    return runCatching {
        val pb = ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
            .directory(project.rootDir)
        val process = pb.start()
        process.waitFor()
        process.inputStream.readAllBytes().toString(StandardCharsets.UTF_8).trim()
    }.getOrDefault("")
}

fun getCommitId(): String {
    return runCatching {
        val pb = ProcessBuilder("git", "rev-parse", "HEAD")
            .directory(project.rootDir)
        val process = pb.start()
        process.waitFor()
        process.inputStream.readAllBytes().toString(StandardCharsets.UTF_8).trim()
    }.getOrDefault("")
}



tasks.withType<JavaCompile>().configureEach {
    options.release = 25
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_25
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("branch_name", getCurrentBranchName())
                property("commit_id", getCommitId())
            }
        }
    }
}