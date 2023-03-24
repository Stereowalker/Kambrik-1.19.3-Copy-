import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {

    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.6.0"
    base
    `maven-publish`
    signing
    id("architectury-plugin") version "3.4.143"
    id("dev.architectury.loom") version "1.0.302" apply false

    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
}

object Versions {
    val Mod = "0.1"
    val MC = "1.19.4"
    val Yarn = "1.19.4+build.1"
}

// Set the Minecraft version for Architectury.
architectury {
    minecraft = Versions.MC
}

// Set up basic Maven artifact metadata, including the project version
// and archive names.
group = "io.ejekta.kambrik"
// Set the project version to be <mod version>+<Minecraft version> so the MC version is semver build metadata.
// The "mod-version" and "minecraft-version" properties are read from gradle.properties.
version = "${Versions.Mod}+${Versions.MC}"
base.archivesName.set("Kambrik")

// Do the shared setup for the Minecraft subprojects.
subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "architectury-plugin")

    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    group = rootProject.group
    version = rootProject.version
    base.archivesName.set(rootProject.base.archivesName)

    dependencies {
        // Note that the configuration name has to be in quotes (a string) since Loom isn't applied to the root project,
        // and so the Kotlin accessor method for it isn't generated for this file.
        "minecraft"("net.minecraft:minecraft:${Versions.MC}")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
        "mappings"("net.fabricmc:yarn:${Versions.Yarn}:v2")
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.release.set(17)
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "17"
            kotlinOptions.freeCompilerArgs = listOf("-Xlambdas=indy", "-Xjvm-default=all",)
        }
    }
}

// Set up "platform" subprojects (non-common subprojects).
subprojects {
    if (path != ":common") {

        // Apply the shadow plugin which lets us include contents
        // of any libraries in our mod jars. Architectury uses it
        // for bundling the common mod code in the platform jars.
        apply(plugin = "com.github.johnrengelman.shadow")

        // Define the "bundle" configuration which will be included in the shadow jar.
        val bundle by configurations.creating {
            // This configuration is only meant to be resolved to its files but not published in
            // any way, so we set canBeConsumed = false and canBeResolved = true.
            // See https://docs.gradle.org/current/userguide/declaring_dependencies.html#sec:resolvable-consumable-configs.
            isCanBeConsumed = false
            isCanBeResolved = true
        }

        tasks.withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.release.set(17)
        }

        tasks {
            "shadowJar"(ShadowJar::class) {
                archiveClassifier.set("dev-shadow")
                if (path == ":forge") { exclude("fabric.mod.json") }
                exclude("architectury.common.json")
                configurations = listOf(bundle)
            }
            "remapJar"(RemapJarTask::class) {
                injectAccessWidener.set(true)
                inputFile.set(named<ShadowJar>("shadowJar").flatMap { it.archiveFile })
                dependsOn("shadowJar")
                archiveClassifier.set(project.name)
            }
            "jar"(Jar::class) { archiveClassifier.set("dev") }
        }
    }
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
}
repositories {
    mavenCentral()
}
kotlin {
    jvmToolchain(8)
}