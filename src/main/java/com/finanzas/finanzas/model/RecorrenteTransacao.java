package com.finanzas.finanzas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Getter
@Setter
public class RecorrenteTransacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;

    private BigDecimal valor;

    private String tipoTransacao;

    private String tipoSaida;

    private Boolean salario = false;

    private Integer diaVencimento;

    private Boolean ativa = true;

    private String userId;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();

    public RecorrenteTransacao() {}

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getDescricao() { return descricao; }

    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValor() { return valor; }

    public void setValor(BigDecimal valor) { this.valor = valor; }

    public String getTipoTransacao() { return tipoTransacao; }

    public void setTipoTransacao(String tipoTransacao) { this.tipoTransacao = tipoTransacao; }

    public String getTipoSaida() { return tipoSaida; }

    public void setTipoSaida(String tipoSaida) { this.tipoSaida = tipoSaida; }

    public Boolean getSalario() { return salario; }

    public void setSalario(Boolean salario) { this.salario = salario; }

    public Integer getDiaVencimento() { return diaVencimento; }

    public void setDiaVencimento(Integer diaVencimento) { this.diaVencimento = diaVencimento; }

    public Boolean getAtiva() { return ativa; }

    public void setAtiva(Boolean ativa) { this.ativa = ativa; }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public LocalDateTime getCriadoEm() { return criadoEm; }

    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}