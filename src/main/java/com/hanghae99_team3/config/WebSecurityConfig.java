package com.hanghae99_team3.config;

import com.hanghae99_team3.login.exception.CustomAuthenticationEntryPoint;
import com.hanghae99_team3.login.handler.OAuth2SuccessHandler;
import com.hanghae99_team3.login.handler.TokenAccessDeniedHandler;
import com.hanghae99_team3.login.jwt.JwtAuthFilter;
import com.hanghae99_team3.login.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;


@EnableWebSecurity
@Configuration
public class WebSecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final TokenAccessDeniedHandler tokenAccessDeniedHandler;

    @Autowired
    public WebSecurityConfig(JwtTokenProvider jwtTokenProvider, OAuth2SuccessHandler oAuth2SuccessHandler, TokenAccessDeniedHandler tokenAccessDeniedHandler) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.tokenAccessDeniedHandler = tokenAccessDeniedHandler;
    }


    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("https://nyangjanggo.com");
        configuration.addAllowedOrigin("https://api.nyangjanggo.com");
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.setAllowedMethods(Arrays.asList("GET","POST", "OPTIONS", "PUT","DELETE"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // h2-console ????????? ?????? ?????? (CSRF, FrameOptions ??????)
        return web ->
                web
                        .ignoring()
                        .antMatchers("/h2-console/**");
    }

    @Bean
    public BCryptPasswordEncoder encodePassword() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .httpBasic().disable() // rest api ?????? ???????????? ?????? ????????? ?????????????????????.
                .csrf().disable() // csrf ?????? ?????? disable??????.
                .cors().configurationSource(corsConfigurationSource())

                .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // ?????? ?????? ??????????????? ?????? ?????? ???????????? ????????????.

                .and()
                    .exceptionHandling()
                    .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                    .accessDeniedHandler(tokenAccessDeniedHandler)

                // ?????? ??????
                .and()
                    .authorizeRequests()
                    .antMatchers("/").permitAll()
                    .antMatchers("/api/boards/**").permitAll()
                    .antMatchers(HttpMethod.GET, "/api/board/**").permitAll()
                    .antMatchers("/refresh/**").permitAll()
                    .antMatchers("/docs/**").permitAll()


                    .antMatchers("/api/health").permitAll()
                    .antMatchers("/**").hasAnyRole("USER")
//                    .anyRequest().permitAll()
//                    .anyRequest().hasAnyRole("USER")



                .and()
                    .oauth2Login()
                    .successHandler(oAuth2SuccessHandler)


                .and()
                    .addFilterBefore(new JwtAuthFilter(jwtTokenProvider),
                            UsernamePasswordAuthenticationFilter.class);
        // JwtAuthenticationFilter??? UsernamePasswordAuthenticationFilter ?????? ?????????
        return http.build();
    }

}
