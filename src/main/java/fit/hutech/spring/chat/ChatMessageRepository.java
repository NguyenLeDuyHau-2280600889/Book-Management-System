package fit.hutech.spring.chat;

import fit.hutech.spring.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByCustomerIdOrderByCreatedAtAsc(Long customerId);
    ChatMessage findTopByCustomerIdOrderByCreatedAtDesc(Long customerId);
    long countByCustomerId(Long customerId);
    @Modifying
    @Transactional
    void deleteByCustomerId(Long customerId);
    @Modifying
    @Transactional
    void deleteBySenderId(Long senderId);

    @Query("select distinct m.customer from ChatMessage m order by m.customer.username")
    List<User> findDistinctCustomers();
}
