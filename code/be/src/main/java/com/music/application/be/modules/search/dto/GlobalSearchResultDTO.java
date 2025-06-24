package com.music.application.be.modules.search.dto;

import com.music.application.be.modules.album.dto.AlbumDTO;
import com.music.application.be.modules.artist.dto.ArtistDTO;
import com.music.application.be.modules.playlist.dto.PlaylistDTO;
import com.music.application.be.modules.song.dto.SongDTO;
import lombok.Data;

import java.util.List;

@Data
public class GlobalSearchResultDTO {
    private List<SongDTO> songs;
    private List<PlaylistDTO> playlists;
    private List<ArtistDTO> artists;
    private List<AlbumDTO> albums;
    private Long totalResults;
}
