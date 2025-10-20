package com.txq.domain.config;

import com.txq.domain.repository.UserRepository;
import com.txq.domain.service.UserDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 领域服务配置
 */
@Configuration
public class DomainConfig {

    @Bean
    public UserDomainService userDomainService(UserRepository userRepository) {
        return new UserDomainService(userRepository);
    }
}