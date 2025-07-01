package com.music.application.be.modules.song;

import com.music.application.be.modules.album.Album;
import com.music.application.be.modules.album.AlbumRepository;
import com.music.application.be.modules.artist.Artist;
import com.music.application.be.modules.artist.ArtistRepository;
import com.music.application.be.modules.cloudinary.CloudinaryService;
import com.music.application.be.modules.downloaded_song.DownloadedSongRepository;
import com.music.application.be.modules.favorite_song.FavoriteSongRepository;
import com.music.application.be.modules.genre.Genre;
import com.music.application.be.modules.genre.GenreRepository;
import com.music.application.be.modules.playlist.Playlist;
import com.music.application.be.modules.playlist.PlaylistRepository;
import com.music.application.be.modules.song.dto.CreateSongDTO;
import com.music.application.be.common.PagedResponse;
import com.music.application.be.modules.song.dto.SongDTO;
import com.music.application.be.modules.song.dto.SongResponseDTO;
import com.music.application.be.modules.song.dto.UpdateSongDTO;
import com.music.application.be.modules.song_playlist.SongPlaylist;
import com.music.application.be.modules.song_playlist.SongPlaylistRepository;
import com.music.application.be.modules.user.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class SongService {

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private SongPlaylistRepository songPlaylistRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private FavoriteSongRepository favoriteSongRepository;

    @Autowired
    private DownloadedSongRepository downloadedSongRepository;
    // Create
    @CacheEvict(value = {"songs", "searchedSongs", "songsByGenre", "songsByArtist", "topSongs"}, allEntries = true)
    public SongDTO createSong(CreateSongDTO createSongDTO, MultipartFile audioFile, MultipartFile thumbnailFile) throws IOException {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new IllegalArgumentException("Audio file is required");
        }
        if (thumbnailFile == null || thumbnailFile.isEmpty()) {
            throw new IllegalArgumentException("Thumbnail file is required");
        }
        if (createSongDTO.getTitle() == null || createSongDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Song title is required");
        }

        Song song = new Song();
        song.setTitle(createSongDTO.getTitle());

        // Tạo file tạm cho audio
        File audioTempFile = createTempFile(audioFile);
        try {
            // Trích xuất duration từ file tạm
            int duration = extractDurationFromAudio(audioTempFile);
            song.setDuration(duration);

            // Upload file audio lên Cloudinary
            String audioUrl = cloudinaryService.uploadFile(audioTempFile, "video");
            song.setAudioUrl(audioUrl);
        } finally {
            deleteTempFile(audioTempFile);
        }

        // Upload file thumbnail lên Cloudinary
        String thumbnailUrl = cloudinaryService.uploadFile(thumbnailFile, "image");
        song.setThumbnail(thumbnailUrl);

        song.setLyrics(createSongDTO.getLyrics());
        song.setReleaseDate(createSongDTO.getReleaseDate());
        song.setViewCount(0);

        Artist artist = artistRepository.findById(createSongDTO.getArtistId())
                .orElseThrow(() -> new EntityNotFoundException("Artist not found with id: " + createSongDTO.getArtistId()));
        song.setArtist(artist);

        if (createSongDTO.getAlbumId() != null) {
            Album album = albumRepository.findById(createSongDTO.getAlbumId())
                    .orElseThrow(() -> new EntityNotFoundException("Album not found with id: " + createSongDTO.getAlbumId()));
            song.setAlbum(album);
        }

        if (createSongDTO.getGenreIds() != null && !createSongDTO.getGenreIds().isEmpty()) {
            List<Genre> genres = genreRepository.findAllById(createSongDTO.getGenreIds());
            if (genres.size() != createSongDTO.getGenreIds().size()) {
                throw new IllegalArgumentException("One or more genres not found");
            }
            song.setGenres(genres);
        }

        Song savedSong = songRepository.save(song);

        if (!song.getGenres().isEmpty()) {
            List<Playlist> matchingPlaylists = playlistRepository.findByGenresIn(song.getGenres());
            for (Playlist playlist : matchingPlaylists) {
                SongPlaylist songPlaylist = new SongPlaylist();
                songPlaylist.setSong(savedSong);
                songPlaylist.setPlaylist(playlist);
                songPlaylist.setAddedAt(LocalDateTime.now());
                songPlaylistRepository.save(songPlaylist);
            }
        }

        return mapToDTO(savedSong);
    }

    // Update
    @CachePut(value = "songs", key = "#id")
    @CacheEvict(value = {"searchedSongs", "songsByGenre", "songsByArtist", "topSongs"}, allEntries = true)
    public SongDTO updateSong(Long id, UpdateSongDTO updateSongDTO, MultipartFile audioFile, MultipartFile thumbnailFile) throws IOException {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Song not found with id: " + id));

        song.setTitle(updateSongDTO.getTitle());
        if (audioFile != null && !audioFile.isEmpty()) {
            File audioTempFile = createTempFile(audioFile);
            try {
                int duration = extractDurationFromAudio(audioTempFile);
                song.setDuration(duration);
                String audioUrl = cloudinaryService.uploadFile(audioTempFile, "video");
                song.setAudioUrl(audioUrl);
            } finally {
                deleteTempFile(audioTempFile);
            }
        }
        song.setLyrics(updateSongDTO.getLyrics());
        song.setReleaseDate(updateSongDTO.getReleaseDate());
        song.setViewCount(updateSongDTO.getViewCount() != null ? updateSongDTO.getViewCount() : song.getViewCount());

        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            String thumbnailUrl = cloudinaryService.uploadFile(thumbnailFile, "image");
            song.setThumbnail(thumbnailUrl);
        }

        Artist artist = artistRepository.findById(updateSongDTO.getArtistId())
                .orElseThrow(() -> new EntityNotFoundException("Artist not found with id: " + updateSongDTO.getArtistId()));
        song.setArtist(artist);

        if (updateSongDTO.getAlbumId() != null) {
            Album album = albumRepository.findById(updateSongDTO.getAlbumId())
                    .orElseThrow(() -> new EntityNotFoundException("Album not found with id: " + updateSongDTO.getAlbumId()));
            song.setAlbum(album);
        } else {
            song.setAlbum(null);
        }

        if (updateSongDTO.getGenreIds() != null) {
            List<Genre> genres = genreRepository.findAllById(updateSongDTO.getGenreIds());
            if (genres.size() != updateSongDTO.getGenreIds().size()) {
                throw new IllegalArgumentException("One or more genres not found");
            }
            song.setGenres(genres);
        } else {
            song.setGenres(null);
        }

        List<SongPlaylist> existingSongPlaylists = songPlaylistRepository.findBySongId(song.getId());
        songPlaylistRepository.deleteAll(existingSongPlaylists);

        if (song.getGenres() != null && !song.getGenres().isEmpty()) {
            List<Playlist> matchingPlaylists = playlistRepository.findByGenresIn(song.getGenres());
            for (Playlist playlist : matchingPlaylists) {
                SongPlaylist songPlaylist = new SongPlaylist();
                songPlaylist.setSong(song);
                songPlaylist.setPlaylist(playlist);
                songPlaylist.setAddedAt(LocalDateTime.now());
                songPlaylistRepository.save(songPlaylist);
            }
        }

        Song updatedSong = songRepository.save(song);
        return mapToDTO(updatedSong);
    }

    // Update thumbnail only
    @CachePut(value = "songs", key = "#id")
    @CacheEvict(value = {"searchedSongs", "songsByGenre", "songsByArtist", "topSongs"}, allEntries = true)
    public SongDTO updateSongThumbnail(Long id, MultipartFile thumbnailFile) throws IOException {
        if (thumbnailFile == null || thumbnailFile.isEmpty()) {
            throw new IllegalArgumentException("Thumbnail file is required");
        }

        Song song = songRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Song not found with id: " + id));

        // Upload file thumbnail lên Cloudinary
        String thumbnailUrl = cloudinaryService.uploadFile(thumbnailFile, "image");
        song.setThumbnail(thumbnailUrl);

        Song updatedSong = songRepository.save(song);
        return mapToDTO(updatedSong);
    }

    // Read by ID
    @Cacheable(value = "songs", key = "#id")
    public SongResponseDTO getSongById(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Song not found with id: " + id));
        song.setViewCount(song.getViewCount() + 1);
        songRepository.save(song);
        return mapToResponseDTO(song);
    }

    // Read all with pagination
    @Cacheable(value = "allSongs", key = "'page-' + #pageable.pageNumber + '-size-' + #pageable.pageSize")
    public PagedResponse<SongResponseDTO> getAllSongs(Pageable pageable) {
        Page<Song> page = songRepository.findAll(pageable);
        List<SongResponseDTO> content = page.getContent().stream()
                .map(this::mapToResponseDTO)
                .toList();

        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    // Delete
    @CacheEvict(value = {"songs", "searchedSongs", "songsByGenre", "songsByArtist", "topSongs"}, allEntries = true)
    public void deleteSong(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Song not found with id: " + id));
        songRepository.delete(song);
    }

    // Search songs
    @Cacheable(value = "searchedSongs", key = "'query-' + #query + '-page-' + #page + '-size-' + #size")
    public PagedResponse<SongDTO> searchSongs(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Song> songPage = songRepository.findByTitleContainingIgnoreCase(query, pageable);

        return new PagedResponse<>(
                mapToDTOList(songPage.getContent()),
                songPage.getNumber(),
                songPage.getSize(),
                songPage.getTotalElements(),
                songPage.getTotalPages(),
                songPage.isLast()
        );
    }


    // Get songs by genre
    @Cacheable(value = "songsByGenre", key = "'genre-' + #genreId + '-page-' + #page + '-size-' + #size")
    public PagedResponse<SongDTO> getSongsByGenre(Long genreId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Song> songPage = songRepository.findByGenresId(genreId, pageable);

        return new PagedResponse<>(
                mapToDTOList(songPage.getContent()),
                songPage.getNumber(),
                songPage.getSize(),
                songPage.getTotalElements(),
                songPage.getTotalPages(),
                songPage.isLast()
        );
    }
    // Get songs by artist
    @Cacheable(value = "songsByArtist", key = "'artist-' + #artistId + '-page-' + #page + '-size-' + #size")
    public PagedResponse<SongDTO> getSongsByArtist(Long artistId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Song> songPage = songRepository.findByArtistId(artistId, pageable);

        return new PagedResponse<>(
                mapToDTOList(songPage.getContent()),
                songPage.getNumber(),
                songPage.getSize(),
                songPage.getTotalElements(),
                songPage.getTotalPages(),
                songPage.isLast()
        );
    }


    // Get songs by album
    @Cacheable(value = "songsByAlbum", key = "'album-' + #albumId + '-page-' + #page + '-size-' + #size")
    public PagedResponse<SongDTO> getSongsByAlbumId(Long albumId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Song> songPage = songRepository.findByAlbumId(albumId, pageable);

        return new PagedResponse<>(
                mapToDTOList(songPage.getContent()),
                songPage.getNumber(),
                songPage.getSize(),
                songPage.getTotalElements(),
                songPage.getTotalPages(),
                songPage.isLast()
        );
    }


    // Get top songs by view count
    @Cacheable(value = "topSongs", key = "'top-page-' + #page + '-size-' + #size")
    public PagedResponse<SongDTO> getTopSongsByViewCount(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Song> songPage = songRepository.findAllByOrderByViewCountDesc(pageable);

        return new PagedResponse<>(
                mapToDTOList(songPage.getContent()),
                songPage.getNumber(),
                songPage.getSize(),
                songPage.getTotalElements(),
                songPage.getTotalPages(),
                songPage.isLast()
        );
    }


    // Share song
    @Cacheable(value = "songs", key = "'share-' + #id")
    public String shareSong(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Song not found with id: " + id));
        return "http://localhost:8080/api/songs/" + id;
    }

    private List<SongDTO> mapToDTOList(List<Song> songs) {
        return songs.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public SongResponseDTO mapToResponseDTO(Song song) {
        SongResponseDTO songDTO = new SongResponseDTO();
        songDTO.setId(song.getId());
        songDTO.setTitle(song.getTitle());
        songDTO.setDuration(song.getDuration());
        songDTO.setAudioUrl(song.getAudioUrl());
        songDTO.setThumbnail(song.getThumbnail());
        songDTO.setLyrics(song.getLyrics());
        songDTO.setReleaseDate(song.getReleaseDate());
        songDTO.setViewCount(song.getViewCount());

        if (song.getArtist() != null) {
            songDTO.setArtistId(song.getArtist().getId());
            songDTO.setArtistName(song.getArtist().getName());
        }

        if (song.getAlbum() != null) {
            songDTO.setAlbumId(song.getAlbum().getId());
            songDTO.setAlbumName(song.getAlbum().getName());
        }

        songDTO.setGenreIds(song.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toList()));

        // Lấy thông tin người dùng từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Long userId = ((User) userDetails).getId();

            boolean isFavorite = favoriteSongRepository.existsByUser_IdAndSong_Id(userId, song.getId());
            boolean isDownloaded = downloadedSongRepository.existsByUser_IdAndSong_Id(userId, song.getId());

            songDTO.setFavorite(isFavorite);
            songDTO.setDownloaded(isDownloaded);
        }

        return songDTO;
    }


    private SongDTO mapToDTO(Song song) {
        SongDTO songDTO = new SongDTO();
        songDTO.setId(song.getId());
        songDTO.setTitle(song.getTitle());
        songDTO.setDuration(song.getDuration());
        songDTO.setAudioUrl(song.getAudioUrl());
        songDTO.setThumbnail(song.getThumbnail());
        songDTO.setLyrics(song.getLyrics());
        songDTO.setReleaseDate(song.getReleaseDate());
        songDTO.setViewCount(song.getViewCount());

        if (song.getArtist() != null) {
            songDTO.setArtistId(song.getArtist().getId());
            songDTO.setArtistName(song.getArtist().getName());
        }

        if (song.getAlbum() != null) {
            songDTO.setAlbumId(song.getAlbum().getId());
            songDTO.setAlbumName(song.getAlbum().getName());
        }

        songDTO.setGenreIds(song.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toList()));
        return songDTO;
    }

    private File createTempFile(MultipartFile file) throws IOException {
        String tempFileName = "audio_" + UUID.randomUUID().toString() + "." +
                getFileExtension(file.getOriginalFilename());
        File tempFile = new File(System.getProperty("java.io.tmpdir"), tempFileName);

        File parentDir = tempFile.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Failed to create temporary directory: " + parentDir.getAbsolutePath());
        }

        file.transferTo(tempFile);

        if (!tempFile.exists()) {
            throw new IOException("Temporary audio file does not exist: " + tempFile.getAbsolutePath());
        }
        if (!tempFile.canRead()) {
            throw new IOException("Cannot read temporary audio file: " + tempFile.getAbsolutePath());
        }

        System.out.println("Created temp file at: " + tempFile.getAbsolutePath());
        return tempFile;
    }

    private void deleteTempFile(File tempFile) {
        if (tempFile != null && tempFile.exists()) {
            boolean deleted = tempFile.delete();
            if (!deleted) {
                System.err.println("Failed to delete temporary file: " + tempFile.getAbsolutePath());
            } else {
                System.out.println("Temporary file deleted: " + tempFile.getAbsolutePath());
            }
        }
    }

    private int extractDurationFromAudio(File tempFile) throws IOException {
        try {
            org.jaudiotagger.audio.AudioFile audio = org.jaudiotagger.audio.AudioFileIO.read(tempFile);
            int durationInSeconds = audio.getAudioHeader().getTrackLength();
            System.out.println("Extracted duration: " + durationInSeconds + " seconds");
            return durationInSeconds;
        } catch (org.jaudiotagger.audio.exceptions.CannotReadException e) {
            throw new IOException("Cannot read audio file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IOException("Error extracting duration from audio file: " + e.getMessage(), e);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "mp3";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}