package com.music.application.be.modules.song.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SongResponseDTO {
    private Long id;

    @NotBlank(message = "Title is mandatory")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    private Integer duration;
    private String audioUrl;
    private String thumbnail;
    private String lyrics;
    private LocalDate releaseDate;
    private Integer viewCount;
    private Long artistId;
    private String artistName; // Thêm tên artist
    private Long albumId;
    private String albumName; // Thêm tên album
    private List<Long> genreIds;

    @JsonProperty("isFavorite")
    @JsonAlias("favorite")
    private boolean isFavorite;

    @JsonProperty("isDownloaded")
    @JsonAlias("downloaded")
    private boolean isDownloaded;
}