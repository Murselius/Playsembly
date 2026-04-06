package backend.playsembly.web;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import backend.playsembly.domain.AppUser;
import backend.playsembly.domain.AppUserRepository;
import backend.playsembly.dto.RegistrationForm;

@Controller
public class RegistrationFormController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationFormController(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute RegistrationForm form, Model model) {

        //Tarkistetaan onko salasana ja confirmPassword sama
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match");
            return "register";
        }

        //Tarkistetaan onko käyttäjänimi jo olemassa, annetaan virheilmoitus jos on
        if (userRepository.existsByUsername(form.getUsername())) {
            model.addAttribute("error", "Username already taken");
            return "register";
        }

        //Luodaan uusi käyttäjä ja tallennetaan Hashattu salasana
        AppUser user = new AppUser();
        String role = "ROLE_USER"; //täytyy olla ROLE_ etuliite tietokannassakin Authority checkkejä varten
        user.setUsername(form.getUsername());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRole(role);
        userRepository.save(user);

        //palautetaan onnistumisviesti ja ohjataan takaisin Login -sivulle.
        model.addAttribute("message", "Registration successful! You can now login.");
        return "register";
    }
}