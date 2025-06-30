package com.music.application.be.modules.artist;

import com.music.application.be.common.PagedResponse;
import com.music.application.be.modules.album.AlbumRepository;
import com.music.application.be.modules.artist.dto.ArtistResponseDTO;
import com.music.application.be.modules.artist.dto.CreateArtistDTO;
import com.music.application.be.modules.artist.dto.UpdateArtistDTO;
import com.music.application.be.modules.cloudinary.CloudinaryService;
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
public class ArtistService {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public ArtistResponseDTO createArtist(CreateArtistDTO createArtistDTO, MultipartFile avatarFile) throws IOException {
        Artist artist = new Artist();
        artist.setName(createArtistDTO.getName());
        artist.setDescription(createArtistDTO.getDescription());
        artist.setFollowerCount(0);

        // Upload avatar to Cloudinary if file is provided
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarUrl = cloudinaryService.uploadFile(avatarFile, "image");
            artist.setAvatar(avatarUrl);
        }

        Artist savedArtist = artistRepository.save(artist);
        return mapToResponseDTO(savedArtist);
    }

    @Cacheable(value = "artists", key = "#id")
    public ArtistResponseDTO getArtistById(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artist not found"));
        return mapToResponseDTO(artist);
    }

    @Cacheable(
            value = "allArtists",
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()"
    )
    public PagedResponse<ArtistResponseDTO> getAllArtists(Pageable pageable) {
        Page<ArtistResponseDTO> pageResult = artistRepository.findAll(pageable)
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

    @CachePut(value = "artists", key = "#id")
    public ArtistResponseDTO updateArtist(Long id, UpdateArtistDTO updateArtistDTO, MultipartFile avatarFile) throws IOException {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artist not found"));

        if (updateArtistDTO.getName() != null) {
            artist.setName(updateArtistDTO.getName());
        }
        if (updateArtistDTO.getDescription() != null) {
            artist.setDescription(updateArtistDTO.getDescription());
        }

        // Upload new avatar to Cloudinary if file is provided
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarUrl = cloudinaryService.uploadFile(avatarFile, "image");
            artist.setAvatar(avatarUrl);
        }

        Artist updatedArtist = artistRepository.save(artist);
        return mapToResponseDTO(updatedArtist);
    }

    @CacheEvict(value = "artists", key = "#id")
    public void deleteArtist(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artist not found"));
        artistRepository.delete(artist);
    }

    public Page<ArtistResponseDTO> searchArtists(String query, Pageable pageable) {
        return artistRepository.findByNameContainingIgnoreCase(query, pageable).map(this::mapToResponseDTO);
    }

    public String shareArtist(Long id) {
        return "https://musicapp.com/artist/" + id;
    }

    private ArtistResponseDTO mapToResponseDTO(Artist artist) {
        ArtistResponseDTO dto = new ArtistResponseDTO();
        dto.setId(artist.getId());
        dto.setName(artist.getName());
        dto.setAvatar(artist.getAvatar());
        dto.setDescription(artist.getDescription());
        dto.setFollowerCount(artist.getFollowerCount());
        return dto;
    }
}