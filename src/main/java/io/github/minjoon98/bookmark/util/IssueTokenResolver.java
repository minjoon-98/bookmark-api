package io.github.minjoon98.bookmark.util;

import io.github.minjoon98.bookmark.entity.User;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IssueTokenResolver {

    private final JwtKeyHolder jwtKeyHolder;

    public String issueToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .signWith(jwtKeyHolder.getKey())
                .compact();
    }
}
