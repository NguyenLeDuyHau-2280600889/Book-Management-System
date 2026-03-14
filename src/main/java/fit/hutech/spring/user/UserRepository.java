package fit.hutech.spring.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User findByUsername(String username);
    User findByEmail(String email);
    User findByProviderAndProviderId(String provider, String providerId);
    java.util.List<User> findByRole(String role);
}
