package io.github.minjoon98.bookmark.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Getter
@Component
public class JwtKeyHolder {

    private final SecretKey key;

    public JwtKeyHolder(@Value("${jwt.secret}") String jwtSecret) {
        this.key = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
    }
}
