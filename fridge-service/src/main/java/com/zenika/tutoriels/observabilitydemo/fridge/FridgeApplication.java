package com.zenika.tutoriels.observabilitydemo.fridge;

import com.zenika.tutoriels.observabilitydemo.fridge.business.CheeseInitialStock;
import com.zenika.tutoriels.observabilitydemo.fridge.business.Fridge;
import com.zenika.tutoriels.observabilitydemo.fridge.business.MeatInitialStock;
import com.zenika.tutoriels.observabilitydemo.fridge.web.CheeseController;
import com.zenika.tutoriels.observabilitydemo.fridge.web.FridgeErrorControllerAdvice;
import com.zenika.tutoriels.observabilitydemo.fridge.web.MeatController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import static java.lang.Math.random;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableConfigurationProperties({FridgeApplication.FridgeMeatConfigurationProperties.class,
        FridgeApplication.FridgeCheeseConfigurationProperties.class})
public class FridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(FridgeApplication.class, args);
    }

    @ConfigurationProperties("fridge.meat")
    @ConstructorBinding
    public record FridgeMeatConfigurationProperties(int chicken, int beef, int pork) {
    }

    @ConfigurationProperties("fridge.cheese")
    @ConstructorBinding
    public record FridgeCheeseConfigurationProperties(int cheddar, int goat, int blue) {
    }

    @Bean
    @Profile("!generate-random-failures")
    public Fridge getSafeFridge(FridgeMeatConfigurationProperties meats, FridgeCheeseConfigurationProperties cheeses) {
        return new Fridge(new MeatInitialStock(meats.chicken(), meats.beef(), meats.pork()),
                new CheeseInitialStock(cheeses.cheddar(), cheeses.goat(), cheeses.blue()), () -> {});
    }

    @Bean
    @Profile("generate-random-failures")
    public Fridge getFailingFridge(FridgeMeatConfigurationProperties meats, FridgeCheeseConfigurationProperties cheeses) {
        return new Fridge(new MeatInitialStock(meats.chicken(), meats.beef(), meats.pork()),
                new CheeseInitialStock(cheeses.cheddar(), cheeses.goat(), cheeses.blue()), () -> {
            if(random() > 0.8d){
                throw new RuntimeException("Random error");
            }
        });
    }

    @Bean
    public MeatController meatController(Fridge fridge) {
        return new MeatController(fridge);
    }

    @Bean
    public CheeseController cheeseController(Fridge fridge) {
        return new CheeseController(fridge);
    }

    @Bean
    public FridgeErrorControllerAdvice errorControllerAdvice(){
        return new FridgeErrorControllerAdvice();
    }

}
