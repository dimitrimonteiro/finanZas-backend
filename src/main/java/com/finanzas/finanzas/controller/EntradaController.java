package com.finanzas.finanzas.controller;

import com.finanzas.finanzas.model.Entrada;
import com.finanzas.finanzas.repository.EntradaRepository;
import com.finanzas.finanzas.security.UsuarioAtual;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/entradas")
public class EntradaController {

    @Autowired
    private EntradaRepository entradaRepository;

    @GetMapping
    public List<Entrada> getAllEntradas() {
        return entradaRepository.findByUserId(UsuarioAtual.uid());
    }

    @GetMapping("/total")
    public Map<String, BigDecimal> getTotalEntradas() {
        return Map.of("total", entradaRepository.sumValorByUserId(UsuarioAtual.uid()));
    }

    @GetMapping("/{id}")
    public Entrada getEntradaById(@PathVariable Long id) {
        return buscar(id);
    }

    @PostMapping
    public Entrada createEntrada(@Valid @RequestBody Entrada entrada) {
        entrada.setId(null); // impede sobrescrever um registro existente enviando um id
        entrada.setUserId(UsuarioAtual.uid());
        entrada.setRecorrenteId(null); // só o sistema define
        if (entrada.getCategoria() == null) entrada.setCategoria("outros");
        entrada.setSalario("salario".equals(entrada.getCategoria()));
        return entradaRepository.save(entrada);
    }

    @PutMapping("/{id}")
    public Entrada updateEntrada(@PathVariable Long id, @Valid @RequestBody Entrada detalhes) {
        Entrada entrada = buscar(id);
        entrada.setDescricao(detalhes.getDescricao());
        entrada.setValor(detalhes.getValor());
        entrada.setData(detalhes.getData());
        entrada.setCategoria(detalhes.getCategoria() != null ? detalhes.getCategoria() : "outros");
        entrada.setSalario("salario".equals(entrada.getCategoria()));
        return entradaRepository.save(entrada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntrada(@PathVariable Long id) {
        entradaRepository.delete(buscar(id));
        return ResponseEntity.noContent().build();
    }

    private Entrada buscar(Long id) {
        return entradaRepository.findByIdAndUserId(id, UsuarioAtual.uid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada não encontrada."));
    }
}