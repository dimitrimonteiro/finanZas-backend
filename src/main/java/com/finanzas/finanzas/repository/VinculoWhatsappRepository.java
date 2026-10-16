package com.finanzas.finanzas.repository;

import com.finanzas.finanzas.model.VinculoWhatsapp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VinculoWhatsappRepository extends JpaRepository<VinculoWhatsapp, Long> {
    Optional<VinculoWhatsapp> findByWhatsappId(String whatsappId);
    List<VinculoWhatsapp> findByUserId(String userId);
    void deleteByWhatsappId(String whatsappId);
    void deleteByUserId(String userId);
}