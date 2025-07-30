package br.com.llz.cnd.controller;

import br.com.llz.cnd.dto.CndValidacaoResponse;
import br.com.llz.cnd.dto.UnidadeCndPdfResponse;
import br.com.llz.cnd.dto.UnidadeCndRequest;
import br.com.llz.cnd.service.UnidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cnd")
@RequiredArgsConstructor
@Tag(name = "CND - Certidão Negativa de Débitos", description = "APIs para emissão e validação de CND")
public class CndController {
    
    private final UnidadeService unidadeService;
    
    @Operation(summary = "Emitir CND em PDF com Assinatura Digital",
               description = "Emite uma Certidão Negativa de Débitos em formato PDF com assinatura digital")
    @PostMapping("/emitir/{unidadeId}")
    public ResponseEntity<UnidadeCndPdfResponse> emitirCndPdf(
            @Parameter(description = "ID da unidade") @PathVariable Long unidadeId,
            @Parameter(description = "Parâmetros da CND") @RequestBody UnidadeCndRequest request,
            HttpServletRequest httpRequest) {
        
        String ip = getClientIpAddress(httpRequest);
        UnidadeCndPdfResponse response = unidadeService.emitirCndPdf(unidadeId, request, ip);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Validar CND por Código",
               description = "Valida uma CND através do código de validação")
    @GetMapping("/validar/{codigo}")
    public ResponseEntity<CndValidacaoResponse> validarCnd(
            @Parameter(description = "Código de validação da CND") @PathVariable String codigo) {
        
        CndValidacaoResponse response = unidadeService.validarCnd(codigo);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Download da CND Assinada",
               description = "Faz o download do PDF da CND assinada")
    @GetMapping("/download/{codigo}")
    public ResponseEntity<byte[]> downloadCnd(
            @Parameter(description = "Código de validação da CND") @PathVariable String codigo) {
        
        byte[] pdf = unidadeService.downloadCnd(codigo);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "cnd-" + codigo + ".pdf");
        headers.setContentLength(pdf.length);
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdf);
    }
    
    @Operation(summary = "Validar Hash do Documento",
               description = "Valida se o hash fornecido corresponde ao documento")
    @PostMapping("/validar-hash/{codigo}")
    public ResponseEntity<Boolean> validarHash(
            @Parameter(description = "Código de validação da CND") @PathVariable String codigo,
            @Parameter(description = "Hash para validação") @RequestBody String hash) {
        
        try {
            byte[] documento = unidadeService.downloadCnd(codigo);
            String hashCalculado = org.apache.commons.codec.digest.DigestUtils.sha256Hex(documento);
            boolean valido = hashCalculado.equals(hash.trim());
            
            return ResponseEntity.ok(valido);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}