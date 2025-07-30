package br.com.llz.cnd.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UnidadeCndData {
    private String nomeCondominio;
    private String logradouro;
    private String numero;
    private String bairro;
    private String cidade;
    private String uf;
    private String cep;
    private String unidadeCodigo;
    private String bloco;
    private String validadeDocumento;
    private String usuario;
    private List<String> dataVencimento;
    private Boolean comPeriodo;
}