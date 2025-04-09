package com.clokey.server.domain.member.api;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.member.application.AuthService;
import com.clokey.server.domain.member.application.UnlinkService;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.dto.AuthDTO;
import com.clokey.server.domain.member.exception.MemberException;
import com.clokey.server.domain.member.exception.annotation.AuthUser;
import com.clokey.server.global.common.response.BaseResponse;
import com.clokey.server.global.error.code.status.ErrorStatus;
import com.clokey.server.global.error.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UnlinkService logoutService;

    @Operation(summary = "로그인 API", description = "로그인하는 API입니다.")
    @PostMapping("/login")
    public BaseResponse<AuthDTO.TokenResponse> login(@RequestBody AuthDTO.LoginRequest loginRequest) {
        // 로그인 타입 확인
        String loginType = loginRequest.getType();

        if (loginType == null || loginType.isBlank()) {
            throw new MemberException(ErrorStatus.MISSING_LOGIN_TYPE);
        }

        BaseResponse<AuthDTO.TokenResponse> response;

        //카카오 로그인
        if (loginType.equalsIgnoreCase("kakao") && loginRequest.getAccessToken() != null) {
            response = authService.authenticateKakaoUser(loginRequest.getAccessToken(), loginRequest.getDeviceToken());
        }
        //애플 로그인
        else if (loginType.equalsIgnoreCase("apple") && loginRequest.getAuthorizationCode() != null) {
            // Apple 로그인 처리
            response= authService.appleLogin(loginRequest.getAuthorizationCode(), loginRequest.getDeviceToken());
        }
        //로그인 타입이 잘못된 경우
        else if (!loginType.equalsIgnoreCase("kakao") && !loginType.equalsIgnoreCase("apple")) {
            throw new MemberException(ErrorStatus.INVALID_LOGIN_TYPE);
        } else {
            throw new MemberException(ErrorStatus.LOGIN_FAILED);
        }

        return response;
    }


    @Operation(summary = "토큰 재발급 API", description = "액세스 토큰을 재발급하는 API입니다.")
    @PostMapping("/reissue-token")
    public BaseResponse<AuthDTO.TokenResponse> reissueToken(@RequestBody AuthDTO.RefreshTokenRequest request) {

        AuthDTO.TokenResponse response = authService.refreshAccessToken(request.getRefreshToken());
        return BaseResponse.onSuccess(SuccessStatus.LOGIN_UPDATED, response);

    }


    @Operation(summary = "회원탈퇴 API", description = "회원탈퇴하는 API입니다.")
    @DeleteMapping("/unlink")
    public BaseResponse<Void> unlink(@Parameter(name = "user", hidden = true) @AuthUser Member member) {
        logoutService.unlink(member.getId());
        return BaseResponse.onSuccess(SuccessStatus.UNLINK_SUCCESS, null);
    }

    @Operation(summary = "관리자용 즉시 회원탈퇴 API", description = "즉시 회원탈퇴하는 API입니다.")
    @DeleteMapping("/instant-unlink")
    public BaseResponse<Void> instantUnlink(@Parameter(name = "user", hidden = true) @AuthUser Member member) {
        logoutService.unlink(member.getId());
        logoutService.deleteData(member.getId());
        return BaseResponse.onSuccess(SuccessStatus.UNLINK_SUCCESS, null);
    }

}
