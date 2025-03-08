plugins {
  // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
  alias(libs.plugins.kotlin.jvm)
  // Apply the java-library plugin for API and implementation separation.
  `java-library`
  `maven-publish`
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

// TODO: remove this if you don't need to create a fat jar
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

// TODO: publish to maven
//publishing {
//    publications {
//        create<MavenPublication>("mavenJava") {
//            groupId = "com.lockerfish"
//            artifactId = "ktor-firebase"
//            version = "0.1.0"
//
//            from(components["kotlin"]) // or "kotlin" if it's a Kotlin library
//
//            pom {
//                name = "Ktor Firebase Provider"
//                description = "Authentication Provider for Firebase Auth"
//                url = "https://github.com/lockerfish/ktor-firebase"
//                licenses {
//                    license {
//                        name = "The Apache License, Version 2.0"
//                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
//                    }
//                }
//                developers {
//                    developer {
//                        id = "lockerfish"
//                        name = "Hendrix Tavarez"
//                        email = "hendrix@lockerfish.com"
//                    }
//                }
//            }
//        }
//    }
//}