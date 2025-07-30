package br.com.llz.cnd.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "unidade")
@Data
public class Unidade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String codigo;
    
    private String bloco;
    
    @Column(name = "condominio_id", nullable = false)
    private Long condominioId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condominio_id", insertable = false, updatable = false)
    private Condominio condominio;
    
    private String situacao = "ATIVO";
    
    @Column(name = "reg_ativo")
    private Boolean regAtivo = true;
    
    @Column(name = "dt_criacao")
    private LocalDateTime dtCriacao = LocalDateTime.now();
    
    @Column(name = "dt_alteracao")
    private LocalDateTime dtAlteracao = LocalDateTime.now();
}