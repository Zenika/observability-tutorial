package com.zenika.tutoriels.observabilitydemo.burgermaker.service;

import com.zenika.tutoriels.observabilitydemo.api.contracts.FridgeErrorBody;

public class UnknownFridgeException extends FridgeException {

    public UnknownFridgeException(String ... messages) {
        super(new FridgeErrorBody("error.unknown", messages));
    }

    public UnknownFridgeException(FridgeErrorBody body){
        super(body);
    }
}
