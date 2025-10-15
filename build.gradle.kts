import org.gradle.api.plugins.JavaPluginExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.5.5" apply false
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "1.9.25" apply false
    kotlin("plugin.spring") version "1.9.25" apply false
    kotlin("plugin.jpa") version "1.9.25" apply false
    id("com.google.devtools.ksp") version "1.9.25-1.0.20" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

allprojects {
    group = "com.kangpark.openspot"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    plugins.apply("java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    configurations.named("compileOnly") {
        extendsFrom(configurations.getByName("annotationProcessor"))
    }

    dependencies {
        add("implementation", "org.springframework.boot:spring-boot-starter")
        add("implementation", "org.jetbrains.kotlin:kotlin-reflect")
        add("implementation", "com.fasterxml.jackson.module:jackson-module-kotlin")
        add("annotationProcessor", "org.springframework.boot:spring-boot-configuration-processor")
        add("testImplementation", "org.springframework.boot:spring-boot-starter-test")
        add("testImplementation", "org.junit.jupiter:junit-jupiter")
        add("testImplementation", "org.assertj:assertj-core")
        add("testImplementation", "io.mockk:mockk:1.13.10")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            jvmTarget = "21"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

// Exclude common modules from bootJar task
configure(listOf(project(":common-core"), project(":common-web"))) {
    tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
        enabled = false
    }

    tasks.named<Jar>("jar") {
        enabled = true
        archiveClassifier = ""
    }
}
