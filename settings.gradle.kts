rootProject.name = "open_spot"

include(
    "common-core",
    "common-web",
    "config-service",
    "gateway-service",
    "auth-service",
    "location-service",
    "notification-service",
)

project(":common-core").projectDir = file("msa-modules/0-common-core")
project(":common-web").projectDir = file("msa-modules/1-common-web")
project(":config-service").projectDir = file("msa-modules/2-config-service")
project(":gateway-service").projectDir = file("msa-modules/3-gateway-service")
project(":auth-service").projectDir = file("msa-modules/4-auth-service")
project(":location-service").projectDir = file("msa-modules/5-location-service")
project(":notification-service").projectDir = file("msa-modules/6-notification-service")
