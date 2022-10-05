package com.zenika.tutoriels.observabilitydemo.burgermaker.service;

import com.zenika.tutoriels.observabilitydemo.api.contracts.FridgeErrorBody;

public class UnknownStuffException extends FridgeException {

    public UnknownStuffException(FridgeErrorBody errorBody) {
        super(errorBody);
    }

}
