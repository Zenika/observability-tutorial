import com.zenika.tutoriels.observabilitydemo.burgermaker.clients.CheeseServiceClient
import com.zenika.tutoriels.observabilitydemo.burgermaker.clients.MeatServiceClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@ConfigurationProperties("services.fridge")
@ConstructorBinding
data class FridgeServiceProperties(val url: String)

@Configuration
@EnableConfigurationProperties(FridgeServiceProperties::class)
class FridgeServiceClientConfiguration {
    /** Instantiate WebClient in order to let spring sleuth instrument it
     * See: [documentation](https://docs.spring.io/spring-cloud-sleuth/docs/current-SNAPSHOT/reference/html/integrations.html)
     * and more specifically: TraceWebFluxConfiguration and TraceWebClientBeanPostProcessor
     * @return the wel client that will be instrumented
     */
    @Bean
    fun webClient(): WebClient.Builder {
        return WebClient.builder()
    }

    @Bean
    fun cheeseServiceClient(properties: FridgeServiceProperties,
                            builder: WebClient.Builder): CheeseServiceClient {
        return CheeseServiceClient(properties.url, builder)
    }

    @Bean
    fun meatServiceClient(properties: FridgeServiceProperties,
                          builder: WebClient.Builder): MeatServiceClient {
        return MeatServiceClient(properties.url, builder)
    }
}
