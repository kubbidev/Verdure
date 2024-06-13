import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("java")
    id("java-library")
    alias(libs.plugins.shadow)
    alias(libs.plugins.loom)
}

base {
    archivesName.set("verdure")
}

// make fullVersion accessible in subprojects
project.extra["fullVersion"] = "1.0.0"
project.extra["apiVersion"] = "1.0"

// project settings
group = "me.kubbidev"
version = "${project.extra["apiVersion"]}-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// repositories and dependencies manager
java {
    withSourcesJar()
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21")
    mappings("net.fabricmc:yarn:1.21+build.1:v2")
    modImplementation("net.fabricmc:fabric-loader:0.15.11")

    val apiModules = listOf(
        "fabric-api"
    )

    apiModules.forEach {
        modImplementation(fabricApi.module(it, "0.100.1+1.21"))
    }

    // lombok dependencies & annotation processor
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
}

// building task operations
tasks.processResources {
    inputs.property("version", project.extra["fullVersion"])
    filesMatching("**/fabric.mod.json") {
        expand("version" to project.extra["fullVersion"])
    }
}

tasks.shadowJar {
    archiveFileName = "verdurefabric-${project.extra["fullVersion"]}-dev.jar"

    dependencies {
        include(dependency("me.kubbidev.verdure:.*"))
        exclude(dependency("net.fabricmc:.*"))
    }

    // check if the assets/verdure directory is not empty before including it
    val assetsDir = project.file("src/main/resources/assets/verdure")
    if (assetsDir.exists() && assetsDir.isDirectory && assetsDir.listFiles()?.isNotEmpty() == true) {
        from(assetsDir) {
            include("**/*")
            into("assets/verdure")
        }
    }

    // we don't want to include the mappings in the jar do we?
    exclude("/mappings/*")
}

val remappedShadowJar by tasks.registering(RemapJarTask::class) {
    dependsOn(tasks.shadowJar)

    inputFile = tasks.shadowJar.flatMap {
        it.archiveFile
    }
    addNestedDependencies = true;
    archiveFileName = "Verdure-Fabric-${project.extra["fullVersion"]}.jar"
}

tasks.assemble {
    dependsOn(remappedShadowJar)
}

artifacts {
    archives(remappedShadowJar)
    archives(tasks.shadowJar)
}
