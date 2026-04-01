package backend.playsembly.domain.bgg;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bgg")
public class BggController {

    private final BggService bggService;

    public BggController(BggService bggService) {
        this.bggService = bggService;
    }

    /**
     * REST endpoint pelihakuun.
     * Palauttaa JSON-listan SearchResultDTO:ita.
     */
    @GetMapping("/search")
    public List<SearchResultDTO> search(@RequestParam String query) throws Exception {
        return bggService.searchGames(query);
    }
}