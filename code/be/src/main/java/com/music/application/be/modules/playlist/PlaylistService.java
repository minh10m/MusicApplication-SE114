package com.music.application.be.modules.playlist;

import com.music.application.be.modules.genre.Genre;
import com.music.application.be.modules.genre.GenreRepository;
import com.music.application.be.modules.playlist.dto.PlaylistDTO;
import com.music.application.be.modules.playlist.dto.PlaylistRequestDTO;
import com.music.application.be.modules.role.Role;
import com.music.application.be.modules.song.Song;
import com.music.application.be.modules.song.SongRepository;
import com.music.application.be.modules.song_playlist.SongPlaylist;
import com.music.application.be.modules.song_playlist.SongPlaylistRepository;
import com.music.application.be.modules.user.User;
import com.music.application.be.modules.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaylistService {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private SongPlaylistRepository songPlaylistRepository;

    @Autowired
    private UserRepository userRepository;

    // Create playlist for user (no genre)
    @CacheEvict(value = {"playlists", "searchedPlaylists"}, allEntries = true)
    public PlaylistDTO createPlaylist(PlaylistRequestDTO playlistRequestDTO) {
        User user = userRepository.findById(playlistRequestDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + playlistRequestDTO.getUserId()));

        Playlist playlist = new Playlist();
        playlist.setName(playlistRequestDTO.getName());
        playlist.setDescription(playlistRequestDTO.getDescription());
        playlist.setCreatedAt(LocalDateTime.now());
        playlist.setCreatedBy(user);

        // Không xử lý genre cho user
        Playlist savedPlaylist = playlistRepository.save(playlist);
        return mapToDTO(savedPlaylist);
    }

    // Create playlist for admin with genres (auto-add songs)
    @CacheEvict(value = {"playlists", "searchedPlaylists"}, allEntries = true)
    public PlaylistDTO createPlaylistWithGenres(PlaylistRequestDTO playlistRequestDTO) {
        User user = userRepository.findById(playlistRequestDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + playlistRequestDTO.getUserId()));

        Playlist playlist = new Playlist();
        playlist.setName(playlistRequestDTO.getName());
        playlist.setDescription(playlistRequestDTO.getDescription());
        playlist.setCreatedAt(LocalDateTime.now());
        playlist.setCreatedBy(user);

        // Liên kết genres (chỉ admin sử dụng)
        if (playlistRequestDTO.getGenreIds() != null && !playlistRequestDTO.getGenreIds().isEmpty()) {
            List<Genre> genres = genreRepository.findAllById(playlistRequestDTO.getGenreIds());
            if (genres.size() != playlistRequestDTO.getGenreIds().size()) {
                throw new EntityNotFoundException("One or more genres not found");
            }
            playlist.setGenres(genres);
        } else {
            throw new IllegalArgumentException("Genre IDs are required for admin-created playlists");
        }

        Playlist savedPlaylist = playlistRepository.save(playlist);

        // Tự động thêm các bài hát có genre trùng
        if (!playlist.getGenres().isEmpty()) {
            List<Song> matchingSongs = songRepository.findByGenresIn(playlist.getGenres());
            for (Song song : matchingSongs) {
                SongPlaylist songPlaylist = new SongPlaylist();
                songPlaylist.setSong(song);
                songPlaylist.setPlaylist(savedPlaylist);
                songPlaylist.setAddedAt(LocalDateTime.now());
                songPlaylistRepository.save(songPlaylist);
            }
        }

        return mapToDTO(savedPlaylist);
    }

    // Read by ID
    @Cacheable(value = "playlists", key = "#id")
    public PlaylistDTO getPlaylistById(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + id));
        return mapToDTO(playlist);
    }

    // Read all with pagination
    @Cacheable(value = "playlists", key = "'all-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<PlaylistDTO> getAllPlaylists(Pageable pageable) {
        return playlistRepository.findAll(pageable).map(this::mapToDTO);
    }

    // Update
    @CachePut(value = "playlists", key = "#id")
    @CacheEvict(value = "searchedPlaylists", allEntries = true)
    public PlaylistDTO updatePlaylist(Long id, PlaylistRequestDTO playlistRequestDTO) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + id));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
        if (!currentUser.getId().equals(playlist.getCreatedBy().getId()) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new SecurityException("You do not have permission to update this playlist");
        }

        playlist.setName(playlistRequestDTO.getName());
        playlist.setDescription(playlistRequestDTO.getDescription());

        if (playlistRequestDTO.getGenreIds() != null) {
            List<Genre> genres = genreRepository.findAllById(playlistRequestDTO.getGenreIds());
            if (genres.size() != playlistRequestDTO.getGenreIds().size()) {
                throw new EntityNotFoundException("One or more genres not found");
            }
            playlist.setGenres(genres);
        } else {
            playlist.setGenres(null);
        }

        List<SongPlaylist> existingSongs = songPlaylistRepository.findByPlaylistIdOrderByAddedAtDesc(id);
        songPlaylistRepository.deleteAll(existingSongs);

        if (playlist.getGenres() != null && !playlist.getGenres().isEmpty()) {
            List<Song> matchingSongs = songRepository.findByGenresIn(playlist.getGenres());
            for (Song song : matchingSongs) {
                SongPlaylist songPlaylist = new SongPlaylist();
                songPlaylist.setSong(song);
                songPlaylist.setPlaylist(playlist);
                songPlaylist.setAddedAt(LocalDateTime.now());
                songPlaylistRepository.save(songPlaylist);
            }
        }

        Playlist updatedPlaylist = playlistRepository.save(playlist);
        return mapToDTO(updatedPlaylist);
    }

    // Delete
    @CacheEvict(value = {"playlists", "searchedPlaylists"}, allEntries = true)
    public void deletePlaylist(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + id));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
        if (!currentUser.getId().equals(playlist.getCreatedBy().getId()) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new SecurityException("You do not have permission to delete this playlist");
        }

        playlistRepository.delete(playlist);
    }

    // Search playlists
    public Page<PlaylistDTO> searchPlaylists(String query, Pageable pageable) {
        return playlistRepository.findByNameContainingIgnoreCase(query, pageable).map(this::mapToDTO);
    }

    // Share playlist
    @Cacheable(value = "playlists", key = "'share-' + #id")
    public String sharePlaylist(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + id));
        return "http://localhost:8080/api/playlists/" + id;
    }

    // Map entity to DTO
    private PlaylistDTO mapToDTO(Playlist playlist) {
        PlaylistDTO dto = new PlaylistDTO();
        dto.setId(playlist.getId());
        dto.setName(playlist.getName());
        dto.setDescription(playlist.getDescription());
        dto.setCreatedAt(playlist.getCreatedAt());
        dto.setGenreIds(playlist.getGenres() != null ? playlist.getGenres().stream().map(Genre::getId).collect(Collectors.toList()) : null);
        dto.setUserId(playlist.getCreatedBy().getId());
        return dto;
    }
}