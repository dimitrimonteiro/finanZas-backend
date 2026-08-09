package com.finanzas.finanzas.repository;

import com.finanzas.finanzas.model.RecorrenteTransacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecorrenteTransacaoRepository extends JpaRepository<RecorrenteTransacao, Long> {
    List<RecorrenteTransacao> findByUserId(String userId);
    List<RecorrenteTransacao> findByUserIdAndAtivaTrue(String userId);
}