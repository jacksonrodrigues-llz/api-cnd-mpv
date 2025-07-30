package br.com.llz.cnd.dto;

import lombok.Data;

@Data
public class UnidadeCndRequest {
    private Boolean comAssinatura = false;
    private Boolean comPeriodo = false;
    private String canalEmissao = "WEB";
    
    public static Boolean isTrue(Boolean param) {
        return Boolean.TRUE.equals(param);
    }
}