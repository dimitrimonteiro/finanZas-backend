package com.finanzas.finanzas.repository;

import com.finanzas.finanzas.model.MetaFinanceira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetaFinanceiraRepository extends JpaRepository<MetaFinanceira, Long> {
    List<MetaFinanceira> findByUserId(String userId);

    Optional<MetaFinanceira> findByIdAndUserId(Long id, String userId);
}