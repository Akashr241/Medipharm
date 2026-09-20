package com.example.demo.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.security.jwt.JwtAuthenticationEntryPoint;
import com.example.demo.security.jwt.JwtFilter;


@Configuration
public class SecurityConfig {


    @Value("${app.frontend.url}")
    private String frontendUrl;

    

@Value("${spring.security.oauth2.client.registration.google.client-id}")
private String googleClientId;

    private final JwtFilter jwtFilter;

    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public SecurityConfig(
            JwtFilter jwtFilter,
            OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint
    ) {

        this.jwtFilter =
                jwtFilter;

        this.oAuth2AuthenticationSuccessHandler =
                oAuth2AuthenticationSuccessHandler;

        this.jwtAuthenticationEntryPoint =
                jwtAuthenticationEntryPoint;
    }


    // =====================================================
    // SECURITY FILTER CHAIN
    // =====================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {
   System.out.println("========================================");
    System.out.println("GOOGLE OAUTH DEBUG");
    System.out.println("GOOGLE CLIENT ID = [" + googleClientId + "]");
    System.out.println("CLIENT ID LENGTH = " + googleClientId.length());
    System.out.println("========================================");

        http

     

                // =================================================
                // CORS
                // =================================================

                .cors(cors -> {})


                // =================================================
                // CSRF
                // =================================================

                .csrf(csrf -> csrf.disable())


                // =================================================
                // AUTHORIZATION
                // =================================================

                .authorizeHttpRequests(auth -> auth


                // -------------------------------------------------
                // PUBLIC AUTHENTICATION APIs
                // -------------------------------------------------

                .requestMatchers(
                        "/auth/**"
                ).permitAll()


                // -------------------------------------------------
                // GOOGLE OAUTH2
                // -------------------------------------------------

                .requestMatchers(
                        "/oauth2/**",
                        "/login/**"
                ).permitAll()


                // -------------------------------------------------
                // SWAGGER
                // -------------------------------------------------

                .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html"
                ).permitAll()


                // -------------------------------------------------
                // HEALTH CHECK
                // -------------------------------------------------

                .requestMatchers(
                        "/health"
                ).permitAll()


                // =================================================
                // PRODUCTS
                // =================================================

                // ADMIN - CREATE PRODUCT
                .requestMatchers(
                        HttpMethod.POST,
                        "/products"
                ).hasAuthority("ADMIN")


                // ADMIN - UPDATE PRODUCT
                .requestMatchers(
                        HttpMethod.PUT,
                        "/products"
                ).hasAuthority("ADMIN")


                // ADMIN - DELETE PRODUCT
                .requestMatchers(
                        HttpMethod.DELETE,
                        "/products"
                ).hasAuthority("ADMIN")


                // PRODUCT IMPORT
                .requestMatchers(
                        HttpMethod.POST,
                        "/products/import"
                ).permitAll()


                // PUBLIC - VIEW PRODUCTS
                .requestMatchers(
                        HttpMethod.GET,
                        "/products"
                ).permitAll()


                .requestMatchers(
                        HttpMethod.GET,
                        "/products/{id}"
                ).permitAll()


                .requestMatchers(
                        HttpMethod.GET,
                        "/products/**"
                ).permitAll()


                // =================================================
                // USERS
                // =================================================

                .requestMatchers(
                        HttpMethod.GET,
                        "/users"
                ).hasAuthority("ADMIN")


                .requestMatchers(
                        HttpMethod.GET,
                        "/users/**"
                ).hasAuthority("ADMIN")


                // =================================================
                // CHECKOUT
                // =================================================

                .requestMatchers(
                        HttpMethod.POST,
                        "/checkout/**"
                ).permitAll()


                // =================================================
                // CART
                // =================================================

                // ADD CART ITEM
                .requestMatchers(
                        HttpMethod.POST,
                        "/cart/**"
                ).hasAuthority("USER")


                // ADMIN - VIEW ALL CARTS
                .requestMatchers(
                        HttpMethod.GET,
                        "/cart/all"
                ).hasAuthority("ADMIN")


                // USER - VIEW OWN CART
                .requestMatchers(
                        HttpMethod.GET,
                        "/cart/my-cart"
                ).hasAuthority("USER")


                // USER - REMOVE CART ITEM
                .requestMatchers(
                        HttpMethod.DELETE,
                        "/cart/remove/{cartItemId}"
                ).hasAuthority("USER")


                .requestMatchers(
                        HttpMethod.DELETE,
                        "/cart/remove/**"
                ).hasAuthority("USER")


