package com.zenika.tutoriels.observabilitydemo.burgermaker

import FridgeServiceClientConfiguration
import com.zenika.tutoriels.observabilitydemo.burgermaker.clients.CheeseServiceClient
import com.zenika.tutoriels.observabilitydemo.burgermaker.clients.MeatServiceClient
import com.zenika.tutoriels.observabilitydemo.burgermaker.service.BurgerMaker
import com.zenika.tutoriels.observabilitydemo.burgermaker.web.BurgerMakerWebHandler
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.cloud.sleuth.Tracer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.web.reactive.function.server.*

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(FridgeServiceClientConfiguration::class)
class BurgerMakerApplication {
    @Bean
    fun burgerMaker(tracer: Tracer?, meatService: MeatServiceClient, cheeseService: CheeseServiceClient): BurgerMaker {
        return BurgerMaker(tracer, meatService, cheeseService)
    }

    @Bean
    fun router(burgerMaker: BurgerMaker): RouterFunction<ServerResponse> {
        val webHandler = BurgerMakerWebHandler(burgerMaker)
        return router {
            POST("/burgers/new").invoke(webHandler::makeABurger )
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(BurgerMakerApplication::class.java, *args)
        }
    }
}