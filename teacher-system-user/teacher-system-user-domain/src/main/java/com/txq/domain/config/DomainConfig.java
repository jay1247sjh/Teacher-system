package com.txq.domain.config;

import com.txq.domain.infra.repository.RoleRepository;
import com.txq.domain.infra.repository.UserRepository;
import com.txq.domain.infra.repository.UserRoleRepository;
import com.txq.domain.service.UserDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 领域服务配置
 */
@Configuration
public class DomainConfig {

    @Bean
    public UserDomainService userDomainService(UserRepository userRepository,
                                               UserRoleRepository userRoleRepository,
                                               RoleRepository roleRepository) {
        return new UserDomainService(
                userRepository,
                userRoleRepository,
                roleRepository
        );
    }
}