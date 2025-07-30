package br.com.llz.cnd.repository;

import br.com.llz.cnd.entity.UnidadeCnd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UnidadeCndRepository extends JpaRepository<UnidadeCnd, Long> {
    
    Optional<UnidadeCnd> findByCodigoValidacao(String codigoValidacao);
    
    Optional<UnidadeCnd> findByCodigoPlataforma(String codigoPlataforma);
    
    @Query("SELECT c FROM UnidadeCnd c WHERE c.unidadeId = :unidadeId " +
           "AND c.hashParametros = :hashParametros " +
           "AND c.dtCriacao > :dataLimite")
    Optional<UnidadeCnd> findByUnidadeIdAndHashParametrosAndDtCriacaoAfter(
        @Param("unidadeId") Long unidadeId,
        @Param("hashParametros") String hashParametros,
        @Param("dataLimite") LocalDateTime dataLimite
    );
}