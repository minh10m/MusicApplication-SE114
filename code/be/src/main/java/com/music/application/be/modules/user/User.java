package com.music.application.be.modules.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.music.application.be.modules.artist.Artist;
import com.music.application.be.modules.forget_password.ForgetPassword;
import com.music.application.be.modules.playlist.Playlist;
import com.music.application.be.modules.role.Role;
import com.music.application.be.modules.song.Song;
import com.music.application.be.modules.token.Token;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Enumerated(value = EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column()
    private String phone;

    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String avatar;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreationTimestamp
    @Column(name = "updated_at", updatable = false)
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "user_favorite_songs",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    private Set<Song> favoriteSongs = new HashSet<>();

    // Quan hệ nhiều-nhiều với Playlist (Favorite Playlists)
    @ManyToMany
    @JoinTable(
            name = "user_favorite_playlists",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "playlist_id")
    )
    private Set<Playlist> favoritePlaylists = new HashSet<>();

    // Quan hệ nhiều-nhiều với Artist (Followed Artists)
    @ManyToMany
    @JoinTable(
            name = "user_followed_artists",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    private Set<Artist> followedArtists = new HashSet<>();

//    private String theme;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Token> tokens;

    @OneToOne(mappedBy = "user")
    private ForgetPassword forgetPassword;

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority( role.name()));
    }

}