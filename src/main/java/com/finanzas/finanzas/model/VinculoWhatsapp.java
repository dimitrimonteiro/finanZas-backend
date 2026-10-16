package com.finanzas.finanzas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class VinculoWhatsapp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "whatsapp_id", unique = true, nullable = false)
    private String whatsappId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();

    public VinculoWhatsapp() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWhatsappId() { return whatsappId; }
    public void setWhatsappId(String whatsappId) { this.whatsappId = whatsappId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}