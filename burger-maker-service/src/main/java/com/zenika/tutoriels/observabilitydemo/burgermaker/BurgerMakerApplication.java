package com.zenika.tutoriels.observabilitydemo.burgermaker;

import com.zenika.tutoriels.observabilitydemo.api.contracts.CheeseService;
import com.zenika.tutoriels.observabilitydemo.api.contracts.MeatService;
import com.zenika.tutoriels.observabilitydemo.burgermaker.service.BurgerMaker;
import com.zenika.tutoriels.observabilitydemo.burgermaker.web.BurgerMakerController;
import com.zenika.tutoriels.observabilitydemo.burgermaker.web.BurgerMakerControllerAdvice;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableFeignClients("com.zenika.tutoriels.observabilitydemo.burgermaker.clients")
public class BurgerMakerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BurgerMakerApplication.class, args);
    }

    @Bean
    public BurgerMaker burgerMaker(MeatService meatService, CheeseService cheeseService) {
        return new BurgerMaker(meatService, cheeseService);
    }

    @Bean
    public BurgerMakerController burgerMakerController(BurgerMaker burgerMaker) {
        return new BurgerMakerController(burgerMaker);
    }

    @Bean
    public BurgerMakerControllerAdvice burgerMakerControllerAdvice() {
        return new BurgerMakerControllerAdvice();
    }

}
