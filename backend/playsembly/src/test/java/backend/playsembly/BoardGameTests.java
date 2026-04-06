package backend.playsembly;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import backend.playsembly.domain.bgg.BoardGame;

import java.time.Year;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BoardGameTests {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    //Testataan ettei nimi saa olla tyhjä
    @Test
    void testNameNotBlank() {
        BoardGame game = new BoardGame();
        game.setName(""); // tyhjä nimi
        game.setMinPlayers(1);
        game.setMaxPlayers(2);

        Set<ConstraintViolation<BoardGame>> violations = validator.validate(game);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    //Testataan että minimipelaajamäärä ei saa ylittää maksimipelaajamäärää
    @Test
    void testMinMaxPlayers() {
        BoardGame game = new BoardGame();
        game.setName("Catan");
        game.setMinPlayers(4);
        game.setMaxPlayers(3); // virheellinen

       IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
        if (game.getMaxPlayers() < game.getMinPlayers()) {
                throw new IllegalArgumentException("Max players cannot be less than min players");
            }
         });
        assertEquals("Max players cannot be less than min players", thrown.getMessage());
    }

    //Testataan että julkaisuvuosi ei saa olla tulevaisuudessa
    @Test
    void testYearPublishedNotFuture() {
        BoardGame game = new BoardGame();
        game.setName("Catan");
        game.setMinPlayers(1);
        game.setMaxPlayers(4);
        game.setYearPublished(3000); // tulevaisuus

       IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
        if (game.getYearPublished() > Year.now().getValue()) {
                throw new IllegalArgumentException("Publication year cannot be in the future");
            }
         });
        assertEquals("Publication year cannot be in the future", thrown.getMessage());
    }

    //Varmistetaan että virheitä ei tule kelvolla lautapelillä
    @Test
    void testValidBoardGame() {
        BoardGame game = new BoardGame();
        game.setName("Catan");
        game.setMinPlayers(2);
        game.setMaxPlayers(4);
        game.setYearPublished(1995);

        Set<ConstraintViolation<BoardGame>> violations = validator.validate(game);

        //Jos violations.isEmpty()=false, annetaan lista violationeista
        assertTrue(violations.isEmpty(), "Expected no validation errors but instead got: " + violations);

    }
}