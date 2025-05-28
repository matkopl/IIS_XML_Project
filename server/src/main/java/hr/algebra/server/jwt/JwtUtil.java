package hr.algebra.server.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey ACCESS_SECRET = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private final SecretKey REFRESH_SECRET = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private final long ACCESS_EXPIRATION = 1000 * 60 * 10; // 15 min
    private final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 24; //24h

    public String generateAccessToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
                .signWith(ACCESS_SECRET)
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(REFRESH_SECRET)
                .compact();
    }

    public String extractUsernameFromAccessToken(String token) {
        return Jwts.parser()
                .setSigningKey(ACCESS_SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractUsernameFromRefreshToken(String token) {
        return Jwts.parser()
                .setSigningKey(REFRESH_SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(ACCESS_SECRET)
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(REFRESH_SECRET)
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

 }
