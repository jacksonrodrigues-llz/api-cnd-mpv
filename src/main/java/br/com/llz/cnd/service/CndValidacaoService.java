package br.com.llz.cnd.service;

import br.com.llz.cnd.dto.CndValidacaoResponse;
import br.com.llz.cnd.dto.UnidadeCndRequest;
import br.com.llz.cnd.entity.UnidadeCnd;
import br.com.llz.cnd.repository.UnidadeCndRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class CndValidacaoService {
    
    private static final int MAX_TENTATIVAS_HORA = 5;
    private final UnidadeCndRepository cndRepository;
    
    public void validarAntiFraude(Long unidadeId, UnidadeCndRequest request, String ip) {
        String hashParametros = calcularHash(unidadeId, request);
        
        Optional<UnidadeCnd> ultimaCnd = cndRepository
            .findByUnidadeIdAndHashParametrosAndDtCriacaoAfter(
                unidadeId, hashParametros, LocalDateTime.now().minusHours(1)
            );
        
        if (ultimaCnd.isPresent()) {
            UnidadeCnd cnd = ultimaCnd.get();
            if (cnd.getTentativasEmissao() >= MAX_TENTATIVAS_HORA) {
                throw new RuntimeException("Muitas tentativas. Tente novamente em 60 minutos");
            }
            
            // Incrementar tentativas
            cnd.setTentativasEmissao(cnd.getTentativasEmissao() + 1);
            cndRepository.save(cnd);
        }
    }
    
    public String gerarCodigoValidacao() {
        // Formato: CND + timestamp + random (ex: CND240315001)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String random = String.format("%03d", new Random().nextInt(1000));
        return "CND" + timestamp + random;
    }
    
    public CndValidacaoResponse validarPorCodigo(String codigo) {
        UnidadeCnd cnd = cndRepository.findByCodigoValidacao(codigo)
            .orElseThrow(() -> new RuntimeException("CND não encontrada"));
        
        return CndValidacaoResponse.builder()
            .codigoValidacao(codigo)
            .valido("ASSINADO".equals(cnd.getStatus()))
            .status(cnd.getStatus())
            .dataEmissao(cnd.getDtCriacao())
            .dataAssinatura(cnd.getDtAssinatura())
            .nomeCondominio(cnd.getUnidade() != null && cnd.getUnidade().getCondominio() != null ? 
                cnd.getUnidade().getCondominio().getRazaoSocial() : "N/A")
            .unidadeCodigo(cnd.getUnidade() != null ? cnd.getUnidade().getCodigo() : "N/A")
            .bloco(cnd.getUnidade() != null ? cnd.getUnidade().getBloco() : "N/A")
            .hashDocumento(cnd.getDocumentoAssinado() != null ? 
                DigestUtils.sha256Hex(cnd.getDocumentoAssinado()) : null)
            .dadosAssinatura(cnd.getDadosAssinatura())
            .build();
    }
    
    public byte[] obterDocumentoAssinado(String codigo) {
        UnidadeCnd cnd = cndRepository.findByCodigoValidacao(codigo)
            .orElseThrow(() -> new RuntimeException("CND não encontrada"));
        
        if (cnd.getDocumentoAssinado() != null) {
            return cnd.getDocumentoAssinado();
        } else if (cnd.getDocumentoPdf() != null) {
            return cnd.getDocumentoPdf();
        } else {
            throw new RuntimeException("Documento não disponível");
        }
    }
    
    private String calcularHash(Long unidadeId, UnidadeCndRequest request) {
        String dados = unidadeId + "|" + request.getComPeriodo() + "|" + request.getComAssinatura();
        return DigestUtils.sha256Hex(dados);
    }
}