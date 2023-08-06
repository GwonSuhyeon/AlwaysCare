package server.AlwaysCare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AlwaysCareApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlwaysCareApplication.class, args);
	}

}
