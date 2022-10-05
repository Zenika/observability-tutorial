package com.zenika.tutoriels.observabilitydemo.fridge.business

import com.zenika.tutoriels.observabilitydemo.logger.utils.ServiceLoggerAutoConfiguration.Companion.withRequestInfoInMdc
import com.zenika.tutoriels.observabilitydemo.logger.utils.ServiceLoggerAutoConfiguration.Companion.withRequestInfoInMdcForLog
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.lang.Math.random
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors

enum class FoodStuff {
    cheese, meat
}


data class MeatInitialStock(val chicken: Int, val beef: Int, val pork: Int)

data class CheeseInitialStock(val goat: Int, val cheddar: Int, val blue: Int)

class NoMoreFoodStuffException(val foodStuff: FoodStuff, val kind: String) : RuntimeException()

class UnknownFoodStuffException(val foodStuff: FoodStuff, val kind: String) : RuntimeException()

class RandomException: RuntimeException()


class Fridge(meatInitialStock: MeatInitialStock, cheeseInitialStock: CheeseInitialStock) {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(Fridge::class.java)
    }

    private val stocks: EnumMap<FoodStuff, Map<String, AtomicInteger>> = EnumMap(FoodStuff::class.java)

    init {
        this.stocks[FoodStuff.meat] = mapOf("chicken" to AtomicInteger(meatInitialStock.chicken), "beef" to AtomicInteger(meatInitialStock.beef), "pork" to AtomicInteger(meatInitialStock.pork))
        this.stocks[FoodStuff.cheese] = mapOf("cheddar" to AtomicInteger(cheeseInitialStock.cheddar), "goat" to AtomicInteger(cheeseInitialStock.goat), "blue" to AtomicInteger(cheeseInitialStock.blue))
    }

    fun removeMeatFromFridge(meat: String): Mono<Void> {
        return removeFromStock(FoodStuff.meat, meat)
    }

    fun removeCheeseFromFridge(cheese: String): Mono<Void> {
        return removeFromStock(FoodStuff.cheese, cheese)
    }

    fun listAvailable(foodStuff: FoodStuff): Mono<List<String>> {
        return Mono.just(foodStuff)
                .doOnEach(withRequestInfoInMdcForLog { LOGGER.debug("Listing available {}s", foodStuff.name) })
                .map { s ->
                    stocks[s]!!.entries.stream()
                            .filter { it.value.get() > 0 }
                            .map { it.key }
                            .collect(Collectors.toList())
                }
    }

    private fun removeFromStock(stuff: FoodStuff, kind: String): Mono<Void> {
        return Mono.zip(Mono.just(stuff), Mono.just(kind))
                .doOnEach(withRequestInfoInMdcForLog { LOGGER.debug("Removing a porting of {} from {} stock", it!!.t2, it.t1) })
                .flatMap(withRequestInfoInMdc { remove(it.t1, it.t2) })
    }

    private fun remove(stuff: FoodStuff, kind: String): Mono<Void> {
        if (random() > 0.8) {
            return Mono.error(RandomException())
        }
        val capacity: AtomicInteger? = this.stocks[stuff]!![kind]

        if (capacity == null) {
            return Mono.error(UnknownFoodStuffException(stuff, kind))
        } else if (capacity.decrementAndGet() < 0) {
            LOGGER.error("No more {}: {}", stuff, kind)
            return Mono.error(NoMoreFoodStuffException(stuff, kind))
        }
        return Mono.empty()
    }

}