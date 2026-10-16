package com.finanzas.finanzas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Getter
@Setter
public class Saida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Size(max = 255)
    private String descricao;
    @Pattern(regexp = "moradia|alimentacao|transporte|lazer|saude|educacao|assinaturas|outros|metas")
    private String categoria = "outros";
    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("999999999.99")
    private BigDecimal valor;
    @NotNull
    private LocalDate data;
    private Long metaId;
    @Pattern(regexp = "fixa|variável|aporte")
    private String tipo;
    private String userId;
    private Long recorrenteId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getRecorrenteId() { return recorrenteId; }

    public void setRecorrenteId(Long recorrenteId) { this.recorrenteId = recorrenteId; }

    public String getCategoria() { return categoria; }

    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Long getMetaId() { return metaId; }

    public void setMetaId(Long metaId) { this.metaId = metaId; }
}