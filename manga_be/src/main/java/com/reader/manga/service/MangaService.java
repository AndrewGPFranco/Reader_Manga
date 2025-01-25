package com.reader.manga.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.reader.manga.dto.manga.GetMangaDTO;
import com.reader.manga.dto.manga.MangaDTO;
import com.reader.manga.dto.manga.UpdateMangaDTO;
import com.reader.manga.exception.CreationErrorException;
import com.reader.manga.exception.NotFoundException;
import com.reader.manga.model.Chapter;
import com.reader.manga.model.Manga;
import com.reader.manga.repository.MangaRepository;
import com.reader.manga.vo.MangaCoverVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MangaService {

    private final MangaRepository repository;

    static Map<String, String> coversManga = new HashMap<>();

    static {
        // Manga Covers
        coversManga.put("Naruto", "https://m.media-amazon.com/images/I/91xUwI2UEVL._AC_UF894,1000_QL80_.jpg");
        coversManga.put("Demon Slayer", "https://http2.mlstatic.com/D_NQ_NP_942681-MLU50423106087_062022-O.webp");
        coversManga.put("Jojo's", "https://m.media-amazon.com/images/I/91XRYa+4cHL._AC_UF1000,1000_QL80_.jpg");
        coversManga.put("My Hero Academia", "https://m.media-amazon.com/images/I/71bELfIWTDL._AC_UF1000,1000_QL80_.jpg");
        coversManga.put("One Piece", "https://m.media-amazon.com/images/I/716EGgqzyOL._AC_UF1000,1000_QL80_.jpg");
        coversManga.put("Hunter x Hunter", "https://www.jbchost.com.br/editorajbc/wp-content/uploads/2008/01/hunterxhunter-01-capaaz.jpg");
        coversManga.put("Bungo Stray Dogs", "https://m.media-amazon.com/images/I/81zJTGwXrtL._AC_UF1000,1000_QL80_.jpg");
        coversManga.put("Boruto", "https://m.media-amazon.com/images/I/81HpeSpReJL._AC_UF1000,1000_QL80_.jpg");
        coversManga.put("Tokyo Revengers", "https://m.media-amazon.com/images/I/711RqaljbIL.jpg");
        coversManga.put("Record of Ragnarok", "https://m.media-amazon.com/images/I/91ifr0L+XrL.jpg");
        coversManga.put("Dragon Ball", "https://m.media-amazon.com/images/I/81fHfEpEHTL._AC_UF1000,1000_QL80_.jpg");
        coversManga.put("Hellsing", "https://m.media-amazon.com/images/I/71KIyHsciwL._AC_UF1000,1000_QL80_.jpg");
        coversManga.put("Noragami", "https://m.media-amazon.com/images/I/91f63co2jKL._AC_UF1000,1000_QL80_.jpg");
        coversManga.put("The Rising of the Shield Hero", "https://m.media-amazon.com/images/I/71szZSLOYGL._AC_UF894,1000_QL80_.jpg");
    }

    public GetMangaDTO createManga(MangaDTO dto) {
        try {
            Manga manga = new Manga(dto.title(), dto.description(), dto.size(), dto.creationDate(), dto.closingDate(), dto.status(), dto.gender(), dto.author(),  dto.image());
            Manga savedManga = repository.save(manga);
            return new GetMangaDTO(savedManga.getId(), savedManga.getTitle(), savedManga.getDescription(), savedManga.getSize(), savedManga.getCreationDate(), savedManga.getClosingDate(), savedManga.getStatus(), savedManga.getGender(), savedManga.getAuthor(), savedManga.getImage());
        }catch (Exception e) {
            throw new CreationErrorException("Error creating Manga. Please try again... " + e.getMessage());
        }
    }

    public void deleteManga(Long id) {
        Optional<Manga> mangaById = repository.findById(id);
        if(mangaById.isEmpty()){
            throw new NotFoundException("No manga found with the id: " + id + ".");
        }
        repository.deleteById(id);
    }

    public List<Manga> readAllMangas(Pageable pageable) {
        if (pageable == null) {
            return repository.findAll();
        }

        Page<Manga> pageResult = repository.findAll(pageable);
        return pageResult.getContent();
    }

    public void updateManga(Long id, UpdateMangaDTO dto) {
        Manga manga = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manga not found"));

        UtilsService.updateField(dto.title(), manga::setTitle);
        UtilsService.updateField(dto.description(), manga::setDescription);
        UtilsService.updateField(dto.size(), manga::setSize);
        UtilsService.updateField(dto.creationDate(), manga::setCreationDate);
        UtilsService.updateField(dto.closingDate(), manga::setClosingDate);
        UtilsService.updateField(dto.status(), manga::setStatus);
        UtilsService.updateField(dto.author(), manga::setAuthor);
        UtilsService.updateField(dto.gender(), manga::setGender);
        UtilsService.updateField(dto.image(), manga::setImage);

        repository.save(manga);
    }

    public List<Manga> getAll() {
        return repository.findAll();
    }

    public Manga findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Mangá not found"));
    }

    public List<Chapter> getChaptersByManga(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Mangá not found")).getChapters();
    }

    public Mono<MangaCoverVO> fetchCoverForTitle(String title, WebClient webClient) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/manga")
                        .queryParam("title", title)
                        .queryParam("limit", 1)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(jsonNode -> {
                    JsonNode data = jsonNode.get("data");
                    if (data != null && !data.isEmpty()) {
                        JsonNode manga = data.get(0);
                        String mangaId = manga.get("id").asText();
                        return fetchCoverImage(mangaId, webClient);
                    }
                    return Mono.empty();
                });
    }

    private Mono<MangaCoverVO> fetchCoverImage(String mangaId, WebClient webClient) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/cover")
                        .queryParam("manga[]", mangaId)
                        .queryParam("limit", 1)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(jsonNode -> {
                    JsonNode data = jsonNode.get("data");
                    if (data != null && !data.isEmpty()) {
                        JsonNode cover = data.get(0);
                        String fileName = cover.get("attributes").get("fileName").asText();
                        String imageUrl = "https://uploads.mangadex.org/covers/" + mangaId + "/" + fileName;

                        return MangaCoverVO.builder().id(mangaId).imageUrl(imageUrl).build();
                    }
                    return null;
                });
    }

    public List<Map.Entry<String, String>> getRandomCovers(int max) {
        List<Map.Entry<String, String>> covers = new ArrayList<>(coversManga.entrySet());
        Collections.shuffle(covers);
        return covers.stream().limit(max).toList();
    }
}