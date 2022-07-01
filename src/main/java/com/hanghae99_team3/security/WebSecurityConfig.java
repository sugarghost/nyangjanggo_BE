package com.hanghae99_team3.security;

import com.hanghae99_team3.security.jwt.JwtAuthFilter;
import com.hanghae99_team3.security.jwt.JwtTokenProvider;
import com.hanghae99_team3.security.oauth2.OAuth2SuccessHandler;
import com.hanghae99_team3.security.oauth2.PrincipalOauth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class WebSecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
//    private final PrincipalOauth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;




    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // h2-console 사용에 대한 허용 (CSRF, FrameOptions 무시)
        return (web) ->
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
                .httpBasic().disable() // rest api 만을 고려하여 기본 설정은 해제하겠습니다.
                .csrf().disable() // csrf 보안 토큰 disable처리.
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 토큰 기반 인증이므로 세션 역시 사용하지 않습니다.
                .and()
                .authorizeRequests() // 요청에 대한 사용권한 체크
//                .and()
//                .oauth2Login()
//                    .successHandler(oAuth2SuccessHandler)
//                    .userInfoEndpoint().userService(oAuth2UserService);
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasAnyAuthority("USER_TOKEN")
                .anyRequest().permitAll() // 그외 나머지 요청은 누구나 접근 가능
                .and()
                    .oauth2Login()
                    .successHandler(oAuth2SuccessHandler);
//                    .userInfoEndpoint().userService(oAuth2UserService);
        http
                .addFilterBefore(new JwtAuthFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);
        // JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 전에 넣는다
        return http.build();
    }

}