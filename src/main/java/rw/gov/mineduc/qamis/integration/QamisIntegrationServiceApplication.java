package rw.gov.mineduc.qamis.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan(basePackages = {"rw.gov.mineduc.qamis.integration", "rw.gov.mineduc.qamis.integration.config"})
@EnableScheduling
public class QamisIntegrationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(QamisIntegrationServiceApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
