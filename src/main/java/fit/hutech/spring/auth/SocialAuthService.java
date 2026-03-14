package fit.hutech.spring.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class SocialAuthService {
    private final RestTemplate restTemplate = new RestTemplate();

    private final String googleClientId;
    private final String facebookAppId;
    private final String facebookAppSecret;

    public SocialAuthService(
            @Value("${app.oauth.google.client-id:}") String googleClientId,
            @Value("${app.oauth.facebook.app-id:}") String facebookAppId,
            @Value("${app.oauth.facebook.app-secret:}") String facebookAppSecret
    ) {
        this.googleClientId = googleClientId;
        this.facebookAppId = facebookAppId;
        this.facebookAppSecret = facebookAppSecret;
    }

    public SocialProfile verifyGoogle(String token) {
        if (!StringUtils.hasText(googleClientId)) {
            throw new IllegalArgumentException("Google client id chua duoc cau hinh.");
        }
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Token Google khong hop le.");
        }
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token="
                + UriUtils.encode(token, StandardCharsets.UTF_8);
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map body = response.getBody();
        if (body == null) {
            throw new IllegalArgumentException("Khong the xac thuc Google token.");
        }
        String aud = value(body, "aud");
        if (!googleClientId.equals(aud)) {
            throw new IllegalArgumentException("Google token khong dung ung dung.");
        }
        String sub = value(body, "sub");
        String email = value(body, "email");
        String name = value(body, "name");
        return new SocialProfile("google", sub, email, name);
    }

    public SocialProfile verifyFacebook(String token) {
        if (!StringUtils.hasText(facebookAppId) || !StringUtils.hasText(facebookAppSecret)) {
            throw new IllegalArgumentException("Facebook app chua duoc cau hinh.");
        }
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Token Facebook khong hop le.");
        }
        String debugUrl = "https://graph.facebook.com/debug_token?input_token="
                + UriUtils.encode(token, StandardCharsets.UTF_8)
                + "&access_token=" + UriUtils.encode(facebookAppId + "|" + facebookAppSecret, StandardCharsets.UTF_8);
        ResponseEntity<Map> debugResponse = restTemplate.getForEntity(debugUrl, Map.class);
        Map debugBody = debugResponse.getBody();
        if (debugBody == null || !debugBody.containsKey("data")) {
            throw new IllegalArgumentException("Khong the xac thuc Facebook token.");
        }
        Map data = (Map) debugBody.get("data");
        Object isValidValue = data.get("is_valid");
        boolean isValid = isValidValue instanceof Boolean && (Boolean) isValidValue;
        String appId = value(data, "app_id");
        if (!isValid || !facebookAppId.equals(appId)) {
            throw new IllegalArgumentException("Facebook token khong hop le.");
        }
        String userId = value(data, "user_id");
        String profileUrl = "https://graph.facebook.com/me?fields=id,name,email&access_token="
                + UriUtils.encode(token, StandardCharsets.UTF_8);
        ResponseEntity<Map> profileResponse = restTemplate.getForEntity(profileUrl, Map.class);
        Map profileBody = profileResponse.getBody();
        if (profileBody == null) {
            throw new IllegalArgumentException("Khong the lay thong tin Facebook.");
        }
        String email = value(profileBody, "email");
        String name = value(profileBody, "name");
        return new SocialProfile("facebook", userId, email, name);
    }

    private String value(Map map, String key) {
        Object value = map.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
