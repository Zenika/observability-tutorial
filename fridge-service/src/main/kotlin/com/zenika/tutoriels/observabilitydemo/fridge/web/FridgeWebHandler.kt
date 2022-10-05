package com.zenika.tutoriels.observabilitydemo.fridge.web

import com.zenika.tutoriels.observabilitydemo.api.contracts.FridgeErrorBody
import com.zenika.tutoriels.observabilitydemo.fridge.business.FoodStuff
import com.zenika.tutoriels.observabilitydemo.fridge.business.Fridge
import com.zenika.tutoriels.observabilitydemo.fridge.business.NoMoreFoodStuffException
import com.zenika.tutoriels.observabilitydemo.fridge.business.UnknownFoodStuffException
import com.zenika.tutoriels.observabilitydemo.logger.utils.ServiceLoggerAutoConfiguration.Companion.withRequestInfoInMdcForLog
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

class FridgeWebHandler(private val fridge: Fridge) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(FridgeWebHandler::class.java)
    }

    fun listMeats(request: ServerRequest): Mono<ServerResponse> {
        return list(request,
                { fridge.listAvailable(FoodStuff.meat) },
                { LOGGER.info("listMeats - init") },
                { response: ServerResponse -> LOGGER.info("listMeats - end with status {}", response.statusCode()) })
    }

    fun listCheeses(request: ServerRequest): Mono<ServerResponse> {
        return list(request,
                { fridge.listAvailable(FoodStuff.cheese) },
                { LOGGER.info("listCheeses - init") },
                { response: ServerResponse -> LOGGER.info("listCheeses - end with status {}", response.statusCode()) })
    }

    fun orderCheeseByKind(request: ServerRequest): Mono<ServerResponse> {
        return orderByKind(request, { cheese: String? -> fridge.removeCheeseFromFridge(cheese!!) },
                { kind: String? -> LOGGER.info("orderCheeseByKind - {} - init", kind) },
                { response: ServerResponse ->
                    LOGGER.info("orderCheeseByKind - {} - end with status {}",
                            request.pathVariable("kind"), response.statusCode())
                })
    }

    fun orderMeatByKind(request: ServerRequest): Mono<ServerResponse> {
        return orderByKind(request, { meat: String? -> fridge.removeMeatFromFridge(meat!!) },
                { kind: String? -> LOGGER.info("orderMeatByKind - {} - init", kind) },
                { response: ServerResponse ->
                    LOGGER.info("orderMeatByKind - {} - end with status {}",
                            request.pathVariable("kind"), response.statusCode())
                })
    }

    private fun list(request: ServerRequest,
                     listFunction: (ServerRequest) -> Mono<List<String>>,
                     initLogger: (ServerRequest) -> Unit,
                     endLogger: (ServerResponse) -> Unit): Mono<ServerResponse> {
        return Mono.just(request)
                .doOnEach(withRequestInfoInMdcForLog(initLogger))
                .flatMap(listFunction)
                .flatMap { result: List<String> ->
                    ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(result))
                }
                .doOnEach(withRequestInfoInMdcForLog(endLogger))
    }

    private fun orderByKind(request: ServerRequest,
                            removalFunction: (String) -> Mono<Void>,
                            initLogger: (String) -> Unit, endLogger: (ServerResponse) -> Unit): Mono<ServerResponse> {
        return Mono.just(request.pathVariable("kind"))
                .doOnEach(withRequestInfoInMdcForLog(initLogger))
                .flatMap(removalFunction)
                .then(ServerResponse.noContent().build())
                .onErrorResume(NoMoreFoodStuffException::class.java) { error: NoMoreFoodStuffException -> handleNoMoreFoodStuff(error) }
                .onErrorResume(UnknownFoodStuffException::class.java) { error: UnknownFoodStuffException -> handleUnknownFoodStuff(error) }
                .doOnEach(withRequestInfoInMdcForLog(endLogger))
    }

    private fun handleUnknownFoodStuff(error: UnknownFoodStuffException): Mono<ServerResponse> {
        return ServerResponse.status(HttpStatus.NOT_FOUND)
                .body(BodyInserters.fromValue(FridgeErrorBody("Unknown food", listOf(error.foodStuff.name, error.kind))))
    }

    private fun handleNoMoreFoodStuff(error: NoMoreFoodStuffException): Mono<ServerResponse> {
        return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(BodyInserters.fromValue(FridgeErrorBody("Stock empty", listOf(error.foodStuff.name, error.kind))))
    }


}