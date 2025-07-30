package br.com.llz.cnd.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class CndValidacaoResponse {
    private String codigoValidacao;
    private boolean valido;
    private String status;
    private LocalDateTime dataEmissao;
    private LocalDateTime dataAssinatura;
    private String nomeCondominio;
    private String unidadeCodigo;
    private String bloco;
    private String hashDocumento;
    private Map<String, Object> dadosAssinatura;
}