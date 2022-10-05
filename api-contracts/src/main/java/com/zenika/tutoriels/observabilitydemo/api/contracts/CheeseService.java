package com.zenika.tutoriels.observabilitydemo.api.contracts;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface CheeseService {


    @GetMapping("/cheeses")
    List<String> listCheeses();

    @DeleteMapping("/cheeses/{kind}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void orderCheeseByKind(@PathVariable("kind") String kind);
}
