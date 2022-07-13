package com.hanghae99_team3.login.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanghae99_team3.login.jwt.JwtTokenProvider;
import com.hanghae99_team3.login.jwt.PrincipalDetails;
import com.hanghae99_team3.login.jwt.TokenService;
import com.hanghae99_team3.login.jwt.dto.TokenDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException{
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

        boolean isNew = principalDetails.isNew();

        // Token 발행
        TokenDto tokenDto = tokenService.login(principalDetails);

        log.info("{}", tokenDto.toString());

        response.setContentType("text/html;charset=UTF-8");
        response.addHeader("Access-Token",tokenDto.getAccessToken());
        response.addHeader("Refresh-Token", tokenDto.getRefreshToken());
        if (isNew) {
            response.addHeader("isNew", "Y");
        } else {
            response.addHeader("isNew", "N");
        }


        getRedirectStrategy().sendRedirect(request, response, makeRedirectUrl());
    }



    private String makeRedirectUrl() {

        return UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/redirect/")

//                .queryParam("token", accessToken)
                .build().toUriString();
    }
//    private void writeTokenResponse(HttpServletResponse response, String token)
//            throws IOException {
//        response.setContentType("text/html;charset=UTF-8");
//
//        response.addHeader("Access-Token", token);
////        response.addHeader("Refresh", token.getRefreshToken());
//        response.setContentType("application/json;charset=UTF-8");
//
//        var writer = response.getWriter();
//        writer.println(objectMapper.writeValueAsString(token));
//        writer.flush();
//    }
}