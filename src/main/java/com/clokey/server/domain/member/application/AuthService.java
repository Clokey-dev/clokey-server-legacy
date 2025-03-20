package com.clokey.server.domain.member.application;


import java.net.http.HttpRequest;
import java.util.Map;

import com.clokey.server.domain.member.dto.AuthDTO;
import com.clokey.server.global.common.response.BaseResponse;

public interface AuthService {

    // JWT 생성 관련 메서드들
    String generateAccessToken(Long userId, String email);

    String generateRefreshToken(Long userId);

    boolean validateJwtToken(String token);

    String extractUserIdFromToken(String token);

    // 카카오 사용자 정보 조회 및 DB 저장 메서드 추가
    BaseResponse<AuthDTO.TokenResponse> authenticateKakaoUser(String kakaoAccessToken, String deviceToken);

    AuthDTO.KakaoUserResponse getUserInfoFromKakao(String kakaoAccessToken);

    AuthDTO.TokenResponse refreshAccessToken(String refreshToken);

    String createClientSecret();

    BaseResponse<AuthDTO.TokenResponse> appleLogin(String code, String deviceToken);

    HttpRequest.BodyPublisher getParamsUrlEncoded(Map<String, String> parameters);

    String getAppleRefreshToken(String clientSecret, String authCode);

}
