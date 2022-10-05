package com.zenika.tutoriels.observabilitydemo.burgermaker.service;

import com.zenika.tutoriels.observabilitydemo.api.contracts.CheeseService;
import com.zenika.tutoriels.observabilitydemo.api.contracts.MeatService;

public record BurgerMaker(MeatService meatService, CheeseService cheeseService) {

    public void makeABurger(String meat, String cheese){
        this.meatService.orderMeatByKind(meat);
        this.cheeseService.orderCheeseByKind(cheese);
    }
}
