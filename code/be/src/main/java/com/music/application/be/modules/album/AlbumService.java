package com.music.application.be.modules.album;

import com.music.application.be.common.PagedResponse;
import com.music.application.be.modules.album.dto.AlbumResponseDTO;
import com.music.application.be.modules.album.dto.CreateAlbumDTO;
import com.music.application.be.modules.album.dto.UpdateAlbumDTO;
import com.music.application.be.modules.artist.Artist;
import com.music.application.be.modules.artist.ArtistRepository;
import com.music.application.be.modules.cloudinary.CloudinaryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AlbumService {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @CacheEvict(value = "allAlbums", allEntries = true)
    public AlbumResponseDTO createAlbum(CreateAlbumDTO createAlbumDTO, MultipartFile coverImageFile) throws IOException {
        if (coverImageFile == null || coverImageFile.isEmpty()) {
            throw new IllegalArgumentException("Cover image is required");
        }
        if (createAlbumDTO.getName() == null || createAlbumDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Album name is required");
        }
        Artist artist = artistRepository.findById(createAlbumDTO.getArtistId())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Artist not found with id: " + createAlbumDTO.getArtistId()));

        Album album = new Album();
        album.setName(createAlbumDTO.getName());
        album.setReleaseDate(createAlbumDTO.getReleaseDate());
        album.setDescription(createAlbumDTO.getDescription());
        album.setArtist(artist);

        String coverImageUrl = cloudinaryService.uploadFile(coverImageFile, "image");
        album.setCoverImage(coverImageUrl);

        Album savedAlbum = albumRepository.save(album);
        return mapToResponseDTO(savedAlbum);
    }

    @CachePut(value = "albums", key = "#id")
    public AlbumResponseDTO updateAlbum(Long id, UpdateAlbumDTO updateAlbumDTO, MultipartFile coverImageFile) throws IOException {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Album not found with id: " + id));

        if (updateAlbumDTO.getName() != null) {
            album.setName(updateAlbumDTO.getName());
        }
        if (updateAlbumDTO.getReleaseDate() != null) {
            album.setReleaseDate(updateAlbumDTO.getReleaseDate());
        }
        if (updateAlbumDTO.getDescription() != null) {
            album.setDescription(updateAlbumDTO.getDescription());
        }
        if (updateAlbumDTO.getArtistId() != null) {
            Artist artist = artistRepository.findById(updateAlbumDTO.getArtistId())
                    .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Artist not found with id: " + updateAlbumDTO.getArtistId()));
            album.setArtist(artist);
        }
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            String coverImageUrl = cloudinaryService.uploadFile(coverImageFile, "image");
            album.setCoverImage(coverImageUrl);
        }

        Album updatedAlbum = albumRepository.save(album);
        return mapToResponseDTO(updatedAlbum);
    }

    @CacheEvict(value = "allAlbums", allEntries = true)
    public AlbumResponseDTO updateAlbumCover(Long id, MultipartFile coverImageFile) throws IOException {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Album not found with id: " + id));

        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            String coverImageUrl = cloudinaryService.uploadFile(coverImageFile, "image");
            album.setCoverImage(coverImageUrl);
        }

        Album updatedAlbum = albumRepository.save(album);
        return mapToResponseDTO(updatedAlbum);
    }


    @Cacheable(value = "albums", key = "#id")
    public AlbumResponseDTO getAlbumById(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Album not found"));
        return mapToResponseDTO(album);
    }

    @Cacheable(
            value = "allAlbums",
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()"
    )
    public PagedResponse<AlbumResponseDTO> getAllAlbums(Pageable pageable) {
        Page<AlbumResponseDTO> pageResult = albumRepository.findAll(pageable)
                .map(this::mapToResponseDTO);

        return new PagedResponse<>(
                pageResult.getContent(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()
        );
    }

    @CacheEvict(value = "allAlbums", allEntries = true)
    public void deleteAlbum(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Album not found"));
        albumRepository.delete(album);
    }
    public Page<AlbumResponseDTO> searchAlbums(String query, Pageable pageable) {
        return albumRepository.findByNameContainingIgnoreCase(query, pageable).map(this::mapToResponseDTO);
    }

    public Page<AlbumResponseDTO> getAlbumsByArtist(Long artistId, Pageable pageable) {
        return albumRepository.findByArtistId(artistId, pageable).map(this::mapToResponseDTO);
    }

    public String shareAlbum(Long id) {
        return "http://localhost:8080/api/albums/" + id;
    }

    private AlbumResponseDTO mapToResponseDTO(Album album) {
        AlbumResponseDTO dto = new AlbumResponseDTO();
        dto.setId(album.getId());
        dto.setName(album.getName());
        dto.setReleaseDate(album.getReleaseDate());
        dto.setCoverImage(album.getCoverImage());
        dto.setDescription(album.getDescription());
        dto.setArtistId(album.getArtist().getId());
        return dto;
    }
}