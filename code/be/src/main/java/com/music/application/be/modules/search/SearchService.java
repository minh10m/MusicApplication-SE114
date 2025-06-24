package com.music.application.be.modules.search;

import com.music.application.be.modules.album.Album;
import com.music.application.be.modules.album.AlbumRepository;
import com.music.application.be.modules.album.dto.AlbumDTO;
import com.music.application.be.modules.artist.Artist;
import com.music.application.be.modules.artist.ArtistRepository;
import com.music.application.be.modules.artist.dto.ArtistDTO;
import com.music.application.be.modules.playlist.Playlist;
import com.music.application.be.modules.playlist.PlaylistRepository;
import com.music.application.be.modules.playlist.dto.PlaylistDTO;
import com.music.application.be.modules.search.dto.GlobalSearchResultDTO;
import com.music.application.be.modules.song.Song;
import com.music.application.be.modules.song.SongRepository;
import com.music.application.be.modules.song.dto.SongDTO;
import com.music.application.be.modules.user.User;
import com.music.application.be.modules.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private UserRepository userRepository;

    // Global search - tìm kiếm tất cả các entity
    public GlobalSearchResultDTO globalSearch(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return new GlobalSearchResultDTO();
        }

        String searchQuery = query.trim();
        Pageable pageable = PageRequest.of(0, limit);

        // Tìm kiếm songs
        List<Song> songs = songRepository.findByTitleContainingIgnoreCase(searchQuery, pageable).getContent();
        List<SongDTO> songDTOs = songs.stream().map(this::mapSongToDTO).collect(Collectors.toList());

        // Tìm kiếm playlists (chỉ public hoặc của user hiện tại)
        List<Playlist> playlists = getFilteredPlaylists(searchQuery, pageable);
        List<PlaylistDTO> playlistDTOs = playlists.stream().map(this::mapPlaylistToDTO).collect(Collectors.toList());

        // Tìm kiếm artists
        List<Artist> artists = artistRepository.findByNameContainingIgnoreCase(searchQuery, pageable).getContent();
        List<ArtistDTO> artistDTOs = artists.stream().map(this::mapArtistToDTO).collect(Collectors.toList());

        // Tìm kiếm albums
        List<Album> albums = albumRepository.findByNameContainingIgnoreCase(searchQuery, pageable).getContent();
        List<AlbumDTO> albumDTOs = albums.stream().map(this::mapAlbumToDTO).collect(Collectors.toList());

        // Tạo result
        GlobalSearchResultDTO result = new GlobalSearchResultDTO();
        result.setSongs(songDTOs);
        result.setPlaylists(playlistDTOs);
        result.setArtists(artistDTOs);
        result.setAlbums(albumDTOs);
        result.setTotalResults((long) (songDTOs.size() + playlistDTOs.size() + artistDTOs.size() + albumDTOs.size()));

        return result;
    }    // Lấy playlist được filter theo quyền
    private List<Playlist> getFilteredPlaylists(String query, Pageable pageable) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User) {
                User currentUser = (User) authentication.getPrincipal();
                
                // Admin có thể search tất cả playlist
                if (currentUser.getRole().name().equals("ADMIN")) {
                    return playlistRepository.findByNameContainingIgnoreCase(query, pageable).getContent();
                } else {
                    // User chỉ search playlist public hoặc của mình
                    return playlistRepository.findByNameContainingIgnoreCaseAndIsPublicTrueOrCreatedBy(query, currentUser, pageable).getContent();
                }
            } else {
                // Không đăng nhập chỉ search playlist public
                return playlistRepository.findByNameContainingIgnoreCaseAndIsPublicTrue(query, pageable).getContent();
            }
        } catch (Exception e) {
            // Fallback: chỉ trả về playlist public
            return playlistRepository.findByNameContainingIgnoreCaseAndIsPublicTrue(query, pageable).getContent();
        }
    }// Mapping methods
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
    }private PlaylistDTO mapPlaylistToDTO(Playlist playlist) {
        PlaylistDTO dto = new PlaylistDTO();
        dto.setId(playlist.getId());
        dto.setUserId(playlist.getCreatedBy() != null ? playlist.getCreatedBy().getId() : null);
        dto.setName(playlist.getName());
        dto.setDescription(playlist.getDescription());
        dto.setThumbnail(playlist.getThumbnail());
        dto.setCreatedAt(playlist.getCreatedAt());
        dto.setIsPublic(playlist.getIsPublic()); // Thêm isPublic
        dto.setGenreIds(playlist.getGenres() != null ? playlist.getGenres().stream().map(genre -> genre.getId()).collect(Collectors.toList()) : null);
        return dto;
    }

    private ArtistDTO mapArtistToDTO(Artist artist) {
        ArtistDTO dto = new ArtistDTO();
        dto.setId(artist.getId());
        dto.setName(artist.getName());
        dto.setAvatar(artist.getAvatar());
        dto.setDescription(artist.getDescription());
        dto.setFollowerCount(artist.getFollowerCount());
        return dto;
    }

    private AlbumDTO mapAlbumToDTO(Album album) {
        AlbumDTO dto = new AlbumDTO();
        dto.setId(album.getId());
        dto.setName(album.getName());
        dto.setReleaseDate(album.getReleaseDate());
        dto.setCoverImage(album.getCoverImage());
        dto.setDescription(album.getDescription());
        dto.setArtistId(album.getArtist() != null ? album.getArtist().getId() : null);
        dto.setArtistName(album.getArtist() != null ? album.getArtist().getName() : null);
        return dto;
    }
}
