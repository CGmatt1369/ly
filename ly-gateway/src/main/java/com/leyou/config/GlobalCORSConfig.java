package com.leyou.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/8/29
 * @描述 CORS的跨域过滤器
 */
@Configuration
public class GlobalCORSConfig {

    @Bean
    public CorsFilter corsFilter() {
        //        1.添加cors的配置信息
        CorsConfiguration config = new CorsConfiguration();
//          允许的域,不要写*，否则cookie就无法使用了
        config.addAllowedOrigin("http://manage.leyou.com");
        config.addAllowedOrigin("http://www.leyou.com");
//          是否允许发送cookie
        config.setAllowCredentials(true);
//          允许的请求方式
        config.addAllowedMethod(HttpMethod.GET);
        config.addAllowedMethod(HttpMethod.POST);
        config.addAllowedMethod(HttpMethod.PUT);
        config.addAllowedMethod(HttpMethod.DELETE);
//          允许的头信息
        config.addAllowedHeader("*");
//          访问有效期 s
        config.setMaxAge(360000L);

//       2.添加映射路径，我们拦截一切请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",config);
//       3.返回新的CORSFilter
        return new CorsFilter(source);
    }
}
