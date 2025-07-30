package br.com.llz.cnd.service;

import br.com.llz.cnd.dto.*;
import br.com.llz.cnd.entity.UnidadeCnd;
import br.com.llz.cnd.repository.UnidadeCndRepository;
import br.com.llz.cnd.repository.UnidadeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnidadeService {
    
    private final UnidadeRepository unidadeRepository;
    private final UnidadeCndRepository cndRepository;
    private final CndPdfService cndPdfService;
    private final CndAssinaturaService cndAssinaturaService;
    private final CndValidacaoService cndValidacaoService;
    
    @Transactional
    public UnidadeCndPdfResponse emitirCndPdf(Long unidadeId, UnidadeCndRequest request, String ip) {
        // 1. Validar se unidade existe e está adimplente
        var unidade = unidadeRepository.findByIdAndRegAtivoIsTrue(unidadeId)
            .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));
        
        boolean isAdimplente = unidadeRepository.isUnidadeAdimplente(unidadeId);
        //isAdimplente = true;
        if (!isAdimplente) {
            throw new RuntimeException("Unidade possui débitos em aberto. CND não pode ser emitida.");
        }
        
        // 2. Validar anti-fraude
        cndValidacaoService.validarAntiFraude(unidadeId, request, ip);
        
        // 3. Gerar código único
        String codigo = cndValidacaoService.gerarCodigoValidacao();
        
        // 4. Coletar dados da unidade (simulando dados reais)
        UnidadeCndData dadosUnidade = coletarDadosUnidade(unidade.getId(), request);
        
        // 5. Gerar PDF
        byte[] pdf = cndPdfService.gerarPdf(dadosUnidade, codigo);
        
        // 6. Salvar registro
        UnidadeCnd cnd = salvarCndEmitida(unidadeId, request, codigo, pdf, ip);
        
        // 7. Enviar para assinatura (assíncrono)
        cndAssinaturaService.processarAssinaturaAsync(cnd.getId());
        
        return UnidadeCndPdfResponse.builder()
            .codigoValidacao(codigo)
            .status("PROCESSANDO")
            .dataEmissao(LocalDateTime.now())
            .dataExpiracao(LocalDateTime.now().plusDays(30))
            .urlValidacao("http://localhost:8080/api/cnd/validar/" + codigo)
            .hashDocumento(cndPdfService.calcularHashDocumento(pdf))
            .build();
    }
    
    private UnidadeCndData coletarDadosUnidade(Long unidadeId, UnidadeCndRequest request) {
        // Simulando dados reais - em produção viria do banco
        List<String> datasVencimento = request.getComPeriodo() ? 
            Arrays.asList("01/01/2024", "01/02/2024", "01/03/2024") : null;
        
        return UnidadeCndData.builder()
            .nomeCondominio("Condomínio Residencial Jardim das Flores")
            .logradouro("Rua das Flores")
            .numero("123")
            .bairro("Centro")
            .cidade("Belo Horizonte")
            .uf("MG")
            .cep("30112-000")
            .unidadeCodigo("101")
            .bloco("A")
            .validadeDocumento(LocalDateTime.now().plusDays(30).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            .usuario("Sistema Automatizado")
            .dataVencimento(datasVencimento)
            .comPeriodo(request.getComPeriodo())
            .build();
    }
    
    private UnidadeCnd salvarCndEmitida(Long unidadeId, UnidadeCndRequest request, String codigo, byte[] pdf, String ip) {
        String hashParametros = DigestUtils.sha256Hex(unidadeId + "|" + request.getComPeriodo() + "|" + request.getComAssinatura());
        
        UnidadeCnd cnd = new UnidadeCnd();
        cnd.setCodigoValidacao(codigo);
        cnd.setUnidadeId(unidadeId);
        cnd.setHashParametros(hashParametros);
        cnd.setStatus("PROCESSANDO");
        cnd.setCanalEmissao(request.getCanalEmissao());
        cnd.setDocumentoPdf(pdf);
        cnd.setDtExpiracao(LocalDateTime.now().plusDays(30));
        cnd.setIpOrigem(ip);
        
        return cndRepository.save(cnd);
    }
    
    public CndValidacaoResponse validarCnd(String codigo) {
        return cndValidacaoService.validarPorCodigo(codigo);
    }
    
    public byte[] downloadCnd(String codigo) {
        return cndValidacaoService.obterDocumentoAssinado(codigo);
    }
}