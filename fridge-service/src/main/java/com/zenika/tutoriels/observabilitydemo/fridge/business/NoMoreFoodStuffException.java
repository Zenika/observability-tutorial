package com.zenika.tutoriels.observabilitydemo.fridge.business;

public class NoMoreFoodStuffException extends RuntimeException {

    private final FoodStuff foodStuff;

    private final String kind;

    public NoMoreFoodStuffException(FoodStuff foodStuff, String kind) {
        this.foodStuff = foodStuff;
        this.kind = kind;
    }

    public FoodStuff foodStuff() {
        return foodStuff;
    }

    public String kind() {
        return kind;
    }

}
