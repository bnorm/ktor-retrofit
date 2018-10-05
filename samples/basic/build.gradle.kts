import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
  kotlin("jvm") version "1.2.71"
}

repositories {
  jcenter()
  maven { setUrl("https://dl.bintray.com/kotlin/ktor") }
}

dependencies {
  implementation(project(":ktor-retrofit"))

  implementation(kotlin("stdlib-jdk8"))
  implementation("ch.qos.logback:logback-classic:1.2.3")
  implementation("io.ktor:ktor-server-netty:0.9.5")
  implementation("io.ktor:ktor-jackson:0.9.5")
  implementation("com.squareup.retrofit2:retrofit:2.4.0")
}

kotlin {
  experimental.coroutines = Coroutines.ENABLE
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}
