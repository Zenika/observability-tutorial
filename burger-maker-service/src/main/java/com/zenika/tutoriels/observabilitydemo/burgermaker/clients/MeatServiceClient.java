
package com.zenika.tutoriels.observabilitydemo.burgermaker.clients;

import com.zenika.tutoriels.observabilitydemo.api.contracts.MeatService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "${services.fridge.name:fridge}", contextId = "meat",
        url = "${services.fridge.url:}", configuration = FridgeErrorClientConfiguration.class)
public interface MeatServiceClient extends MeatService {
}
