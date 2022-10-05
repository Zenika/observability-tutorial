package com.zenika.tutoriels.observabilitydemo.burgermaker.web;

import com.zenika.tutoriels.observabilitydemo.api.contracts.FridgeErrorBody;
import com.zenika.tutoriels.observabilitydemo.burgermaker.service.NoMoreInStockException;
import com.zenika.tutoriels.observabilitydemo.burgermaker.service.UnknownFridgeException;
import com.zenika.tutoriels.observabilitydemo.burgermaker.service.UnknownStuffException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static io.micrometer.core.instrument.Metrics.counter;

@RestControllerAdvice
public class BurgerMakerControllerAdvice {

    @ExceptionHandler(NoMoreInStockException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public FridgeErrorBody noMoreStockException(NoMoreInStockException error) {
        counter("application.fridge.no-more-in-stocks",
                "foodStuff", error.getErrorBody().parameters()[0],
                "kind", error.getErrorBody().parameters()[1]).increment();
        return error.getErrorBody();
    }
    @ExceptionHandler(UnknownStuffException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public FridgeErrorBody remoteNotFound(UnknownStuffException error) {
        return error.getErrorBody();
    }
    @ExceptionHandler(UnknownFridgeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public FridgeErrorBody unknownFridgeError(UnknownFridgeException error) {
        counter("application.fridge.unknown-error").increment();
        return error.getErrorBody();
    }

}
