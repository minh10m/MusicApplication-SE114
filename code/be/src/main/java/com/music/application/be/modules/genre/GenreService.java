package com.music.application.be.modules.genre;

import com.music.application.be.modules.genre.dto.GenreDTO;
import com.music.application.be.modules.genre.dto.GenreRequestDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class GenreService {

    @Autowired
    private GenreRepository genreRepository;

    // Create
    @CacheEvict(value = {"genres", "searchedGenres"}, allEntries = true)
    public GenreDTO createGenre(GenreRequestDTO genreRequestDTO) {
        Genre genre = new Genre();
        genre.setName(genreRequestDTO.getName());
        genre.setDescription(genreRequestDTO.getDescription());

        Genre savedGenre = genreRepository.save(genre);
        return mapToDTO(savedGenre);
    }

    // Read by ID
    @Cacheable(value = "genres", key = "#id")
    public GenreDTO getGenreById(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Genre not found with id: " + id));
        return mapToDTO(genre);
    }

    // Read all with pagination
    public Page<GenreDTO> getAllGenres(Pageable pageable) {
        return genreRepository.findAll(pageable).map(this::mapToDTO);
    }

    // Update
    @CachePut(value = "genres", key = "#id")
    @CacheEvict(value = "searchedGenres", allEntries = true)
    public GenreDTO updateGenre(Long id, GenreRequestDTO genreRequestDTO) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Genre not found with id: " + id));

        genre.setName(genreRequestDTO.getName());
        genre.setDescription(genreRequestDTO.getDescription());

        Genre updatedGenre = genreRepository.save(genre);
        return mapToDTO(updatedGenre);
    }

    // Delete
    @CacheEvict(value = {"genres", "searchedGenres"}, allEntries = true)
    public void deleteGenre(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Genre not found with id: " + id));
        genreRepository.delete(genre);
    }

    // Search genres
    public Page<GenreDTO> searchGenres(String query, Pageable pageable) {
        return genreRepository.findByNameContainingIgnoreCase(query, pageable).map(this::mapToDTO);
    }

    // Map entity to DTO
    private GenreDTO mapToDTO(Genre genre) {
        GenreDTO dto = new GenreDTO();
        dto.setId(genre.getId());
        dto.setName(genre.getName());
        dto.setDescription(genre.getDescription());
        return dto;
    }
}