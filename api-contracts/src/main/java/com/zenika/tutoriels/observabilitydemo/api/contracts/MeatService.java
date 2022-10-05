package com.zenika.tutoriels.observabilitydemo.api.contracts;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface MeatService {

    @GetMapping("/meats")
    List<String> listMeats();

    @DeleteMapping("/meats/{kind}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void orderMeatByKind(@PathVariable("kind") String kind);

}
