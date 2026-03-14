package fit.hutech.spring.auth;

import fit.hutech.spring.user.User;
import fit.hutech.spring.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/social")
public class SocialAuthController {
    private final SocialAuthService socialAuthService;
    private final UserService userService;

    public SocialAuthController(SocialAuthService socialAuthService, UserService userService) {
        this.socialAuthService = socialAuthService;
        this.userService = userService;
    }

    @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> loginGoogle(@RequestBody SocialTokenRequest request, HttpServletRequest httpRequest) {
        return handleLogin(() -> socialAuthService.verifyGoogle(request.getToken()), httpRequest);
    }

    @PostMapping("/facebook")
    public ResponseEntity<Map<String, Object>> loginFacebook(@RequestBody SocialTokenRequest request, HttpServletRequest httpRequest) {
        return handleLogin(() -> socialAuthService.verifyFacebook(request.getToken()), httpRequest);
    }

    private Map<String, Object> loginAndRespond(SocialProfile profile, HttpServletRequest httpRequest) {
        User user = userService.getOrCreateSocialUser(
                profile.getProvider(),
                profile.getProviderId(),
                profile.getEmail(),
                profile.getName()
        );
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRole())
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        Map<String, Object> body = new HashMap<>();
        body.put("ok", true);
        body.put("redirectUrl", "/");
        return body;
    }

    private ResponseEntity<Map<String, Object>> handleLogin(ProfileSupplier supplier, HttpServletRequest httpRequest) {
        try {
            SocialProfile profile = supplier.get();
            return ResponseEntity.ok(loginAndRespond(profile, httpRequest));
        } catch (IllegalArgumentException ex) {
            Map<String, Object> body = new HashMap<>();
            body.put("ok", false);
            body.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(body);
        } catch (Exception ex) {
            Map<String, Object> body = new HashMap<>();
            body.put("ok", false);
            body.put("message", "Khong the xac thuc dang nhap.");
            return ResponseEntity.badRequest().body(body);
        }
    }

    private interface ProfileSupplier {
        SocialProfile get();
    }
}
