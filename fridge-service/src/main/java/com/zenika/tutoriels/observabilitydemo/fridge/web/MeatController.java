package com.zenika.tutoriels.observabilitydemo.fridge.web;

import com.zenika.tutoriels.observabilitydemo.api.contracts.MeatService;
import com.zenika.tutoriels.observabilitydemo.fridge.business.Fridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public record MeatController(Fridge fridge) implements MeatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeatController.class);

    @Override
    public List<String> listMeats() {
        LOGGER.info("listMeats - init");
        List<String> result = fridge.listAvailableMeats();
        LOGGER.info("listMeats - end");
        return result;
    }

    @Override
    public void orderMeatByKind(String kind) {
        LOGGER.info("orderMeatByKind - {} - init", kind);
        this.fridge.removeMeatFromFridge(kind);
        LOGGER.info("orderMeatByKind - {} - end", kind);
    }
}
