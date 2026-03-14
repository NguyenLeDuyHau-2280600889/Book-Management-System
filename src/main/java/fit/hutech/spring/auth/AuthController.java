package fit.hutech.spring.auth;

import fit.hutech.spring.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.beans.factory.annotation.Value;

@Controller
public class AuthController {
    private final UserService userService;
    private final String adminCode;

    public AuthController(UserService userService, @Value("${app.admin.code:admin123}") String adminCode) {
        this.userService = userService;
        this.adminCode = adminCode;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute("form") RegisterForm form, Model model) {
        if (form.getPassword() == null || !form.getPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp.");
            return "auth/register";
        }

        try {
            String role = normalizeRole(form.getRole());
            if ("ADMIN".equals(role)) {
                if (form.getAdminCode() == null || !form.getAdminCode().equals(adminCode)) {
                    model.addAttribute("error", "Mã quản lý không đúng.");
                    return "auth/register";
                }
            }
            userService.register(form.getUsername(), form.getEmail(), form.getPassword(), role);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "auth/register";
        }

        return "auth/register_success";
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "CUSTOMER";
        }
        String normalized = role.trim().toUpperCase();
        if (!normalized.equals("ADMIN")) {
            return "CUSTOMER";
        }
        return normalized;
    }
}
