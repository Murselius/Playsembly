package backend.playsembly.domain.bgg;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import backend.playsembly.domain.BoardGameRepository;

@Service
public class BggService {

    private final BoardGameRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final XmlMapper xmlMapper = new XmlMapper();

    //Injektoitu bgg -autorisointi token environmentista
    @Value("${bgg.api.token}")
    private String bggToken;

    public BggService(BoardGameRepository repository) {
        this.repository = repository;
    }

    //Hakee pelejä BGG:stä heidän xml -apin kautta
    public List<SearchResultDTO> searchGames(String query) throws Exception {

        String url = "https://boardgamegeek.com/xmlapi2/search?query=" + query + "&type=boardgame";

        //lisätään headeriin oma bgg-token jotta query menee läpi
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + bggToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        //response olio
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        //tehdään oliosta string muoto
        String xml = response.getBody();

        System.out.println(xml);

        //xmlMapper tallettaa searchResponseen halutut kentät aiemmin muodostetusta string -esityksestä
        SearchResponse searchResponse = xmlMapper.readValue(xml, SearchResponse.class);

        //Tehdään uusi lista haun tulosten DTO:ista
        List<SearchResultDTO> results = Collections.emptyList();

        if (searchResponse.getItem() != null) {
        results = searchResponse.getItem().stream().limit(20)
                .map(item -> new SearchResultDTO(
                        item.getId(),
                        item.getPrimaryName(),
                        item.getYearpublished() != null ? item.getYearpublished().getValue() : "",
                        item.getThumbnail() != null ? item.getThumbnail().getValue() : ""
                ))
                .toList();
        }
        
        //Ajetaan lista kuvien lisäyksen läpi ja palautetaan se
        return enrichWithImages(results);
    }

    //koska bgg xml ei palauta kuvia haussa, ajan haun tulokset tämän läpi ja haen kullekkin erikseen kuvan jonka liitän hakutulosten listaukseen.
    public List<SearchResultDTO> enrichWithImages(List<SearchResultDTO> results) throws Exception {

        // yhdistä ID:t pilkulla
        String ids = results.stream()
                .map(r -> r.getId().toString())
                .collect(Collectors.joining(","));

        //Muodostetaan String muotoinen url jossa loppuun lisätään pelin id jotta osataan hakea oikea kuva
        String url = "https://boardgamegeek.com/xmlapi2/thing?id=" + ids;

        //Lisätään taas bgg -token jotta haku menee läpi
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + bggToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        //Luodaan haun vastineolio
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        //Tehdään vastineesta string -muoto
        String xml = response.getBody();

        //Ajetaan string xmlMapperin läpi ja otetaan haluttu data talteen
        ThingResponse thingResponse = xmlMapper.readValue(xml, ThingResponse.class);

        // mapataan jokaisesta id:n perusteella → image
        Map<Long, String> imageMap = thingResponse.getItems().stream()
                .collect(Collectors.toMap(
                        GameItem::getId,
                        item -> item.getThumbnail() != null ? item.getThumbnail().getValue() : ""
                ));

        // yhdistä kuvat search tuloksiin
        results.forEach(r -> r.setImageUrl(imageMap.get(r.getId())));

        //palauta tulokset kuvineen
        return results;
    }

    // 🔹 Hakee yksittäisen pelin BGG:stä ja tallentaa tietokantaan
    public BoardGame importGame(Long id) throws Exception {

        if (repository.existsById(id)) {
            return repository.findById(id).get();
        }

        String url = "https://boardgamegeek.com/xmlapi2/thing?id=" + id;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + bggToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        String xml = response.getBody();

        ThingResponse thingResponse = xmlMapper.readValue(xml, ThingResponse.class);

        GameItem item = thingResponse.getItems().get(0);

        BoardGame game = new BoardGame();
        game.setBggId(item.getId());
        game.setName(item.getPrimaryName());
        game.setMinPlayers(Integer.parseInt(item.getMinplayers().getValue()));
        game.setMaxPlayers(Integer.parseInt(item.getMaxplayers().getValue()));

        if (item.getImage() != null && item.getImage().getValue() != null) {
            game.setImageUrl(item.getImage().getValue());
        }

        if (item.getYearpublished() != null && item.getYearpublished().getValue() != null) {
            game.setYearPublished(Integer.parseInt(item.getYearpublished().getValue()));
        }

        if (item.getDescription() != null) {
            game.setDescription(cleanDescription(item.getDescription()));
        }

        System.out.println("BoardGame imageUrl = " + game.getImageUrl());


        return repository.save(game);
    }

    //ajellaan regexin läpi ja siivotaan turhaa junk tekstiä
    private String cleanDescription(String raw) {
        return raw
                .replaceAll("&#10;", "\n")
                .replaceAll("&quot;", "\"")
                .replaceAll("&amp;", "&")
                .replaceAll("<.*?>", "") // poistaa HTML-tagit
                .replaceAll("\\s*-description from the publisher\\s*$", "")
                .replaceAll("\\s*&mdash;description from the publisher\\s*$", "")
                .trim();
    }       
}