package at.technikum_wien.swen3.paperless;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PaperlessRestApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaperlessRestApiApplication.class, args);
	}

}
