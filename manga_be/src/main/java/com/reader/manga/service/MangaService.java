package com.reader.manga.service;

import com.reader.manga.dto.GetMangaDTO;
import com.reader.manga.dto.MangaDTO;
import com.reader.manga.model.Manga;
import com.reader.manga.repository.MangaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MangaService {

    @Autowired
    private MangaRepository repository;

    public GetMangaDTO createManga(MangaDTO dto) {
        try {
            Manga manga = new Manga(dto.title(), dto.description(), dto.size(), dto.creationDate(), dto.closingDate(), dto.status(), dto.gender(), dto.author(),  dto.image());
            Manga savedManga = repository.save(manga);
            return new GetMangaDTO(savedManga.getId(), savedManga.getTitle(), savedManga.getDescription(), savedManga.getSize(), savedManga.getCreationDate(), savedManga.getClosingDate(), savedManga.getStatus(), savedManga.getGender(), savedManga.getAuthor(),  savedManga.getImage());
        }catch (Exception e) {
            throw new RuntimeException("Error creating Manga. Please try again...");
        }
    }

    public void deleteManga(Long id) {
        repository.findById(id).orElseThrow(() -> new RuntimeException("Manga not found"));
        repository.deleteById(id);
    }

    public List<Manga> readAllMangas(Pageable pageable) {
        if (pageable == null) {
            return repository.findAll();
        }

        Page<Manga> pageResult = repository.findAll(pageable);
        return pageResult.getContent();
    }
}