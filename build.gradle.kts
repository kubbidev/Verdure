import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("java")
    id("java-library")
    alias(libs.plugins.shadow)
    alias(libs.plugins.loom)
}

// store the version as a variable,
// as we use it several times
val fullVersion = "1.0.0"

// project settings
group = "me.kubbidev"
version = "1.0-SNAPSHOT"

base {
    archivesName.set("verdure")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    // include source in when publishing
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

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// building task operations
tasks.processResources {
    inputs.property("version", fullVersion)
    filesMatching("**/fabric.mod.json") {
        expand("version" to fullVersion)
    }
}

tasks.shadowJar {
    archiveFileName = "verdurefabric-${fullVersion}-dev.jar"

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
    addNestedDependencies = true
    archiveFileName = "Verdure-Fabric-${fullVersion}.jar"
}

tasks.assemble {
    dependsOn(remappedShadowJar)
}

artifacts {
    archives(remappedShadowJar)
    archives(tasks.shadowJar)
}
