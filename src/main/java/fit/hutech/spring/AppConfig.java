package fit.hutech.spring;

import fit.hutech.spring.entities.Book;
import fit.hutech.spring.entities.BookRepository;
import fit.hutech.spring.user.User;
import fit.hutech.spring.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {
    @Bean
    public CommandLineRunner initData(BookRepository bookRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (bookRepository.count() == 0) {
                bookRepository.save(new Book(null, "Lập trình Web Spring Framework", "Ánh Nguyễn", 29.99, "Công nghệ thông tin",
                        "https://picsum.photos/seed/spring-framework/300/400"));
                bookRepository.save(new Book(null, "Lập trình ứng dụng Java", "Huy Cường", 45.63, "Công nghệ thông tin",
                        "https://picsum.photos/seed/java-app/300/400"));
                bookRepository.save(new Book(null, "Lập trình Web Spring Boot", "Xuân Nhân", 12.0, "Công nghệ thông tin",
                        "https://picsum.photos/seed/spring-boot/300/400"));
                bookRepository.save(new Book(null, "Lập trình Web Spring MVC", "Ánh Nguyễn", 0.12, "Công nghệ thông tin",
                        "https://picsum.photos/seed/spring-mvc/300/400"));
            }
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPasswordHash(passwordEncoder.encode("admin"));
                admin.setRole("ADMIN");
                userRepository.save(admin);
            }
        };
    }
}
