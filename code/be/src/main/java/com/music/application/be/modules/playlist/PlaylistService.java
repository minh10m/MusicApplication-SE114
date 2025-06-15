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
import com.music.application.be.modules.song_playlist.dto.SongPlaylistDTO;
import com.music.application.be.modules.user.User;
import com.music.application.be.modules.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();

        Playlist playlist = new Playlist();
        playlist.setName(playlistRequestDTO.getName());
        playlist.setDescription(playlistRequestDTO.getDescription());
        playlist.setCreatedAt(LocalDateTime.now());
        playlist.setCreatedBy(user);

        Playlist savedPlaylist = playlistRepository.save(playlist);
        // Cập nhật thumbnail từ bài hát đầu tiên (nếu có)
        updateThumbnail(savedPlaylist.getId());
        return mapToDTO(savedPlaylist);
    }

    // Create playlist for admin with genres (auto-add songs)
    @CacheEvict(value = {"playlists", "searchedPlaylists"}, allEntries = true)
    public PlaylistDTO createPlaylistWithGenres(PlaylistRequestDTO playlistRequestDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();

        Playlist playlist = new Playlist();
        playlist.setName(playlistRequestDTO.getName());
        playlist.setDescription(playlistRequestDTO.getDescription());
        playlist.setCreatedAt(LocalDateTime.now());
        playlist.setCreatedBy(user);

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

        if (!playlist.getGenres().isEmpty()) {
            List<Song> matchingSongs = songRepository.findByGenresIn(playlist.getGenres());
            for (Song song : matchingSongs) {
                SongPlaylist songPlaylist = new SongPlaylist();
                songPlaylist.setSong(song);
                songPlaylist.setPlaylist(savedPlaylist);
                songPlaylist.setAddedAt(LocalDateTime.now());
                songPlaylistRepository.save(songPlaylist);
            }
            // Cập nhật thumbnail từ bài hát đầu tiên
            updateThumbnail(savedPlaylist.getId());
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

    // Read playlist with songs by ID
    @Cacheable(value = "playlists", key = "'with-songs-' + #id")
    public PlaylistDTO getPlaylistWithSongs(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + id));

        // Lấy danh sách bài hát từ SongPlaylist
        List<SongPlaylist> songPlaylists = songPlaylistRepository.findByPlaylistIdOrderByAddedAtDesc(id);
        List<SongPlaylistDTO> songPlaylistDTOs = songPlaylists.stream()
                .map(this::mapToSongPlaylistDTO)
                .collect(Collectors.toList());

        PlaylistDTO dto = mapToDTO(playlist);
        dto.setSongPlaylists(songPlaylistDTOs); // Thêm danh sách bài hát
        return dto;
    }

    // Read all with pagination
    @Cacheable(value = "playlists", key = "'all-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<PlaylistDTO> getAllPlaylists(Pageable pageable) {
        return playlistRepository.findAll(pageable).map(this::mapToDTO);
    }

    // Update playlist for user (no genre)
    @CachePut(value = "playlists", key = "#id")
    @CacheEvict(value = "searchedPlaylists", allEntries = true)
    public PlaylistDTO updatePlaylist(Long id, PlaylistRequestDTO playlistRequestDTO) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getId().equals(playlist.getCreatedBy().getId()) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new SecurityException("You do not have permission to update this playlist");
        }

        playlist.setName(playlistRequestDTO.getName());
        playlist.setDescription(playlistRequestDTO.getDescription());

        // Không xử lý genre cho playlist của user
        Playlist updatedPlaylist = playlistRepository.save(playlist);
        // Cập nhật thumbnail từ bài hát đầu tiên (nếu có)
        updateThumbnail(id);
        return mapToDTO(updatedPlaylist);
    }

    // Update playlist for admin with genres (auto-add songs)
    @CachePut(value = "playlists", key = "#id")
    @CacheEvict(value = "searchedPlaylists", allEntries = true)
    public PlaylistDTO updatePlaylistWithGenres(Long id, PlaylistRequestDTO playlistRequestDTO) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User currentUser = (User) authentication.getPrincipal();
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
            // Cập nhật thumbnail từ bài hát đầu tiên
            updateThumbnail(id);
        }

        Playlist updatedPlaylist = playlistRepository.save(playlist);
        return mapToDTO(updatedPlaylist);
    }

    // Delete
    @CacheEvict(value = {"playlists", "searchedPlaylists"}, allEntries = true)
    public void deletePlaylist(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getId().equals(playlist.getCreatedBy().getId()) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new SecurityException("You do not have permission to delete this playlist");
        }

        playlistRepository.delete(playlist);
    }

    // Search playlists
    @Cacheable(value = "searchedPlaylists", key = "#query + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
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
        dto.setThumbnail(playlist.getThumbnail()); // Thêm thumbnail
        dto.setCreatedAt(playlist.getCreatedAt());
        dto.setGenreIds(playlist.getGenres() != null ? playlist.getGenres().stream().map(Genre::getId).collect(Collectors.toList()) : null);
        dto.setUserId(playlist.getCreatedBy().getId());
        return dto;
    }

    // Map SongPlaylist to DTO
    private SongPlaylistDTO mapToSongPlaylistDTO(SongPlaylist songPlaylist) {
        SongPlaylistDTO dto = new SongPlaylistDTO();
        dto.setId(songPlaylist.getId());
        dto.setSongId(songPlaylist.getSong().getId());
        dto.setPlaylistId(songPlaylist.getPlaylist().getId());
        dto.setAddedAt(songPlaylist.getAddedAt());
        return dto;
    }

    // Phương thức cập nhật thumbnail từ bài hát đầu tiên
    public void updateThumbnail(Long playlistId) {
        List<SongPlaylist> songPlaylists = songPlaylistRepository.findByPlaylistIdOrderByAddedAtAsc(playlistId);
        if (!songPlaylists.isEmpty()) {
            Song firstSong = songPlaylists.get(0).getSong();
            Playlist playlist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + playlistId));
            playlist.setThumbnail(firstSong.getThumbnail()); // Giả định Song có trường thumbnail
            playlistRepository.save(playlist);
        }
    }
}