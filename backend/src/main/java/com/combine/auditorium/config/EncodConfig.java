package com.combine.auditorium.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class EncodConfig implements WebMvcConfigurer {

    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 移除默认的 StringHttpMessageConverter (通常是 ISO-8859-1)
        converters.removeIf(converter -> converter instanceof StringHttpMessageConverter);
        // 添加 UTF-8 编码的 Converter
        converters.add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }
}

