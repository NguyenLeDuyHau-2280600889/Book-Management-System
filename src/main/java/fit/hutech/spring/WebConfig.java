package fit.hutech.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.ParseException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final Path uploadPath;

    public WebConfig(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(new org.springframework.format.Formatter<Double>() {
            @Override
            public Double parse(String text, Locale locale) throws ParseException {
                if (text == null || text.trim().isEmpty()) {
                    return null;
                }
                String normalized = text.trim().replace(",", "");
                try {
                    return Double.valueOf(normalized);
                } catch (NumberFormatException ex) {
                    throw new ParseException("Invalid number: " + text, 0);
                }
            }

            @Override
            public String print(Double object, Locale locale) {
                if (object == null) {
                    return "";
                }
                return object.toString();
            }
        });
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath.toUri().toString());
    }
}
