package br.com.llz.cnd.repository;

import br.com.llz.cnd.entity.Unidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UnidadeRepository extends JpaRepository<Unidade, Long> {
    
    Optional<Unidade> findByIdAndRegAtivoIsTrue(Long id);
    
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
           "FROM Unidade u WHERE u.id = :unidadeId AND u.regAtivo = true")
    boolean isUnidadeAdimplente(@Param("unidadeId") Long unidadeId);
}