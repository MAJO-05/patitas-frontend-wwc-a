package pe.edu.cibertec.patitas_frontend_wc_a.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // Timeout para la conexión
            .responseTimeout(Duration.ofSeconds(5)) // Timeout para la respuesta
            .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS)) // Timeout de lectura
            );

    @Bean
    public WebClient webClientAuthentication(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:8081/autenticacion")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
