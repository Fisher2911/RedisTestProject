plugins {
    java
}

group = "io.github.fisher2911"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation("redis.clients:jedis:4.4.3")
    implementation("it.ozimov:embedded-redis:0.7.3")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}