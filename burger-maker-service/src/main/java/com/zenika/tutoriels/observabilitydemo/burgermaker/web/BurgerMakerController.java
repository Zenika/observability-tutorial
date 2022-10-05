package com.zenika.tutoriels.observabilitydemo.burgermaker.web;

import com.zenika.tutoriels.observabilitydemo.burgermaker.service.BurgerMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public record BurgerMakerController(BurgerMaker burgerMaker) {

    private static final Logger LOGGER = LoggerFactory.getLogger(BurgerMakerController.class);

    public static class IllegalOrderException extends RuntimeException {
    }

    public record BurgerOrder(String meat, String cheese) {
    }

    @ExceptionHandler(IllegalOrderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    void invalidOrder(){}

    @PostMapping("/burgers/new")
    void order(@RequestBody BurgerOrder order) {
        LOGGER.info("order init");
        if (order.meat() == null || order.meat().isEmpty() || order.cheese() == null || order.cheese().isEmpty()) {
            throw new IllegalOrderException();
        }
        this.burgerMaker.makeABurger(order.meat(), order.cheese());
        LOGGER.info("order end");
    }
}
