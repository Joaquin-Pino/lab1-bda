package cl.grupo3.lab1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Punto de entrada de Spring Boot. Escanea todas las capas bajo cl.grupo3.lab1 (controller, service, repository, ...). */
@SpringBootApplication
@EnableScheduling // Habilita la ejecución de tareas programadas (cron jobs) en Spring Boot
public class Lab1Application {

	public static void main(String[] args) {
		SpringApplication.run(Lab1Application.class, args);
	}

}
