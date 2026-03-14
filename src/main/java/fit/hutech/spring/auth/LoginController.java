package fit.hutech.spring.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    private final String googleClientId;
    private final String facebookAppId;

    public LoginController(
            @Value("${app.oauth.google.client-id:}") String googleClientId,
            @Value("${app.oauth.facebook.app-id:}") String facebookAppId
    ) {
        this.googleClientId = googleClientId;
        this.facebookAppId = facebookAppId;
    }

    @GetMapping("/login")
    public String login(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        model.addAttribute("googleClientId", googleClientId);
        model.addAttribute("facebookAppId", facebookAppId);
        return "auth/login";
    }
}
