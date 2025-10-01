dependencies {
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.data:spring-data-commons")

    // Jackson for JSON processing
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Utility libraries
    api("org.apache.commons:commons-lang3")

    // UUIDv7 (Time-based sortable UUID)
    api("com.github.f4b6a3:uuid-creator:5.3.7")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}