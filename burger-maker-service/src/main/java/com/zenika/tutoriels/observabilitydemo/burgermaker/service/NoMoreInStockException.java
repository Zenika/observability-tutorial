package com.zenika.tutoriels.observabilitydemo.burgermaker.service;

import com.zenika.tutoriels.observabilitydemo.api.contracts.FridgeErrorBody;

public class NoMoreInStockException extends FridgeException {

    public NoMoreInStockException(FridgeErrorBody errorBody) {
        super(errorBody);
    }

}
