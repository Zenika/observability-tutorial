package com.zenika.tutoriels.observabilitydemo.fridge.business;

public class UnknownFoodStuffException extends RuntimeException {

    private final FoodStuff foodStuff;

    private final String kind;

    public UnknownFoodStuffException(FoodStuff foodStuff, String kind) {
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
