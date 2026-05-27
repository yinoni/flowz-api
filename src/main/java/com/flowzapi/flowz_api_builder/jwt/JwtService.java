package com.flowzapi.flowz_api_builder.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.flowzapi.flowz_api_builder.model.User;
import com.flowzapi.flowz_api_builder.model.user.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {


    private final String SECRET_KEY = "my_super_secret_key_that_should_be_long_and_stored_in_application_properties";

    private final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);


    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    // 1. יצירת טוקן
    public String generateToken(CustomUserDetails user) {
        return JWT.create()
                .withSubject(user.getEmail())
                .withClaim("id", user.getId())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // בתוקף ל-24 שעות
                .withIssuer("flowz-api")
                .sign(algorithm);
    }

    // 2. אימות וחילוץ שם המשתמש מהטוקן
    public String validateTokenAndGetEmail(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).withIssuer("flowz-api").build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException exception) {
            return null; // הטוקן פג תוקף או מזויף
        }
    }

}
