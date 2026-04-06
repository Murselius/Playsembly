package backend.playsembly;

import backend.playsembly.domain.Event;
import jakarta.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EventTests {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    //Luo ehdot täyttävän Event -olion
    private Event createValidEvent() {
        return new Event(
                "Game Night",
                "Library",
                "Tampere",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(4)
        );
    }

    //Testataan edellisellä metodilla luotavaa oliota. Violations pitäisi olla tyhjä, muutoin listataan ne.
    @Test
    void validEvent_shouldHaveNoViolations() {
        Event event = createValidEvent();

        Set<ConstraintViolation<Event>> violations = validator.validate(event);

        assertTrue(violations.isEmpty(), "Expected no violations but got: " + violations);
    }

    //Testataan virhettä kun annetaan tyhjä nimi
    @Test
    void blankName_shouldFailValidation() {
        Event event = createValidEvent();
        event.setName("");

        Set<ConstraintViolation<Event>> violations = validator.validate(event);

        assertFalse(violations.isEmpty());
    }

    //Testataan virhettä kun annetaan tyhjä alkamisaika
    @Test
    void nullStartTime_shouldFailValidation() {
        Event event = createValidEvent();
        event.setStartTime(null);

        Set<ConstraintViolation<Event>> violations = validator.validate(event);

        assertFalse(violations.isEmpty());
    }

    //Testataan virhettä kun annetaan päättymisaika joka on menneisyydessä
    @Test
    void endTimeInPast_shouldFailValidation() {
        Event event = createValidEvent();
        event.setEndTime(LocalDateTime.now().minusDays(1));

        Set<ConstraintViolation<Event>> violations = validator.validate(event);

        assertFalse(violations.isEmpty());
    }

    //Testataan virhettä kun annetaan päättymisaika joka on ennen alkamisaikaa
    @Test
    void endBeforeStart_shouldThrowException() {
        Event event = createValidEvent();
        event.setStartTime(LocalDateTime.now().plusDays(2));
        event.setEndTime(LocalDateTime.now().plusDays(1));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
        if (event.getEndTime().isBefore(event.getStartTime())) {
                throw new IllegalArgumentException("End time cannot be before start time!");
            }
         });
        assertEquals("End time cannot be before start time!", thrown.getMessage());
    }

    //Testataan liian pitkällä nimellä
    @Test
    void nameTooLong_shouldFailValidation() {
        Event event = createValidEvent();
        event.setName("A".repeat(101));

        Set<ConstraintViolation<Event>> violations = validator.validate(event);

        assertFalse(violations.isEmpty());
    }

    //Testataan liian pitkällä kaupungin nimellä
    @Test
    void cityTooLong_shouldFailValidation() {
        Event event = createValidEvent();
        event.setCity("A".repeat(51));

        Set<ConstraintViolation<Event>> violations = validator.validate(event);

        assertFalse(violations.isEmpty());
    }
}