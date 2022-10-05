package com.zenika.tutoriels.observabilitydemo.burgermaker.clients

import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

class MeatServiceClient(serviceUrl: String, builder: WebClient.Builder) : AbstractFridgeClient(builder, "${serviceUrl}/meats") {
    fun orderMeatByKind(kind: String): Mono<Void> {
        return delete(kind)
    }
}