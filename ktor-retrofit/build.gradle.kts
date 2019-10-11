import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
}

repositories {
  jcenter()
  maven { setUrl("https://dl.bintray.com/kotlin/ktor") }
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  api("io.ktor:ktor-server-core:1.2.5")
  api("com.squareup.retrofit2:retrofit:2.6.1")

  testImplementation("org.junit.jupiter:junit-jupiter-api:5.1.1")
  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.1.1")
  testImplementation("io.ktor:ktor-server-test-host:1.2.5")
  testImplementation("io.ktor:ktor-jackson:1.2.5")
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}
