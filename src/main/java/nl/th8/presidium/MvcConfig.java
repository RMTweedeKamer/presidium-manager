package nl.th8.presidium;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    public MvcConfig() {
    }

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/resources/**")
//                .addResourceLocations("/resources/");
//        registry.addResourceHandler("/")
//                .addResourceLocations("resources/**")
//                .addResourceLocations("classpath:/static/");
//    }

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
    }

    @Bean
    public ViewResolver configureViewResolver() {
        InternalResourceViewResolver vr = new InternalResourceViewResolver();
        vr.setRedirectHttp10Compatible(false);
        // other options
        return vr;
    }
}
