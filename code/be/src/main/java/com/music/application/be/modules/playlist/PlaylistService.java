package com.music.application.be.modules.playlist;

import com.music.application.be.common.PagedResponse;
import com.music.application.be.common.PaginationUtils;
import com.music.application.be.modules.genre.Genre;
import com.music.application.be.modules.genre.GenreRepository;
import com.music.application.be.modules.playlist.dto.PlaylistDTO;
import com.music.application.be.modules.playlist.dto.PlaylistRequestDTO;
import com.music.application.be.modules.role.Role;
import com.music.application.be.modules.song.Song;
import com.music.application.be.modules.song.SongRepository;
import com.music.application.be.modules.song.dto.SongDTO;
import com.music.application.be.modules.song_playlist.SongPlaylist;
import com.music.application.be.modules.song_playlist.SongPlaylistRepository;
import com.music.application.be.modules.song_playlist.dto.SongPlaylistDTO;
import com.music.application.be.modules.user.User;
import com.music.application.be.modules.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
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
    public PlaylistDTO createPlaylist(PlaylistRequestDTO playlistRequestDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }        User user = (User) authentication.getPrincipal();

        Playlist playlist = new Playlist();
        playlist.setName(playlistRequestDTO.getName());
        playlist.setDescription(playlistRequestDTO.getDescription());
        playlist.setIsPublic(playlistRequestDTO.getIsPublic() != null ? playlistRequestDTO.getIsPublic() : false);
        playlist.setCreatedAt(LocalDateTime.now());
        playlist.setCreatedBy(user);

        Playlist savedPlaylist = playlistRepository.save(playlist);
        // Cập nhật thumbnail từ bài hát đầu tiên (nếu có)
        updateThumbnail(savedPlaylist.getId());
        return mapToDTO(savedPlaylist);
    }    // Create playlist for admin with genres (auto-add songs)
    public PlaylistDTO createPlaylistWithGenres(PlaylistRequestDTO playlistRequestDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }        User user = (User) authentication.getPrincipal();        Playlist playlist = new Playlist();
        playlist.setName(playlistRequestDTO.getName());
        playlist.setDescription(playlistRequestDTO.getDescription());
        // Bỏ qua isPublic từ request, luôn set thành true cho playlist có genre
        playlist.setIsPublic(true);
        playlist.setCreatedAt(LocalDateTime.now());
        playlist.setCreatedBy(user);

        if (playlistRequestDTO.getGenreIds() != null && !playlistRequestDTO.getGenreIds().isEmpty()) {
            List<Genre> genres = genreRepository.findAllById(playlistRequestDTO.getGenreIds());
            if (genres.size() != playlistRequestDTO.getGenreIds().size()) {
                throw new EntityNotFoundException("One or more genres not found");
            }
            
            // Kiểm tra trùng genre - không cho phép tạo playlist mới với genre đã tồn tại
            for (Genre genre : genres) {
                Page<Playlist> existingPlaylists = playlistRepository.findByGenresId(genre.getId(), Pageable.unpaged());
                if (!existingPlaylists.isEmpty()) {
                    throw new IllegalArgumentException("Playlist with genre '" + genre.getName() + "' already exists");
                }
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
    }    // Read by ID

    @Cacheable(value = "playlists", key = "#id")
    public PlaylistDTO getPlaylistById(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + id));
        
        // Kiểm tra quyền truy cập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            // Admin có thể xem tất cả, user có thể xem public playlist hoặc playlist của mình
            if (!currentUser.getRole().equals(Role.ADMIN) && !playlist.getIsPublic() && !playlist.getCreatedBy().getId().equals(currentUser.getId())) {
                throw new SecurityException("You do not have permission to view this playlist");
            }
        } else {
            // Guest chỉ có thể xem public playlist
            if (!playlist.getIsPublic()) {
                throw new SecurityException("You do not have permission to view this playlist");
            }
        }
        
        return mapToDTO(playlist);
    }    // Read playlist with songs by ID

    @Cacheable(value = "playlistWithSongs", key = "#id")
    public PlaylistDTO getPlaylistWithSongs(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + id));

        // Kiểm tra quyền truy cập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            // Admin có thể xem tất cả, user có thể xem public playlist hoặc playlist của mình
            if (!currentUser.getRole().equals(Role.ADMIN) && !playlist.getIsPublic() && !playlist.getCreatedBy().getId().equals(currentUser.getId())) {
                throw new SecurityException("You do not have permission to view this playlist");
            }
        } else {
            // Guest chỉ có thể xem public playlist
            if (!playlist.getIsPublic()) {
                throw new SecurityException("You do not have permission to view this playlist");
            }
        }

        // Lấy danh sách bài hát từ SongPlaylist
        List<SongPlaylist> songPlaylists = songPlaylistRepository.findByPlaylistIdOrderByAddedAtDesc(id);
        List<SongPlaylistDTO> songPlaylistDTOs = songPlaylists.stream()
                .map(this::mapToSongPlaylistDTO)
                .collect(Collectors.toList());

        PlaylistDTO dto = mapToDTO(playlist);
        dto.setSongPlaylists(songPlaylistDTOs); // Thêm danh sách bài hát
        return dto;
    }    // Read all with pagination

    @Cacheable(
            value = "allPlaylists",
            key = "'page-' + #pageable.pageNumber + '-size-' + #pageable.pageSize + '-sort-' + #pageable.sort.toString() + '-' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication()?.getPrincipal()?.getClass()?.getSimpleName()"
    )
    public PagedResponse<PlaylistDTO> getAllPlaylists(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Page<PlaylistDTO> pageResult;
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User user) {
            if (user.getRole().equals(Role.ADMIN)) {
                pageResult = playlistRepository.findAll(pageable).map(this::mapToDTO);
            } else {
                pageResult = playlistRepository.findByIsPublicTrueOrCreatedBy(user, pageable).map(this::mapToDTO);
            }
        } else {
            pageResult = playlistRepository.findByIsPublicTrue(pageable).map(this::mapToDTO);
        }

        return PaginationUtils.buildPagedResponse(pageResult.getContent(), pageResult);
    }


    // Update playlist for user (no genre)

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
        }        playlist.setName(playlistRequestDTO.getName());
        playlist.setDescription(playlistRequestDTO.getDescription());
        if (playlistRequestDTO.getIsPublic() != null) {
            playlist.setIsPublic(playlistRequestDTO.getIsPublic());
        }

        // Không xử lý genre cho playlist của user
        Playlist updatedPlaylist = playlistRepository.save(playlist);
        // Cập nhật thumbnail từ bài hát đầu tiên (nếu có)
        updateThumbnail(id);
        return mapToDTO(updatedPlaylist);
    }    // Update playlist for admin with genres (auto-add songs)

    public PlaylistDTO updatePlaylistWithGenres(Long id, PlaylistRequestDTO playlistRequestDTO) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }
        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getId().equals(playlist.getCreatedBy().getId()) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new SecurityException("You do not have permission to update this playlist");        }        
        // Kiểm tra trùng genre trước khi update - không cho phép update nếu có genre trùng
        if (playlistRequestDTO.getGenreIds() != null && !playlistRequestDTO.getGenreIds().isEmpty()) {
            List<Genre> genres = genreRepository.findAllById(playlistRequestDTO.getGenreIds());
            if (genres.size() != playlistRequestDTO.getGenreIds().size()) {
                throw new EntityNotFoundException("One or more genres not found");
            }
            
            // Kiểm tra trùng genre - không cho phép cập nhật với genre đã tồn tại (trừ playlist hiện tại)
            for (Genre genre : genres) {
                Page<Playlist> existingPlaylists = playlistRepository.findByGenresId(genre.getId(), Pageable.unpaged());
                for (Playlist existingPlaylist : existingPlaylists) {
                    if (!existingPlaylist.getId().equals(id)) {
                        throw new IllegalArgumentException("Cannot update playlist: Genre '" + genre.getName() + "' is already used by another playlist");
                    }
                }
            }
        }
        
        // Chỉ update nếu không có genre trùng
        playlist.setName(playlistRequestDTO.getName());
        playlist.setDescription(playlistRequestDTO.getDescription());
        // Bỏ qua isPublic từ request, luôn set thành true cho playlist có genre
        playlist.setIsPublic(true);        if (playlistRequestDTO.getGenreIds() != null && !playlistRequestDTO.getGenreIds().isEmpty()) {
            // Genres đã được validate ở trên, chỉ cần set lại
            List<Genre> genres = genreRepository.findAllById(playlistRequestDTO.getGenreIds());
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
    }    // Search playlists
    @Cacheable(
            value = "searchedPlaylists",
            key = "'search-' + #query + '-page-' + #pageable.pageNumber + '-size-' + #pageable.pageSize"
    )
    public PagedResponse<PlaylistDTO> searchPlaylists(String query, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Page<Playlist> page;

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();

            if (currentUser.getRole().equals(Role.ADMIN)) {
                page = playlistRepository.findByNameContainingIgnoreCase(query, pageable);
            } else {
                page = playlistRepository.findByNameContainingIgnoreCaseAndIsPublicTrueOrCreatedBy(query, currentUser, pageable);
            }
        } else {
            page = playlistRepository.findByNameContainingIgnoreCaseAndIsPublicTrue(query, pageable);
        }

        List<PlaylistDTO> dtoList = page.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return PaginationUtils.buildPagedResponse(dtoList, page);
    }

    // Get playlists by genre (playlist có genre luôn là public)

    @Cacheable(
            value = "searchedPlaylists",
            key = "'genre-' + #genreId + '-page-' + #pageable.pageNumber + '-size-' + #pageable.pageSize"
    )
    public PagedResponse<PlaylistDTO> getPlaylistsByGenre(Long genreId, Pageable pageable) {
        Page<Playlist> playlistPage = playlistRepository.findByGenresId(genreId, pageable);
        List<PlaylistDTO> dtoList = playlistPage.stream()
                .map(this::mapToDTO)
                .toList();
        return PaginationUtils.buildPagedResponse(dtoList, playlistPage);
    }


    // Share playlist
    public String sharePlaylist(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + id));
        
        // Kiểm tra quyền truy cập - chỉ có thể share public playlist hoặc playlist của mình
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            // Admin có thể share tất cả, user có thể share public playlist hoặc playlist của mình
            if (!currentUser.getRole().equals(Role.ADMIN) && !playlist.getIsPublic() && !playlist.getCreatedBy().getId().equals(currentUser.getId())) {
                throw new SecurityException("You do not have permission to share this playlist");
            }
        } else {
            // Guest chỉ có thể share public playlist
            if (!playlist.getIsPublic()) {
                throw new SecurityException("You do not have permission to share this playlist");
            }
        }
        
        return "http://localhost:8080/api/playlists/" + id;
    }

    // Get user's own playlists
    @Cacheable(
            value = "myPlaylists",
            key = "'user-' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getPrincipal().id + '-page-' + #pageable.pageNumber + '-size-' + #pageable.pageSize"
    )
    public PagedResponse<PlaylistDTO> getMyPlaylists(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new EntityNotFoundException("User not authenticated");
        }

        User currentUser = (User) authentication.getPrincipal();

        Page<Playlist> page = playlistRepository.findByCreatedBy(currentUser, pageable);
        List<PlaylistDTO> dtoList = page.stream()
                .map(this::mapToDTO)
                .toList();

        return PaginationUtils.buildPagedResponse(dtoList, page);
    }
    // Map entity to DTO

    private PlaylistDTO mapToDTO(Playlist playlist) {
        PlaylistDTO dto = new PlaylistDTO();
        dto.setId(playlist.getId());
        dto.setName(playlist.getName());
        dto.setDescription(playlist.getDescription());
        dto.setThumbnail(playlist.getThumbnail()); // Thêm thumbnail
        dto.setCreatedAt(playlist.getCreatedAt());
        dto.setIsPublic(playlist.getIsPublic()); // Thêm isPublic
        dto.setGenreIds(playlist.getGenres() != null ? playlist.getGenres().stream().map(Genre::getId).collect(Collectors.toList()) : null);
        dto.setUserId(playlist.getCreatedBy().getId());
        return dto;
    }// Map SongPlaylist to DTO
    private SongPlaylistDTO mapToSongPlaylistDTO(SongPlaylist songPlaylist) {
        SongPlaylistDTO dto = new SongPlaylistDTO();
        dto.setId(songPlaylist.getId());
        dto.setSongId(songPlaylist.getSong().getId());
        dto.setPlaylistId(songPlaylist.getPlaylist().getId());
        dto.setAddedAt(songPlaylist.getAddedAt());
        dto.setSong(mapSongToDTO(songPlaylist.getSong())); // Thêm thông tin chi tiết của song
        return dto;
    }    // Map Song to DTO
    private SongDTO mapSongToDTO(Song song) {
        SongDTO dto = new SongDTO();
        dto.setId(song.getId());
        dto.setTitle(song.getTitle());
        dto.setDuration(song.getDuration());
        dto.setAudioUrl(song.getAudioUrl());
        dto.setThumbnail(song.getThumbnail());
        dto.setLyrics(song.getLyrics());
        dto.setReleaseDate(song.getReleaseDate());
        dto.setViewCount(song.getViewCount());
        dto.setArtistId(song.getArtist() != null ? song.getArtist().getId() : null);
        dto.setArtistName(song.getArtist() != null ? song.getArtist().getName() : null); // Thêm tên artist
        dto.setAlbumId(song.getAlbum() != null ? song.getAlbum().getId() : null);
        dto.setAlbumName(song.getAlbum() != null ? song.getAlbum().getName() : null); // Thêm tên album
        dto.setGenreIds(song.getGenres() != null ? song.getGenres().stream().map(genre -> genre.getId()).collect(Collectors.toList()) : null);
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