package com.zenika.tutoriels.observabilitydemo.fridge.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.Math.random;

public class Fridge {

    private static final Logger LOGGER = LoggerFactory.getLogger(Fridge.class);

    private final Map<FoodStuff, Map<String, AtomicInteger>> stocks;

    private final FailingService failingService;


    public Fridge(MeatInitialStock meatInitialStock, CheeseInitialStock cheeseInitialStock, FailingService failingService) {
        this.stocks = Map.of(FoodStuff.meat,
                Map.of("chicken", new AtomicInteger(meatInitialStock.chicken()),
                        "beef", new AtomicInteger(meatInitialStock.beef()),
                        "pork", new AtomicInteger(meatInitialStock.pork())),
                FoodStuff.cheese,
                Map.of("cheddar", new AtomicInteger(cheeseInitialStock.cheddar()),
                        "goat", new AtomicInteger(cheeseInitialStock.goat()),
                        "blue", new AtomicInteger(cheeseInitialStock.blue())
                )
        );
        this.failingService = failingService;
    }

    public List<String> listAvailableMeats() {
        return getStock(FoodStuff.meat);
    }

    public List<String> listAvailableCheeses() {
        return getStock(FoodStuff.cheese);
    }

    public void removeMeatFromFridge(String meat) {
        removeFromStock(FoodStuff.meat, meat);
    }

    public void removeCheeseFromFridge(String cheese) {
        removeFromStock(FoodStuff.cheese, cheese);
    }

    private List<String> getStock(FoodStuff foodStuff) {
        LOGGER.debug("Listing available {}s", foodStuff.name());
        return stocks.get(foodStuff).entrySet().stream()
                .filter(entry -> entry.getValue().get() > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private void removeFromStock(FoodStuff stuff, String kind) {
        LOGGER.debug("Removing a porting of {} from {} stock", kind, stuff);
        this.failingService.potentiallyFail();
        AtomicInteger capacity = this.stocks.get(stuff).get(kind);
        if (capacity == null) {
            throw new UnknownFoodStuffException(stuff, kind);
        } else if (capacity.decrementAndGet() < 0) {
            LOGGER.error("No more {}: {}", stuff, kind);
            throw new NoMoreFoodStuffException(stuff, kind);
        }
    }

}
