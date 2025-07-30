package br.com.llz.cnd.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "unidade_cnd")
@Data
public class UnidadeCnd {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "codigo_validacao", unique = true, nullable = false)
    private String codigoValidacao;
    
    @Column(name = "unidade_id", nullable = false)
    private Long unidadeId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", insertable = false, updatable = false)
    private Unidade unidade;
    
    @Column(name = "hash_parametros", nullable = false)
    private String hashParametros;
    
    private String status = "PROCESSANDO";
    
    @Column(name = "canal_emissao", nullable = false)
    private String canalEmissao;
    
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "documento_pdf")
    private byte[] documentoPdf;
    
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "documento_assinado")
    private byte[] documentoAssinado;
    
    @Column(name = "codigo_plataforma")
    private String codigoPlataforma;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dados_assinatura")
    private Map<String, Object> dadosAssinatura;
    
    @Column(name = "dt_criacao")
    private LocalDateTime dtCriacao = LocalDateTime.now();
    
    @Column(name = "dt_assinatura")
    private LocalDateTime dtAssinatura;
    
    @Column(name = "dt_expiracao")
    private LocalDateTime dtExpiracao;
    
    @Column(name = "tentativas_emissao")
    private Integer tentativasEmissao = 1;
    
    @Column(name = "ip_origem")
    private String ipOrigem;
    
    @Column(name = "reg_ativo")
    private Boolean regAtivo = true;
    
    @Column(name = "dt_alteracao")
    private LocalDateTime dtAlteracao = LocalDateTime.now();
    
    @Column(name = "usr_criacao")
    private Long usrCriacao;
    
    @Column(name = "usr_alteracao")
    private Long usrAlteracao;
}