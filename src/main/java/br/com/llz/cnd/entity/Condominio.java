package br.com.llz.cnd.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "condominio")
@Data
public class Condominio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "razao_social", nullable = false)
    private String razaoSocial;
    
    @Column(name = "endereco_id")
    private Long enderecoId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endereco_id", insertable = false, updatable = false)
    private Endereco endereco;
    
    @Column(name = "reg_ativo")
    private Boolean regAtivo = true;
    
    @Column(name = "dt_criacao")
    private LocalDateTime dtCriacao = LocalDateTime.now();
}