
package com.zenika.tutoriels.observabilitydemo.burgermaker.clients;

import com.zenika.tutoriels.observabilitydemo.api.contracts.CheeseService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "${services.fridge.name:fridge}", contextId = "cheese",
        url = "${services.fridge.url:}", configuration = FridgeErrorClientConfiguration.class)
public interface CheeseServiceClient extends CheeseService {
}
