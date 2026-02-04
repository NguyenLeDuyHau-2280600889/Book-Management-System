package fit.hutech.spring.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class HomeController {
    @GetMapping
    public String home() {
        return "home/index";
    }

    @GetMapping("/subjects")
    public String subjects(Model model) {
        List<Subject> subjects = List.of(
            new Subject("SE101", "Nhập môn Công nghệ phần mềm", "/images/software.svg"),
            new Subject("SE201", "Phát triển ứng dụng Web", "/images/web.svg"),
            new Subject("SE301", "Lập trình di động", "/images/mobile.svg")
        );
        model.addAttribute("subjects", subjects);
        return "home/subjects";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("sentDate", LocalDate.now());
        return "home/contact";
    }

    @PostMapping("/contact")
    public String submitContact(
        @RequestParam String fullName,
        @RequestParam String email,
        @RequestParam String message,
        @RequestParam String sentDate,
        Model model
    ) {
        model.addAttribute("fullName", fullName);
        model.addAttribute("email", email);
        model.addAttribute("message", message);
        model.addAttribute("sentDate", sentDate);
        return "home/result";
    }

    private record Subject(String code, String name, String imageUrl) {}
}
