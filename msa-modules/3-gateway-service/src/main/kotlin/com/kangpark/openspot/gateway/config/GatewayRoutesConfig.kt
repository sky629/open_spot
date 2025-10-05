package com.kangpark.openspot.gateway.config

import com.kangpark.openspot.gateway.filter.JwtToHeaderGatewayFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http
import org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.RouterFunction
import org.springframework.web.servlet.function.ServerResponse

/**
 * Spring Cloud Gateway MVC 라우트 설정
 *
 * YAML 설정 대신 프로그래매틱 방식으로 라우트를 구성하여
 * 커스텀 필터(JwtToHeaderGatewayFilter)를 적용합니다.
 */
@Configuration
open class GatewayRoutesConfig(
    private val jwtToHeaderFilter: JwtToHeaderGatewayFilter
) {

    @Value("\${AUTH_SERVICE_HOST:localhost}")
    private lateinit var authServiceHost: String

    @Value("\${AUTH_SERVICE_PORT:8081}")
    private lateinit var authServicePort: String

    @Value("\${LOCATION_SERVICE_HOST:localhost}")
    private lateinit var locationServiceHost: String

    @Value("\${LOCATION_SERVICE_PORT:8082}")
    private lateinit var locationServicePort: String

    @Bean
    open fun gatewayRouterFunction(): RouterFunction<ServerResponse> {
        val authServiceUri = "http://$authServiceHost:$authServicePort"
        val locationServiceUri = "http://$locationServiceHost:$locationServicePort"

        return route("auth-service")
            .route(
                path("/api/v1/auth/**")
                    .or(path("/api/v1/users/**"))
                    .or(path("/oauth2/**"))
                    .or(path("/login/oauth2/**")),
                http(authServiceUri)
            )
            .filter(jwtToHeaderFilter)
            .build()
            .and(
                route("location-service")
                    .route(
                        path("/api/v1/locations/**")
                            .or(path("/api/v1/categories/**")),
                        http(locationServiceUri)
                    )
                    .filter(jwtToHeaderFilter)
                    .build()
            )
    }
}
