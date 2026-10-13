package com.finanzas.finanzas.repository;

import com.finanzas.finanzas.model.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {
    List<Orcamento> findByUserId(String userId);
    Optional<Orcamento> findByUserIdAndCategoria(String userId, String categoria);
}