package com.finanzas.finanzas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Getter
@Setter
public class MetaFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String nomeMeta;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("999999999.99")
    private BigDecimal valorObjetivo;

    @DecimalMin("0.00")
    private BigDecimal valorAtual;

    @NotNull
    private LocalDate dataLimite;

    @Pattern(regexp = "viagem|reserva|compra|investimento|outros")
    private String categoria;

    private String userId;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();

    public MetaFinanceira() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeMeta() {
        return nomeMeta;
    }

    public void setNomeMeta(String nomeMeta) {
        this.nomeMeta = nomeMeta;
    }

    public BigDecimal getValorObjetivo() {
        return valorObjetivo;
    }

    public void setValorObjetivo(BigDecimal valorObjetivo) {
        this.valorObjetivo = valorObjetivo;
    }

    public BigDecimal getValorAtual() {
        return valorAtual;
    }

    public void setValorAtual(BigDecimal valorAtual) {
        this.valorAtual = valorAtual;
    }

    public LocalDate getDataLimite() {
        return dataLimite;
    }

    public void setDataLimite(LocalDate dataLimite) {
        this.dataLimite = dataLimite;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}