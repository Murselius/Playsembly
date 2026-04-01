package backend.playsembly.web;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
 
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class WebSecurityConfig {
   
 
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        System.out.println("BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }
 
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
 
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/css/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/register**").permitAll()
                .requestMatchers("/login**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
        )
        .httpBasic(Customizer.withDefaults())
        .csrf(csrf -> csrf
            .ignoringRequestMatchers("/api/**") //API ei vaadi CSRF -tarkistusta, mahdollistaa Postmanin käytön
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
        .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
        .formLogin(form -> form
                .loginPage("/login") //oma login -sivu jotta saadaan register toimimaan / näkymään
                .defaultSuccessUrl("/events", true)
                .permitAll()
        )
        .logout(logout -> logout.permitAll());
        return http.build();
    }
 
}