                .requestMatchers(
                        HttpMethod.DELETE,
                        "/cart/items/{id}"
                ).hasAuthority("USER")


                // USER - UPDATE CART
                .requestMatchers(
                        HttpMethod.PUT,
                        "/cart/**"
                ).hasAuthority("USER")


                // =================================================
                // ORDERS
                // =================================================

                // ADMIN - UPDATE ORDER STATUS
                .requestMatchers(
                        HttpMethod.PUT,
                        "/orders/*/status"
                ).hasAuthority("ADMIN")


                // ADMIN - ORDER STATUS
                .requestMatchers(
                        HttpMethod.GET,
                        "/orders/status"
                ).hasAuthority("ADMIN")


                // ADMIN - ALL ORDERS
                .requestMatchers(
                        HttpMethod.GET,
                        "/orders"
                ).hasAuthority("ADMIN")


                // USER - OWN ORDERS
                .requestMatchers(
                        HttpMethod.GET,
                        "/orders/my-orders"
                ).permitAll()


                // USER - CANCEL ORDER
                .requestMatchers(
                        HttpMethod.DELETE,
                        "/orders/*/cancel"
                ).hasAuthority("USER")


                // =================================================
                // PAYMENTS
                // =================================================

                .requestMatchers(
                        "/api/payments/**"
                ).permitAll()


                .requestMatchers(
                        HttpMethod.POST,
                        "/payments/create*"
                ).hasAuthority("USER")


                .requestMatchers(
                        HttpMethod.POST,
                        "/payments/verify"
                ).hasAuthority("USER")


                .requestMatchers(
                        HttpMethod.GET,
                        "/payments/**"
                ).hasAnyAuthority(
                        "USER",
                        "ADMIN"
                )


                // =================================================
                // RAZORPAY
                // =================================================

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/razorpay/**"
                ).permitAll()


                .requestMatchers(
                        HttpMethod.POST,
                        "/api/razorpay/**"
                ).permitAll()


                // =================================================
                // AI CHATBOT
                // =================================================

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/ai/**"
                ).permitAll()


                .requestMatchers(
                        HttpMethod.POST,
                        "/api/ai/chat/**"
                ).permitAll()


                // =================================================
                // PRESCRIPTION
                // =================================================

                .requestMatchers(
                        HttpMethod.GET,
                        "/prescription/**"
                ).permitAll()


                .requestMatchers(
                        HttpMethod.POST,
                        "/api/prescription/ocr"
                ).permitAll()


                .requestMatchers(
                        HttpMethod.POST,
                        "/api/prescription/analyze"
                ).permitAll()


                // FDA
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/prescription/fda/**"
                ).permitAll()


                .requestMatchers(
                        HttpMethod.GET,
                        "/api/prescription/fda/**"
                ).permitAll()


                // MEDICINES
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/medicines/**"
                ).permitAll()


                .requestMatchers(
                        HttpMethod.POST,
                        "/api/medicines/**"
                ).permitAll()


                // =================================================
                // EVERYTHING ELSE
                // =================================================

                .anyRequest().authenticated())


                // =================================================
                // GOOGLE LOGIN
                // =================================================

                .oauth2Login(oauth2 -> oauth2

                        .successHandler(
                                oAuth2AuthenticationSuccessHandler
                        )

                        .failureHandler(
                                (request, response, exception) -> {

                                    System.out.println(
                                            "========================================"
                                    );

                                    System.out.println(
                                            "GOOGLE OAUTH LOGIN FAILED"
                                    );

                                    System.out.println(
                                            "Exception: "
                                                    + exception
                                                            .getClass()
                                                            .getName()
                                    );

                                    System.out.println(
                                            "Message: "
                                                    + exception.getMessage()
                                    );

                                    System.out.println(
                                            "========================================"
                                    );


                                    response.sendRedirect(
                                            frontendUrl
                                                    + "/login?oauth2Error=true"
                                    );
                                }
                        )
                )


                // =================================================
                // EXCEPTION HANDLING
                // =================================================

                .exceptionHandling(
                        ex -> ex.authenticationEntryPoint(
                                jwtAuthenticationEntryPoint
                        )
                )


                // =================================================
                // SESSION MANAGEMENT
                // =================================================

                .sessionManagement(
                        session -> session.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )


                // =================================================
                // JWT FILTER
                // =================================================

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }


    // =====================================================
    // PASSWORD ENCODER
    // =====================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}