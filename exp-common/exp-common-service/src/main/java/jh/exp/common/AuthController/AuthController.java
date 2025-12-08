//import jh.exp.common.api.ApiResponse;
//import jh.exp.common.audit.AuditLogOperation;
//import jh.exp.common.auth.dto.LoginRequest;
//import jh.exp.common.auth.dto.LoginResult;
//import jh.exp.common.auth.dto.LoginUserInfo;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/exp/auth")
//public class AuthController {
//
//    private final AuthService authService;
//
//    public AuthController(AuthService authService) {
//        this.authService = authService;
//    }
//
//    @PostMapping("/login")
//    @AuditLogOperation(module = "AUTH", action = "LOGIN", targetId = "#req.username")
//    public ApiResponse<LoginResult> login(@RequestBody LoginRequest req) {
//        LoginUserInfo info = authService.login(req);
//        // ...
//        return ApiResponse.success(...);
//    }
//}