import org.gradle.api.tasks.wrapper.Wrapper

plugins {
    kotlin("jvm") version "1.7.10"
    id("io.kotest.multiplatform") version "5.4.1"
    // 'com.adarshr.test-logger' old build used this
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.arrow-kt:arrow-core:1.1.3-alpha.43")
    implementation("io.arrow-kt:arrow-optics:1.1.3-alpha.43")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.arrow-kt:arrow-fx-coroutines:1.1.3-alpha.43")
    implementation("org.jline:jline:3.21.0")

    testImplementation("junit:junit:4.13.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.7.10")
}
// old build used this
//test {
//    testLogging {
//        outputs.upToDateWhen { false }
//    }
//}

tasks.named<Wrapper>("wrapper") {
    gradleVersion = "7.5.1"
}
