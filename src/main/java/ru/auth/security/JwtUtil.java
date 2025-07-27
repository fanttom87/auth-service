package ru.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret:mySuperSecretKey}")
    private String secret;

    @Value("${jwt.expiration.ms:3600000}")
    private long expirationMs;

    /**
     * Извлекает логин пользователя из JWT токена.
     * @param token JWT токен.
     * @return Логин пользователя.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Извлекает дату истечения срока действия из JWT токена.
     * @param token JWT токен.
     * @return Дата истечения срока действия.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Общий метод для извлечения любого Claim из JWT токена.
     * @param token JWT токен.
     * @param claimsResolver Функция, которая извлекает нужный Claim.
     * @param <T> Тип возвращаемого Claim.
     * @return Извлеченное значение Claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Извлекает все Claims из JWT токена.
     * @param token JWT токен.
     * @return Объект Claims.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    /**
     * Проверяет, действителен ли JWT токен для данного пользователя.
     * @param token JWT токен.
     * @param userDetails Информация о пользователе.
     * @return true, если токен действителен, false в противном случае.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Проверяет, истек ли срок действия JWT токена.
     * @param token JWT токен.
     * @return true, если срок действия истек, false в противном случае.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Генерирует JWT токен для пользователя.
     * @param userDetails Информация о пользователе.
     * @return Сгенерированный JWT токен.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Создает JWT токен с заданными claims и subject.
     * @param claims Дополнительные claims.
     * @param subject Идентификатор пользователя (обычно логин).
     * @return Сгенерированный JWT токен.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }
}
