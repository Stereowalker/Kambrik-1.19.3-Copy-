import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.6.0"
    `maven-publish`
    signing
}

architectury {
    // Set up Architectury for the common project.
    // This sets up the transformations (@ExpectPlatform etc.) we need for production environments.
    common(
            "fabric",
            "forge",
    )
}


publishing {
    publications {
        create<MavenPublication>("Kambrik") {
            groupId = "io.ejekta"
            artifactId = "kambrik-common"
            version = "123-SNAPSHOT.16"
            println(components.names)
            from(components.getByName("java"))
        }
    }
}

loom {
    accessWidenerPath.set(file("src/main/resources/kambrik.accesswidener"))
}

dependencies {
    // Add dependencies on the required Kotlin modules.
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    modImplementation("net.fabricmc:fabric-loader:0.14.17")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}
repositories {
    mavenCentral()
}
kotlin {
    jvmToolchain(17)
}

