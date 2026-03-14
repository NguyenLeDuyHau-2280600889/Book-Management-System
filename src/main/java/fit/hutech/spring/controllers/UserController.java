package fit.hutech.spring.controllers;

import fit.hutech.spring.chat.ChatMessageRepository;
import fit.hutech.spring.user.UserRepository;
import fit.hutech.spring.entities.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ChatMessageRepository chatMessageRepository;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "user/list";
    }

    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteUser(@PathVariable Long id) {
        chatMessageRepository.deleteByCustomerId(id);
        chatMessageRepository.deleteBySenderId(id);
        orderRepository.deleteByUserId(id);
        userRepository.deleteById(id);
        return "redirect:/users";
    }
}
