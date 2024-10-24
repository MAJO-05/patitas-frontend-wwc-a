package pe.edu.cibertec.patitas_frontend_wc_a;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class PatitasFrontendWcAApplication {

	public static void main(String[] args) {
		SpringApplication.run(PatitasFrontendWcAApplication.class, args);
	}

}
