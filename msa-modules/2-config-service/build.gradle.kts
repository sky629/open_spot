extra["springCloudVersion"] = "2025.0.0"

dependencies {
    implementation(project(":common-core"))
    implementation("org.springframework.cloud:spring-cloud-config-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.12")
    
    // DevTools for auto-reload (development only)
    developmentOnly("org.springframework.boot:spring-boot-devtools")

}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}