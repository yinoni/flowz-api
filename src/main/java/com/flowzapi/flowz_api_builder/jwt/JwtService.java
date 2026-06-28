package com.flowzapi.flowz_api_builder.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.flowzapi.flowz_api_builder.model.User;
import com.flowzapi.flowz_api_builder.model.user.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {


    private final Algorithm algorithm;

    private final JWTVerifier verifier;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.algorithm = Algorithm.HMAC256(secret);

        this.verifier = JWT.require(algorithm)
                .withIssuer("flowz-api")
                .build();
    }

    private final long EXPIRATION_TIME = java.time.Duration.ofMinutes(15).toMillis();

    public String generateToken(CustomUserDetails user) {
        return JWT.create()
                .withSubject(user.getEmail())
                .withClaim("id", user.getId())
                .withClaim("username", user.getUsername())
                .withClaim("verified",  user.isVerified())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .withIssuer("flowz-api")
                .sign(algorithm);
    }

    public String generateToken(User user) {
        return JWT.create()
                .withSubject(user.getEmail())
                .withClaim("id", user.getId())
                .withClaim("username", user.getUsername())
                .withClaim("verified",  user.isVerified())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .withIssuer("flowz-api")
                .sign(algorithm);
    }

    // 2. אימות וחילוץ שם המשתמש מהטוקן
    public String validateTokenAndGetEmail(String token) {
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException exception) {
            return null; // הטוקן פג תוקף או מזויף
        }
    }

    public <T> T extractClaim(String token, String claimName, Class<T> clazz) {
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            Claim claim = decodedJWT.getClaim(claimName);

            if (claim.isNull()) {
                return null;
            }

            return claim.as(clazz);

        } catch (JWTVerificationException exception) {
            return null;
        }
    }

}
