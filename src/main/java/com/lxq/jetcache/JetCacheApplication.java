package com.lxq.jetcache;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableMethodCache(basePackages = "com.lxq.jetcache")
@SpringBootApplication
public class JetCacheApplication {
    public static void main(String[] args) {
        SpringApplication.run(JetCacheApplication.class, args);
    }
}
