package backend.playsembly;


import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import backend.playsembly.domain.AppUser;
import backend.playsembly.domain.AppUserRepository;
import backend.playsembly.domain.Event;
import backend.playsembly.domain.EventRepository;

@SpringBootApplication
public class PlaysemblyApplication {

	private final AppUserRepository appUserRepository;
    private final EventRepository eventRepository;
    private static final Logger log = LoggerFactory.getLogger(PlaysemblyApplication.class);

	PlaysemblyApplication(AppUserRepository appUserRepository, EventRepository eventRepository) {
		this.appUserRepository = appUserRepository;
		this. eventRepository = eventRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(PlaysemblyApplication.class, args);
	}

	@Bean
	@Profile("!test")
	public CommandLineRunner playDemo(Environment env) {
		return (args) -> {
			log.info("save example data");

			String rawAdminPassword = env.getProperty("admin.password");
            if (rawAdminPassword == null || rawAdminPassword.isBlank()) {
                throw new IllegalArgumentException("Environment variable 'admin.password' is not set!");
            }

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

			if (eventRepository.count()==0) {
				Event event1 = new Event("Jokicon kevät 2026", "Aurora rakennus, UEF", "Joensuu", LocalDateTime.of(2026,3,28,10,00), LocalDateTime.of(2026,3,29,19,00));
				eventRepository.save(event1);
			}

			// Create users: admin/admin user/user
			if (appUserRepository.count()==0) {
			AppUser user1 = new AppUser("user", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6", "USER");
			AppUser user2 = new AppUser("admin", encoder.encode(rawAdminPassword), "ADMIN");
			appUserRepository.save(user1);
			appUserRepository.save(user2);
			}
			
			log.info("fetch all data");
			for (Event event : eventRepository.findAll()) {
				log.info(event.toString());
			}

		};
	}

}
