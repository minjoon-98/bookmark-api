package io.github.minjoon98.bookmark.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtDecoderProvider {

    private final JwtKeyHolder jwtKeyHolder;

    public JwtDecoder getDecoder() throws JwtException {
        return NimbusJwtDecoder.withSecretKey(jwtKeyHolder.getKey()).build();
    }
}
