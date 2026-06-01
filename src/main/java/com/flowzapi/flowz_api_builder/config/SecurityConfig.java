package com.flowzapi.flowz_api_builder.config;

import com.flowzapi.flowz_api_builder.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. מכיוון שמדובר ב-REST API, אנחנו לא צריכים הגנת CSRF ולא עובדים עם Sessions (הכל stateless בזכות ה-JWT)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 2. הגדרת חוקי גישה לנתיבים
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register", "/auth/login", "/auth/google", "/ws-flow/**").permitAll() // נתיבי הרשמה ולוגין פתוחים לכולם
                        .anyRequest().authenticated() // כל שאר ה-Endpoints באפליקציה דורשים יוזר מחובר
                )

                // 3. הזרקת ה-JWT Filter שלנו לפני הפילטר הסטנדרטי של Username/Password
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. הגדרת הדומיינים המורשים (למשל ה-Frontend שלך ב-Next.js או React)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://yourproductiondomain.com", "http://localhost:8080"));

        // 2. הגדרת מתודות ה-HTTP המורשות
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 3. הגדרת ה-Headers שמותר ל-Frontend לשלוח (כולל ה-Authorization עבור ה-JWT)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));

        // 4. האם לאפשר שליחת קוקיז / Credentials במידת הצורך
        configuration.setAllowCredentials(true);

        // 5. חשיפת הדרים מסוימים חזרה ל-Client אם יש צורך (אופציונלי)
        configuration.setExposedHeaders(Collections.singletonList("Authorization"));

        // החלת ההגדרות הללו על כל ה-Endpoints באפליקציה (/**)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // חובה להצפנת סיסמאות ב-DB!
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
