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
public class Entrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Size(max = 255)
    private String descricao;
    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("999999999.99")
    private BigDecimal valor;
    @NotNull
    private LocalDate data;
    private boolean salario;
    @Pattern(regexp = "salario|freelance|investimento|presente|reembolso|outros")
    private String categoria = "outros";
    private Long recorrenteId;
    private String userId;

    public Entrada() {}

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

    public boolean isSalario() {
        return salario;
    }

    public void setSalario(boolean salario) {
        this.salario = salario;
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
}