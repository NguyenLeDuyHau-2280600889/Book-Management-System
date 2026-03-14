package fit.hutech.spring.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String username, String email, String rawPassword, String role) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(email) || !StringUtils.hasText(rawPassword)) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin.");
        }

        String normalizedUsername = username.trim();
        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email đã được sử dụng.");
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);

        return userRepository.save(user);
    }

    public User getOrCreateSocialUser(String provider, String providerId, String email, String displayName) {
        if (!StringUtils.hasText(provider) || !StringUtils.hasText(providerId)) {
            throw new IllegalArgumentException("Thiếu thông tin nhà cung cấp.");
        }

        User existing = userRepository.findByProviderAndProviderId(provider, providerId);
        if (existing != null) {
            return existing;
        }

        String normalizedEmail = StringUtils.hasText(email) ? email.trim().toLowerCase() : null;
        if (StringUtils.hasText(normalizedEmail)) {
            User byEmail = userRepository.findByEmail(normalizedEmail);
            if (byEmail != null) {
                if (!StringUtils.hasText(byEmail.getProvider())) {
                    byEmail.setProvider(provider);
                    byEmail.setProviderId(providerId);
                    return userRepository.save(byEmail);
                }
                return byEmail;
            }
        }

        String baseUsername = StringUtils.hasText(displayName) ? displayName.trim() : "user";
        baseUsername = baseUsername.replaceAll("\\s+", "").toLowerCase();
        if (!StringUtils.hasText(baseUsername)) {
            baseUsername = "user";
        }
        String username = uniqueUsername(baseUsername, providerId);
        String emailValue = StringUtils.hasText(normalizedEmail)
                ? normalizedEmail
                : provider + "_" + providerId + "@social.local";

        User user = new User();
        user.setUsername(username);
        user.setEmail(emailValue);
        user.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        user.setRole("CUSTOMER");
        user.setProvider(provider);
        user.setProviderId(providerId);
        return userRepository.save(user);
    }

    private String uniqueUsername(String base, String providerId) {
        String normalized = base.toLowerCase();
        if (!userRepository.existsByUsername(normalized)) {
            return normalized;
        }
        String seed = providerId.length() > 6 ? providerId.substring(providerId.length() - 6) : providerId;
        String candidate = normalized + seed;
        if (!userRepository.existsByUsername(candidate)) {
            return candidate;
        }
        int counter = 1;
        while (userRepository.existsByUsername(normalized + counter)) {
            counter++;
        }
        return normalized + counter;
    }
}
