package com.zenika.tutoriels.observabilitydemo.burgermaker.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenika.tutoriels.observabilitydemo.api.contracts.FridgeErrorBody;
import com.zenika.tutoriels.observabilitydemo.burgermaker.service.NoMoreInStockException;
import com.zenika.tutoriels.observabilitydemo.burgermaker.service.UnknownFridgeException;
import com.zenika.tutoriels.observabilitydemo.burgermaker.service.UnknownStuffException;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

public class FridgeErrorClientConfiguration {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(FridgeErrorClientConfiguration.class);

    @Bean
    public ErrorDecoder fridgeErrorDecoder(){
        return (methodKey, response) -> {
            try {
                String errorBody = new String(response.body().asInputStream().readAllBytes());
                return returnBusinessError(response.status(), errorBody);
            } catch (IOException e) {
                LOGGER.error("Unknown error while reading fridge error body", e);
                return new UnknownFridgeException(e.getClass().getName(), e.getMessage());
            }
        };
    }

    private static Exception returnBusinessError(int status, String errorBody) {
        try{
            FridgeErrorBody body = OBJECT_MAPPER.readValue(errorBody, FridgeErrorBody.class);
            return switch (status) {
                case 404 -> new UnknownStuffException(body);
                case 503 -> new NoMoreInStockException(body);
                default -> new UnknownFridgeException(body);
            };
        } catch (IOException e){
            return new UnknownFridgeException(errorBody);
        }
    }
}
