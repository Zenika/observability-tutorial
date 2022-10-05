package com.zenika.tutoriels.observabilitydemo.burgermaker.clients

import com.zenika.tutoriels.observabilitydemo.api.contracts.FridgeErrorBody
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

abstract class FridgeException protected constructor(val errorBody: FridgeErrorBody) : RuntimeException()

class NoMoreInStockException(errorBody: FridgeErrorBody) : FridgeException(errorBody)

class UnknownFridgeException(vararg messages: String) : FridgeException(FridgeErrorBody("error.unknown", listOf(*messages)))

class UnknownStuffException(errorBody: FridgeErrorBody) : FridgeException(errorBody)

open class AbstractFridgeClient(private val builder: WebClient.Builder, private val baseUrl: String) {
    protected fun list(): Flux<String> {
        return builder.baseUrl(baseUrl).build()
                .get()
                .exchangeToFlux { response: ClientResponse -> handleListResult(response) }
    }

    protected fun delete(kind: String): Mono<Void> {
        return builder.baseUrl("$baseUrl/$kind").build()
                .delete()
                .exchangeToMono { response: ClientResponse -> handleDeleteResult(response) }
    }

    private fun handleListResult(response: ClientResponse): Flux<String?> {
        val status = response.statusCode()
        return if (status.is2xxSuccessful) {
            response.bodyToFlux(String::class.java)
        } else {
            Flux.error(UnknownFridgeException())
        }
    }

    private fun handleDeleteResult(response: ClientResponse): Mono<Void?> {
        val status = response.statusCode()
        return if (status.is2xxSuccessful) {
            response.bodyToMono(Void::class.java)
        } else if (status == HttpStatus.NOT_FOUND) {
            response.bodyToMono(FridgeErrorBody::class.java)
                    .flatMap { errorBody: FridgeErrorBody -> Mono.error(UnknownStuffException(errorBody)) }
        } else if (status == HttpStatus.SERVICE_UNAVAILABLE) {
            response.bodyToMono(FridgeErrorBody::class.java)
                    .flatMap { errorBody: FridgeErrorBody -> Mono.error(NoMoreInStockException(errorBody)) }
        } else {
            Mono.error(UnknownFridgeException())
        }
    }

}