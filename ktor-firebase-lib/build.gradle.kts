/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin library project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.12.1/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
  // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
  alias(libs.plugins.kotlin.jvm)
  // Apply the java-library plugin for API and implementation separation.
  `java-library`
}

group = "com.lockerfish"
version = "0.1.0"

repositories {
  // Use Maven Central for resolving dependencies.
  mavenCentral()
}

dependencies {
  implementation(libs.firebase.admin)
  implementation(libs.ktor.server.auth)

  testImplementation(libs.ktor.server.test.host)
  testImplementation(libs.kotlin.test.junit)
  testImplementation(libs.mockk.test)
  testImplementation(libs.logback.classic)
}

// Apply a specific Java toolchain to ease working on different environments.
java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
  withJavadocJar()
  withSourcesJar()
}

// create a fat jar
//
//tasks.withType<Jar>() {
//  configurations["compileClasspath"].forEach { file: File ->
//    from(zipTree(file.absoluteFile))
//  }
//  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//  exclude(
//    "META-INF/*.SF",
//    "META-INF/*.DSA",
//    "META-INF/*.RSA",
//  )
//}
