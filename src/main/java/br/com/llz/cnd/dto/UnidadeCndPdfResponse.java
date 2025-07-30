package br.com.llz.cnd.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UnidadeCndPdfResponse {
    private String codigoValidacao;
    private String status;
    private LocalDateTime dataEmissao;
    private LocalDateTime dataExpiracao;
    private String urlValidacao;
    private String hashDocumento;
}