package com.music.application.be.modules.song_playlist;

import com.music.application.be.modules.playlist.Playlist;
import com.music.application.be.modules.playlist.PlaylistRepository;
import com.music.application.be.modules.playlist.PlaylistService;
import com.music.application.be.modules.song.Song;
import com.music.application.be.modules.song.SongRepository;
import com.music.application.be.common.PagedResponse;
import com.music.application.be.modules.song_playlist.dto.SongPlaylistDTO;
import com.music.application.be.modules.song_playlist.dto.SongPlaylistRequestDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.music.application.be.common.PaginationUtils.buildPagedResponse;

@Service
public class SongPlaylistService {

    @Autowired
    private SongPlaylistRepository songPlaylistRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private PlaylistService playlistService; // Thêm dependency

    // Add song to playlist
    @CacheEvict(value = {"playlists", "searchedPlaylists"}, key = "#requestDTO.playlistId")
    public SongPlaylistDTO addSongToPlaylist(SongPlaylistRequestDTO requestDTO) {
        Song song = songRepository.findById(requestDTO.getSongId())
                .orElseThrow(() -> new EntityNotFoundException("Song not found with id: " + requestDTO.getSongId()));
        Playlist playlist = playlistRepository.findById(requestDTO.getPlaylistId())
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + requestDTO.getPlaylistId()));

        // Kiểm tra xem bài hát đã có trong playlist chưa
        boolean exists = songPlaylistRepository.findByPlaylistIdOrderByAddedAtDesc(playlist.getId())
                .stream()
                .anyMatch(sp -> sp.getSong().getId().equals(song.getId()));
        if (exists) {
            throw new IllegalStateException("Song with id " + song.getId() + " already exists in playlist with id " + playlist.getId());
        }

        SongPlaylist songPlaylist = new SongPlaylist();
        songPlaylist.setSong(song);
        songPlaylist.setPlaylist(playlist);
        songPlaylist.setAddedAt(LocalDateTime.now());

        SongPlaylist savedSongPlaylist = songPlaylistRepository.save(songPlaylist);

        // Kiểm tra và cập nhật thumbnail nếu playlist trước đó trống
        long songCount = songPlaylistRepository.countByPlaylistId(playlist.getId());
        if (songCount == 1) { // Sau khi thêm, playlist chỉ có 1 bài hát, tức là trước đó trống
            playlistService.updateThumbnail(playlist.getId());
        }

        return mapToDTO(savedSongPlaylist);
    }

    // Update song or playlist in SongPlaylist
    @CachePut(value = "songPlaylists", key = "#id")
    @CacheEvict(value = {"playlists", "searchedPlaylists"}, allEntries = true)
    public SongPlaylistDTO updateSongPlaylist(Long id, SongPlaylistRequestDTO requestDTO) {
        SongPlaylist songPlaylist = songPlaylistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SongPlaylist not found with id: " + id));

        // Cập nhật song nếu có thay đổi
        if (!songPlaylist.getSong().getId().equals(requestDTO.getSongId())) {
            Song newSong = songRepository.findById(requestDTO.getSongId())
                    .orElseThrow(() -> new EntityNotFoundException("Song not found with id: " + requestDTO.getSongId()));
            // Kiểm tra xem bài hát mới đã có trong playlist chưa
            boolean exists = songPlaylistRepository.findByPlaylistIdOrderByAddedAtDesc(songPlaylist.getPlaylist().getId())
                    .stream()
                    .anyMatch(sp -> sp.getSong().getId().equals(newSong.getId()) && !sp.getId().equals(id));
            if (exists) {
                throw new IllegalStateException("Song with id " + newSong.getId() + " already exists in playlist with id " + songPlaylist.getPlaylist().getId());
            }
            songPlaylist.setSong(newSong);
        }

        // Cập nhật playlist nếu có thay đổi
        if (!songPlaylist.getPlaylist().getId().equals(requestDTO.getPlaylistId())) {
            Playlist newPlaylist = playlistRepository.findById(requestDTO.getPlaylistId())
                    .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + requestDTO.getPlaylistId()));
            // Kiểm tra xem bài hát đã có trong playlist mới chưa
            boolean exists = songPlaylistRepository.findByPlaylistIdOrderByAddedAtDesc(newPlaylist.getId())
                    .stream()
                    .anyMatch(sp -> sp.getSong().getId().equals(songPlaylist.getSong().getId()));
            if (exists) {
                throw new IllegalStateException("Song with id " + songPlaylist.getSong().getId() + " already exists in playlist with id " + newPlaylist.getId());
            }
            songPlaylist.setPlaylist(newPlaylist);
        }

        // Tự động cập nhật addedAt
        songPlaylist.setAddedAt(LocalDateTime.now());

        SongPlaylist updatedSongPlaylist = songPlaylistRepository.save(songPlaylist);
        return mapToDTO(updatedSongPlaylist);
    }

    // Remove song from playlist
    @CacheEvict(value = {"songPlaylists", "playlists", "searchedPlaylists"}, allEntries = true)
    public void removeSongFromPlaylist(Long id) {
        SongPlaylist songPlaylist = songPlaylistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SongPlaylist not found with id: " + id));
        Long playlistId = songPlaylist.getPlaylist().getId();
        songPlaylistRepository.delete(songPlaylist);

        // Nếu sau khi xóa, playlist trở thành trống, cập nhật thumbnail thành null
        long remainingSongs = songPlaylistRepository.countByPlaylistId(playlistId);
        if (remainingSongs == 0) {
            Playlist playlist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new EntityNotFoundException("Playlist not found with id: " + playlistId));
            playlist.setThumbnail(null);
            playlistRepository.save(playlist);
        }
    }

    // Get all song playlists với pagination để admin theo dõi dữ liệu
    @Cacheable(value = "allSongPlaylists", key = "'page-' + #pageable.pageNumber + '-size-' + #pageable.pageSize")
    public PagedResponse<SongPlaylistDTO> getAllSongPlaylists(Pageable pageable) {
        Page<SongPlaylist> page = songPlaylistRepository.findAll(pageable);
        List<SongPlaylistDTO> content = page.getContent()
                .stream()
                .map(this::mapToDTO)
                .toList();

        return buildPagedResponse(content, page);
    }

    private SongPlaylistDTO mapToDTO(SongPlaylist songPlaylist) {
        SongPlaylistDTO dto = new SongPlaylistDTO();
        dto.setId(songPlaylist.getId());
        dto.setSongId(songPlaylist.getSong().getId());
        dto.setPlaylistId(songPlaylist.getPlaylist().getId());
        dto.setAddedAt(songPlaylist.getAddedAt());
        return dto;
    }

}