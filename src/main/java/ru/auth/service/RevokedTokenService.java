package ru.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RevokedTokenService {
    private final Map<String, Date> revokedTokens = new ConcurrentHashMap<>();

    /**
     * Добавляет токен в список отозванных.
     * @param token Отозванный токен.
     * @param expirationDate Дата истечения срока действия этого токена.
     */
    @Transactional
    public void revokeToken(String token, Date expirationDate) {
        if (token != null && expirationDate != null) {
            revokedTokens.put(token, expirationDate);
        }
    }

    /**
     * Проверяет, был ли токен отозван.
     * @param token Токен для проверки.
     * @return true, если токен отозван или его срок действия истек.
     */
    @Transactional
    public boolean isTokenRevoked(String token) {
        if (token == null) {
            return true;
        }
        Date expirationDate = revokedTokens.get(token);

        if (expirationDate != null) {
            if (expirationDate.before(new Date())) {
                revokedTokens.remove(token);
                return false;
            }
            return true;
        }

        return false;
    }
}
