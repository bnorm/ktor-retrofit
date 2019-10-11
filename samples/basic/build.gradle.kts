import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
  kotlin("jvm")
}

repositories {
  jcenter()
  maven { setUrl("https://dl.bintray.com/kotlin/ktor") }
}

dependencies {
  implementation(project(":ktor-retrofit"))

  implementation(kotlin("stdlib-jdk8"))
  implementation("ch.qos.logback:logback-classic:1.2.3")
  implementation("io.ktor:ktor-server-netty:1.2.5")
  implementation("io.ktor:ktor-jackson:1.2.5")
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}
