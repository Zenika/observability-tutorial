package com.zenika.tutoriels.observabilitydemo.fridge.web;

import com.zenika.tutoriels.observabilitydemo.api.contracts.CheeseService;
import com.zenika.tutoriels.observabilitydemo.fridge.business.Fridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
public record CheeseController(Fridge fridge) implements CheeseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheeseController.class);

    @Override
    public List<String> listCheeses() {
        LOGGER.info("listCheeses - init");
        List<String> result = this.fridge.listAvailableCheeses();
        LOGGER.info("listCheeses - end");
        return result;
    }

    @Override
    public void orderCheeseByKind(String kind) {
        LOGGER.info("orderCheeseByKind - {} - init", kind);
        this.fridge.removeCheeseFromFridge(kind);
        LOGGER.info("orderCheeseByKind - {} - end", kind);
    }
}
