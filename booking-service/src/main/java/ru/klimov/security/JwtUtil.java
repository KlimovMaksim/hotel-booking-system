package ru.klimov.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.klimov.entity.User;

import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private Long tokenExpirationMills;

    public String generateTokenForUser(User user) {
        long currentSeconds = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .setIssuedAt(new Date(currentSeconds))
                .setExpiration(new Date(currentSeconds + tokenExpirationMills))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String getUserLogin(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean isValidToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            log.error("An error occurred while checking the token for validity: {}", ex.getMessage());
        }

        return false;
    }
}

