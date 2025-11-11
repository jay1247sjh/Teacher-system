package com.txq.application.entity.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 密钥配置类
 */
@Component
@Getter
public class KeyPairConfig {

    /**
     * 公钥
     */
    @Value("${keypair.public-key}")
    private String publicKey;

    /**
     * 私钥
     */
    @Value("${keypair.private-key}")
    private String privateKey;
}
