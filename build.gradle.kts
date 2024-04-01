plugins {
    id("java")
}

group = "top.tsstudio"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("io.obs-websocket.community:client:2.0.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.apache.logging.log4j:log4j-api:2.23.1")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName.set("TallyServer")
    archiveVersion.set("1.0.0")
    manifest {
        attributes["Main-Class"] = "top.tsstudio.Main"
    }
}