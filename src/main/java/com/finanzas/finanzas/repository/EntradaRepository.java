package com.finanzas.finanzas.repository;

import com.finanzas.finanzas.model.Entrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface EntradaRepository extends JpaRepository<Entrada, Long> {
    List<Entrada> findByUserId(String userId);

    @Query("SELECT COALESCE(SUM(e.valor), 0) FROM Entrada e WHERE e.userId = :userId")
    BigDecimal sumValorByUserId(String userId);

    boolean existsByRecorrenteIdAndDataBetween(Long recorrenteId, java.time.LocalDate inicio, java.time.LocalDate fim);
}