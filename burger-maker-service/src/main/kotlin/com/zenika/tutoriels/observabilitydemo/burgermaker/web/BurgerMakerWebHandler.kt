package com.zenika.tutoriels.observabilitydemo.burgermaker.web

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.zenika.tutoriels.observabilitydemo.api.contracts.FridgeErrorBody
import com.zenika.tutoriels.observabilitydemo.burgermaker.clients.NoMoreInStockException
import com.zenika.tutoriels.observabilitydemo.burgermaker.clients.UnknownFridgeException
import com.zenika.tutoriels.observabilitydemo.burgermaker.clients.UnknownStuffException
import com.zenika.tutoriels.observabilitydemo.burgermaker.service.BurgerMaker
import com.zenika.tutoriels.observabilitydemo.logger.utils.ServiceLoggerAutoConfiguration.Companion.withRequestInfoInMdcForLog
import io.micrometer.core.instrument.Metrics
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono


data class BurgerOrder @JsonCreator constructor(@param:JsonProperty("meat", required = true) val meat: String,
                                                @param:JsonProperty("cheese", required = true) val cheese: String)

class IllegalOrderException : RuntimeException()

class BurgerMakerWebHandler(private val burgerMaker: BurgerMaker) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(BurgerMakerWebHandler::class.java)

    }

    fun makeABurger(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono<BurgerOrder>()
                .doOnEach(withRequestInfoInMdcForLog { LOGGER.info("makeABurger - order init") })
                .flatMap { order ->
                    if (order.meat.isEmpty() || order.cheese.isEmpty()) {
                        return@flatMap Mono.error(IllegalOrderException())
                    }
                    burgerMaker.makeABurger(order.meat, order.cheese)
                }
                .then(ServerResponse.noContent().build())
                .onErrorResume(NoMoreInStockException::class.java) { error: NoMoreInStockException -> handleNoMoreInStock(error) }
                .onErrorResume(UnknownStuffException::class.java) { error: UnknownStuffException -> handleUnknownStuff(error) }
                .onErrorResume(UnknownFridgeException::class.java) { error: UnknownFridgeException -> handleUnknownFridge(error) }
                .onErrorResume(IllegalOrderException::class.java) { ServerResponse.badRequest().build() }
                .doOnEach(withRequestInfoInMdcForLog { s -> LOGGER.info("makeABurger - order end with status code {}", s.statusCode()) })
    }

    private fun handleUnknownFridge(error: UnknownFridgeException): Mono<ServerResponse?> {
        Metrics.counter("application.fridge.unknown-error").increment()
        return ServerResponse.status(HttpStatus.BAD_REQUEST)
                .body(BodyInserters.fromValue(error.errorBody))
    }

    private fun handleUnknownStuff(error: UnknownStuffException): Mono<ServerResponse?> {
        return ServerResponse.status(HttpStatus.BAD_REQUEST)
                .body(BodyInserters.fromValue(error.errorBody))
    }

    private fun handleNoMoreInStock(error: NoMoreInStockException): Mono<ServerResponse?> {
        val errorBody: FridgeErrorBody = error.errorBody
        Metrics.counter("application.fridge.no-more-in-stocks",
                "foodStuff", errorBody.parameters.get(0),
                "kind", errorBody.parameters[1]).increment()
        return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(BodyInserters.fromValue(errorBody))
    }

}