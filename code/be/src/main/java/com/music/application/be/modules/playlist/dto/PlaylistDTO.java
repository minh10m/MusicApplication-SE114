package com.music.application.be.modules.playlist.dto;

import com.music.application.be.modules.song_playlist.dto.SongPlaylistDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PlaylistDTO {
    private Long id;

    private Long userId; // Thêm userId để trả về thông tin người tạo

    @NotBlank(message = "Name is mandatory")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    private String thumbnail;

    private LocalDateTime createdAt;

    private Boolean isPublic; // Thêm trường isPublic

    private List<Long> genreIds;

    private List<SongPlaylistDTO> songPlaylists;

}