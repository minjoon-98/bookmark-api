package io.github.minjoon98.bookmark.docs;

import io.github.minjoon98.bookmark.dto.request.LoginRequest;
import io.github.minjoon98.bookmark.dto.request.SignUpRequest;
import io.github.minjoon98.bookmark.dto.response.LoginResponse;
import io.github.minjoon98.bookmark.dto.response.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "인증 API")
public interface AuthApiDoc {

    @Operation(summary = "회원가입", description = "이메일과 비밀번호로 회원가입합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "이메일 중복")
    })
    default ResponseEntity<?> signUp(SignUpRequest request) {
        throw new UnsupportedOperationException("Doc only");
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    default ResponseEntity<?> login(LoginRequest request) {
        throw new UnsupportedOperationException("Doc only");
    }

    @Operation(summary = "로그아웃", description = "로그아웃합니다. 클라이언트에서 토큰을 삭제해야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "bearerAuth")
    default ResponseEntity<?> logout() {
        throw new UnsupportedOperationException("Doc only");
    }
}
