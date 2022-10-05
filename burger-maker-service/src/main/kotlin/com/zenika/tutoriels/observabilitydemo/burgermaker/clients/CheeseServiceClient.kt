package com.zenika.tutoriels.observabilitydemo.burgermaker.clients

import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

class CheeseServiceClient(serviceUrl: String, builder: WebClient.Builder) : AbstractFridgeClient(builder, "${serviceUrl}/cheeses") {
    fun orderCheeseByKind(kind: String): Mono<Void> {
        return delete(kind)
    }
}