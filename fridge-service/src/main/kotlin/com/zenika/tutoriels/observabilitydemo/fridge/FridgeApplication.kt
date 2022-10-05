package com.zenika.tutoriels.observabilitydemo.fridge

import com.zenika.tutoriels.observabilitydemo.fridge.business.CheeseInitialStock
import com.zenika.tutoriels.observabilitydemo.fridge.business.Fridge
import com.zenika.tutoriels.observabilitydemo.fridge.business.MeatInitialStock
import com.zenika.tutoriels.observabilitydemo.fridge.web.FridgeWebHandler
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.*


@ConfigurationProperties("fridge.meat")
@ConstructorBinding
data class FridgeMeatConfigurationProperties(val chicken: Int, val beef: Int, val pork: Int)

@ConfigurationProperties("fridge.cheese")
@ConstructorBinding
class FridgeCheeseConfigurationProperties(val goat: Int, val cheddar: Int, val blue: Int)


@SpringBootConfiguration
@EnableAutoConfiguration
@EnableConfigurationProperties(FridgeMeatConfigurationProperties::class, FridgeCheeseConfigurationProperties::class)
class FridgeApplication {



    @Bean
    fun fridge(meats: FridgeMeatConfigurationProperties, cheeses: FridgeCheeseConfigurationProperties): Fridge {
        return Fridge(MeatInitialStock(meats.chicken, meats.beef, meats.pork),
                CheeseInitialStock(cheeses.cheddar, cheeses.goat, cheeses.blue))
    }

    @Bean
    fun router(fridge: Fridge): RouterFunction<ServerResponse> {
        val handler = FridgeWebHandler(fridge)
        return router {
            accept(MediaType.APPLICATION_JSON)
                    .nest {
                        GET("/meats").invoke(handler::listMeats)
                        DELETE("/meats/{kind}").invoke(handler::orderMeatByKind)
                        GET("/cheeses").invoke(handler::listCheeses)
                        DELETE("/cheeses/{kind}").invoke(handler::orderCheeseByKind)
                    }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(FridgeApplication::class.java, *args)
        }
    }
}