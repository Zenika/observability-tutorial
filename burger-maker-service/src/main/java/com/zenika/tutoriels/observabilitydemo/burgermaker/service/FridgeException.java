package com.zenika.tutoriels.observabilitydemo.burgermaker.service;

import com.zenika.tutoriels.observabilitydemo.api.contracts.FridgeErrorBody;

public abstract class FridgeException extends RuntimeException {

    private final FridgeErrorBody errorBody;

    protected FridgeException(FridgeErrorBody errorBody) {
        this.errorBody = errorBody;
    }

    public FridgeErrorBody getErrorBody() {
        return errorBody;
    }
}
