import org.gradle.api.tasks.testing.Test
import org.gradle.plugin.compatibility.compatibility
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.autonomousapps.testkit)
    alias(libs.plugins.plugin.publish)
}

group = "io.github.5peak2me.plugin.gradle"
version = "0.0.6"

// This matches the JDK used to build the project, and is not related to what is running on device.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        freeCompilerArgs.add("-Xcontext-parameters")
    }
    explicitApi()
}

fun Provider<PluginDependency>.toDep() = map {
    dependencyFactory.create(it.pluginId, "${it.pluginId}.gradle.plugin", it.version.toString())
}

val pluginTestRuntime by configurations.creating

dependencies {
    compileOnly(libs.plugins.android.application.toDep())
    compileOnly(libs.plugins.kotlin.android.toDep())
    implementation(libs.picnic) {
        exclude("org.jetbrains.kotlin")
    }
    pluginTestRuntime(libs.plugins.android.application.toDep())
    testImplementation(libs.plugins.android.application.toDep())
    testImplementation(gradleTestKit())
    testImplementation(kotlin("test"))

    functionalTestImplementation(libs.plugins.android.application.toDep())
    functionalTestImplementation(gradleTestKit())
    functionalTestImplementation(kotlin("test-junit5"))
}

gradlePlugin {
    website.set("https://daijinlin.com/elf-16k-alignment")
    vcsUrl.set("https://github.com/5peak2me/elf-16k-alignment")

    plugins {
        create("elf-16k-alignment") {
            id = "io.github.5peak2me.gradle.elf-16k-alignment"
            implementationClass = "ElfAlignmentPlugin"

            displayName = "elf-16k-alignment"
            description = "elf-16k-alignment is a Gradle plugin designed for Android developers to detect whether native libraries (JNI .so files) in project dependencies (AARs) comply with the 16KB page alignment requirement."
            tags.set(listOf("android gradle plugin elf-16k-alignment"))

            compatibility {
                features {
                    configurationCache = true
                }
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
    if (name == "functionalTest") {
        useJUnitPlatform()
    }
}

tasks {
    pluginUnderTestMetadata {
        pluginClasspath.from(pluginTestRuntime)
    }
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}
