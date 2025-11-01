package com.txq.domain.config;

import com.txq.domain.infra.repository.TableRepository;
import com.txq.domain.service.TableDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 领域服务配置
 */
@Configuration
public class DomainConfig {

    @Bean
    public TableDomainService tableDomainService(TableRepository tableRepository) {
        return new TableDomainService(tableRepository);
    }
}

