package fit.hutech.spring.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SocialProfile {
    private String provider;
    private String providerId;
    private String email;
    private String name;
}
