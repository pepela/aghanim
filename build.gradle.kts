plugins {
    kotlin("jvm") version "1.8.20"
    application
}

group = "com.pepela"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.skadistats:clarity:4.0.1")
    implementation("org.slf4j:slf4j-simple:2.0.7")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.pepela.minimap.MainKt")
}
