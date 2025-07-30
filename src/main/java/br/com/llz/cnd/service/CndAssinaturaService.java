package br.com.llz.cnd.service;

import br.com.llz.cnd.entity.UnidadeCnd;
import br.com.llz.cnd.repository.UnidadeCndRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CndAssinaturaService {
    
    private final UnidadeCndRepository cndRepository;
    
    @Value("${cnd.certificado.path}")
    private String certificadoPath;
    
    @Value("${cnd.certificado.password}")
    private String certificadoPassword;
    
    @Value("${cnd.certificado.alias}")
    private String certificadoAlias;
    
    @Async
    public void processarAssinaturaAsync(Long cndId) {
        try {
            UnidadeCnd cnd = cndRepository.findById(cndId).orElseThrow();
            
            // Simular assinatura digital (em produção seria integração com plataforma)
            byte[] documentoAssinado = assinarDocumentoLocal(cnd.getDocumentoPdf());
            
            // Calcular hash do documento assinado
            String hashDocumento = DigestUtils.sha256Hex(documentoAssinado);
            
            // Dados da assinatura simulada
            Map<String, Object> dadosAssinatura = new HashMap<>();
            dadosAssinatura.put("algoritmo", "SHA256withRSA");
            dadosAssinatura.put("certificado", "CN=LLZ Garantidora, OU=TI, O=LLZ");
            dadosAssinatura.put("timestamp", LocalDateTime.now().toString());
            dadosAssinatura.put("hash", hashDocumento);
            
            // Atualizar registro
            cnd.setDocumentoAssinado(documentoAssinado);
            cnd.setStatus("ASSINADO");
            cnd.setDtAssinatura(LocalDateTime.now());
            cnd.setDadosAssinatura(dadosAssinatura);
            
            cndRepository.save(cnd);
            
            log.info("CND {} assinada com sucesso", cnd.getCodigoValidacao());
            
        } catch (Exception e) {
            log.error("Erro ao processar assinatura da CND {}: {}", cndId, e.getMessage());
            // Marcar como erro
            UnidadeCnd cnd = cndRepository.findById(cndId).orElse(null);
            if (cnd != null) {
                cnd.setStatus("ERRO");
                cndRepository.save(cnd);
            }
        }
    }
    
    private byte[] assinarDocumentoLocal(byte[] documento) {
        try {
            // Carregar certificado
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(getClass().getClassLoader().getResourceAsStream(certificadoPath), 
                         certificadoPassword.toCharArray());
            
            // Obter chave privada
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(certificadoAlias, 
                                                                certificadoPassword.toCharArray());
            
            // Assinar documento
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(documento);
            byte[] assinatura = signature.sign();
            
            // Para este MVP, vamos apenas adicionar a assinatura ao final do documento
            // Em produção, seria usado iText para assinatura PDF adequada
            byte[] documentoAssinado = new byte[documento.length + assinatura.length];
            System.arraycopy(documento, 0, documentoAssinado, 0, documento.length);
            System.arraycopy(assinatura, 0, documentoAssinado, documento.length, assinatura.length);
            
            return documentoAssinado;
            
        } catch (Exception e) {
            log.error("Erro ao assinar documento localmente: {}", e.getMessage());
            // Em caso de erro, retorna o documento original
            return documento;
        }
    }
    
    public String obterHashDocumento(byte[] documento) {
        return DigestUtils.sha256Hex(documento);
    }
}