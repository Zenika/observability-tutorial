package com.zenika.tutoriels.observabilitydemo.burgermaker.service

import com.zenika.tutoriels.observabilitydemo.burgermaker.clients.CheeseServiceClient
import com.zenika.tutoriels.observabilitydemo.burgermaker.clients.MeatServiceClient
import com.zenika.tutoriels.observabilitydemo.logger.utils.RequestInfo
import com.zenika.tutoriels.observabilitydemo.logger.utils.RequestInfoContext
import com.zenika.tutoriels.observabilitydemo.logger.utils.ServiceLoggerAutoConfiguration.Companion.withRequestInfoInMdc
import reactor.core.publisher.Mono

import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.cloud.sleuth.Tracer
import org.springframework.cloud.sleuth.instrument.kotlin.asContextElement
import java.lang.Thread.sleep


class BurgerMaker(private val tracer: Tracer?, private val  meatService: MeatServiceClient, private val cheeseService: CheeseServiceClient) {

    companion object{
        private val LOGGER: Logger = LoggerFactory.getLogger(BurgerMaker::class.java)
    }
    fun makeABurger(meat: String, cheese: String): Mono<Void> {
        val function : (Any?) -> Mono<Void> = {
            runBlocking {
                val requestInfoContext = RequestInfoContext(RequestInfo(MDC.get("request.method"), MDC.get("request.uri")))
                launch(tracer?.let { it.asContextElement()
                        .plus(requestInfoContext) }?: requestInfoContext) {
                    LOGGER.debug("Let's sleep in coroutine")
                    sleep(1000)
                    LOGGER.debug("Coroutine awaken")
                }
                LOGGER.debug("Out of coroutine")
                return@runBlocking Mono.`when`(this@BurgerMaker.meatService.orderMeatByKind(meat),
                        this@BurgerMaker.cheeseService.orderCheeseByKind(cheese))
            }
        }
        return withRequestInfoInMdc(function).invoke(null);
    }
}