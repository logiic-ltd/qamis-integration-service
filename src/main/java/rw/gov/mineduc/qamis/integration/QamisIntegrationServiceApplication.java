package rw.gov.mineduc.qamis.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"rw.gov.mineduc.qamis.integration", "rw.gov.mineduc.qamis.integration.config"})
public class QamisIntegrationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(QamisIntegrationServiceApplication.class, args);
	}

}
