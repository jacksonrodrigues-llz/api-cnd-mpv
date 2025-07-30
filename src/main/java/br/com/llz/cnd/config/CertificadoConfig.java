package br.com.llz.cnd.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;

@Configuration
@Slf4j
public class CertificadoConfig {
    
    @Value("${cnd.certificado.path}")
    private String certificadoPath;
    
    @Value("${cnd.certificado.password}")
    private String certificadoPassword;
    
    @Value("${cnd.certificado.alias}")
    private String certificadoAlias;
    
    @Bean
    @ConditionalOnProperty(name = "cnd.certificado.path")
    public KeyStore cndKeyStore() {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(certificadoPath)) {
                if (is == null) {
                    log.warn("Certificado não encontrado em: {}", certificadoPath);
                    return null;
                }
                keyStore.load(is, certificadoPassword.toCharArray());
                log.info("Certificado carregado com sucesso: {}", certificadoPath);
                return keyStore;
            }
        } catch (Exception e) {
            log.error("Erro ao carregar certificado CND: {}", e.getMessage());
            return null;
        }
    }
    
    @Bean
    @ConditionalOnProperty(name = "cnd.certificado.path")
    public PrivateKey cndPrivateKey(KeyStore cndKeyStore) {
        if (cndKeyStore == null) {
            return null;
        }
        
        try {
            PrivateKey privateKey = (PrivateKey) cndKeyStore.getKey(certificadoAlias, certificadoPassword.toCharArray());
            log.info("Chave privada carregada com sucesso");
            return privateKey;
        } catch (Exception e) {
            log.error("Erro ao carregar chave privada: {}", e.getMessage());
            return null;
        }
    }
}