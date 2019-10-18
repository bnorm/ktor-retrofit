buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath(kotlin("gradle-plugin", version = "1.3.50"))
    classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.10.0")
  }
}

plugins {
  id("org.jetbrains.dokka") version "0.10.0" apply false
  id("nebula.release") version "13.1.1"
}

val release = tasks.findByPath(":release")
release?.finalizedBy(project.getTasksByName("publish", true))

allprojects {
  group = "com.bnorm.ktor.retrofit"
}

subprojects {
  repositories {
    mavenCentral()
    jcenter()
    maven { setUrl("https://dl.bintray.com/kotlin/ktor") }
  }
}
