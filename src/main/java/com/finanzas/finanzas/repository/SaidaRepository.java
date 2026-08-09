package com.finanzas.finanzas.repository;

import com.finanzas.finanzas.model.Saida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SaidaRepository extends JpaRepository<Saida, Long> {
    List<Saida> findByUserId(String userId);

    @Query("SELECT COALESCE(SUM(s.valor), 0) FROM Saida s WHERE s.userId = :userId")
    BigDecimal sumValorByUserId(String userId);

    boolean existsByRecorrenteIdAndDataBetween(Long recorrenteId, java.time.LocalDate inicio, java.time.LocalDate fim);
}