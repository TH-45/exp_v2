package jh.exp.auth.controller;

import jh.exp.common.auth.dto.LoginRequest;
import jh.exp.common.auth.dto.LoginUserInfo;
import jh.exp.common.auth.dto.ProfileResult;
import jh.exp.auth.service.AuthService;
import jh.exp.auth.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;




/**
 * 仅供网关调用的内部登录校验接口，不直接对前端暴露。
 */
@RestController
@RequestMapping("/internal/auth")
public class InternalAuthController {

    private final AuthService authService;
    private final ProfileService profileService;

    public InternalAuthController(AuthService authService, ProfileService profileService) {
        this.authService = authService;
        this.profileService = profileService;
    }

    @PostMapping("/login")
    public LoginUserInfo login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/profile")
    public ProfileResult profile(@RequestParam("userId") String userId) {
        return profileService.getProfile(userId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleForbidden(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}


