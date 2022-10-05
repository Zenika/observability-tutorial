package com.zenika.tutoriels.observabilitydemo.fridge.web;

import com.zenika.tutoriels.observabilitydemo.api.contracts.FridgeErrorBody;
import com.zenika.tutoriels.observabilitydemo.fridge.business.NoMoreFoodStuffException;
import com.zenika.tutoriels.observabilitydemo.fridge.business.UnknownFoodStuffException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FridgeErrorControllerAdvice {

    @ExceptionHandler(NoMoreFoodStuffException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public FridgeErrorBody noMoreStuffException(NoMoreFoodStuffException exception) {
        return new FridgeErrorBody("Stock empty", new String[]{exception.foodStuff().name(), exception.kind()});
    }

    @ExceptionHandler(UnknownFoodStuffException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public FridgeErrorBody unknownStuffException(UnknownFoodStuffException exception) {
        return new FridgeErrorBody("Unknown food", new String[]{exception.foodStuff().name(), exception.kind()});
    }

}
