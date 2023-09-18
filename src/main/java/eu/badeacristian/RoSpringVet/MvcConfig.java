package eu.badeacristian.RoSpringVet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
	
	@Value("${external.resources.path}")
	private String path;
	@Value("${external.resources.dir}")
	private String resources;
	
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		
		//important: calea catre locatia catre resursa trebuie mereu sa se termine cu "/"
		
		registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
		
		registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");
		
		registry.addResourceHandler("/"+resources+"/**").addResourceLocations("file:"+path);

	}

}