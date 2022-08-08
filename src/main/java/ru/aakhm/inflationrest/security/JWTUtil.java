package ru.aakhm.inflationrest.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;

@Component
public class JWTUtil {
    @Value("${security.jwt_secret}")
    private String SECRET;

    @Value("${security.jwt.expire.minutes:60}")
    private int EXPIRE_MINUTES;

    @Value("${security.issuer}")
    private String ISSUER;

    public String generateToken(String login) {
        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(EXPIRE_MINUTES).toInstant());

        return JWT.create()
                .withSubject("User details")
                .withClaim("login", login)
                .withIssuedAt(new Date())
                .withIssuer(ISSUER)
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(SECRET));
    }

    public String validateTokenAndRetrieveClaim(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET))
                .withSubject("User details")
                .withIssuer(ISSUER)
                .build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("login").asString();
    }
}
