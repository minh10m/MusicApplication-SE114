package com.music.application.be.modules.token;

import com.music.application.be.modules.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.access-token-expiration}")
    private long accessTokenExpire;

    @Value("${application.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpire;

    @Autowired
    private TokenRepository tokenRepository;

    @Cacheable(value = "jwtUsername", key = "#token")
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Cacheable(value = "jwtValid", key = "#token")
    public boolean isValid(String token, UserDetails user) {
        String username = extractUsername(token);

        boolean validToken = tokenRepository
                .findByAccessToken(token)
                .map(t -> !t.isLoggedOut())
                .orElse(false);

        return (username.equals(user.getUsername())) && !isTokenExpired(token) && validToken;
    }

    @Cacheable(value = "jwtValidRefresh", key = "#token")
    public boolean isValidRefreshToken(String token, User user) {
        String username = extractUsername(token);

        boolean validRefreshToken = tokenRepository
                .findByRefreshToken(token)
                .map(t -> !t.isLoggedOut())
                .orElse(false);

        return (username.equals(user.getUsername())) && !isTokenExpired(token) && validRefreshToken;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @Cacheable(value = "jwtClaims", key = "#token")
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSigninKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @CachePut(value = "jwtAccessTokens", key = "#user.username")
    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpire);
    }

    @CachePut(value = "jwtRefreshTokens", key = "#user.username")
    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpire);
    }

    @CacheEvict(value = {"jwtAccessTokens", "jwtRefreshTokens", "jwtUsername", "jwtValid", "jwtValidRefresh", "jwtClaims"}, key = "#user.username")
    public void invalidateTokens(User user) {
        // Method to invalidate all cached tokens for a user
    }

    private String generateToken(User user, long expireTime) {
        System.out.println("=== Generating Token ===");
        System.out.println("Username: " + user.getUsername());
        System.out.println("Expire Time: " + expireTime);

        String token = null;
        try {
            token = Jwts.builder()
                    .subject(user.getUsername())
                    .claim("userId", user.getId())
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + expireTime))
                    .signWith(getSigninKey())
                    .compact();
        } catch (Exception e) {
            System.out.println("Error Generating Token: " + e.getMessage());
        }

        System.out.println("Generated Token: " + token);
        System.out.println("========================");

        return token;
    }

    private SecretKey getSigninKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}