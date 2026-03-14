package fit.hutech.spring.controllers;

import fit.hutech.spring.chat.ChatMessage;
import fit.hutech.spring.chat.ChatConversationSummary;
import fit.hutech.spring.chat.ChatMessageDto;
import fit.hutech.spring.chat.ChatMessageRepository;
import fit.hutech.spring.user.User;
import fit.hutech.spring.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chat")
public class ChatController {
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ChatController(ChatMessageRepository chatMessageRepository, UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String chatHome(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (isAdmin(authentication)) {
            List<ChatConversationSummary> conversations = userRepository.findByRole("CUSTOMER").stream()
                    .map(customer -> {
                        ChatMessage last = chatMessageRepository.findTopByCustomerIdOrderByCreatedAtDesc(customer.getId());
                        String lastMessage = last == null ? "" : last.getContent();
                        java.time.Instant lastMessageAt = last == null ? null : last.getCreatedAt();
                        long totalMessages = chatMessageRepository.countByCustomerId(customer.getId());
                        return new ChatConversationSummary(customer, lastMessage, lastMessageAt, totalMessages);
                    })
                    .sorted(Comparator.comparing(ChatConversationSummary::getLastMessageAt,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
            model.addAttribute("conversations", conversations);
            return "chat/admin_list";
        }
        model.addAttribute("messages", chatMessageRepository.findByCustomerIdOrderByCreatedAtAsc(currentUser.getId()));
        model.addAttribute("customer", currentUser);
        return "chat/customer";
    }

    @GetMapping("/{customerId}")
    public String chatForCustomer(@PathVariable Long customerId, Authentication authentication, Model model) {
        if (!isAdmin(authentication)) {
            return "redirect:/chat";
        }
        Optional<User> customer = userRepository.findById(customerId);
        if (customer.isEmpty()) {
            return "redirect:/chat";
        }
        model.addAttribute("messages", chatMessageRepository.findByCustomerIdOrderByCreatedAtAsc(customerId));
        model.addAttribute("customer", customer.get());
        return "chat/admin";
    }

    @PostMapping("/send")
    public String sendCustomerMessage(@RequestParam("content") String content, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null || !StringUtils.hasText(content)) {
            return "redirect:/chat";
        }
        ChatMessage message = new ChatMessage(null, currentUser, currentUser, content.trim(), null);
        chatMessageRepository.save(message);
        return "redirect:/chat";
    }

    @PostMapping("/{customerId}/send")
    public String sendAdminMessage(@PathVariable Long customerId, @RequestParam("content") String content, Authentication authentication) {
        if (!isAdmin(authentication) || !StringUtils.hasText(content)) {
            return "redirect:/chat";
        }
        User admin = getCurrentUser(authentication);
        Optional<User> customer = userRepository.findById(customerId);
        if (admin == null || customer.isEmpty()) {
            return "redirect:/chat";
        }
        ChatMessage message = new ChatMessage(null, customer.get(), admin, content.trim(), null);
        chatMessageRepository.save(message);
        return "redirect:/chat/" + customerId;
    }

    @GetMapping("/messages")
    @ResponseBody
    public List<ChatMessageDto> customerMessages(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return List.of();
        }
        return toDtoList(chatMessageRepository.findByCustomerIdOrderByCreatedAtAsc(currentUser.getId()), currentUser);
    }

    @GetMapping("/{customerId}/messages")
    @ResponseBody
    public List<ChatMessageDto> adminMessages(@PathVariable Long customerId, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return List.of();
        }
        User admin = getCurrentUser(authentication);
        if (admin == null) {
            return List.of();
        }
        return toDtoList(chatMessageRepository.findByCustomerIdOrderByCreatedAtAsc(customerId), admin);
    }

    private List<ChatMessageDto> toDtoList(List<ChatMessage> messages, User currentUser) {
        return messages.stream()
                .map(message -> new ChatMessageDto(
                        message.getId(),
                        message.getSender().getUsername(),
                        message.getContent(),
                        message.getCreatedAt(),
                        message.getSender().getId().equals(currentUser.getId())
                ))
                .collect(Collectors.toList());
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName());
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
