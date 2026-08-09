package com.finanzas.finanzas.controller;

import com.finanzas.finanzas.model.Entrada;
import com.finanzas.finanzas.repository.EntradaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/entradas")
public class EntradaController {

    @Autowired
    private EntradaRepository entradaRepository;

    @GetMapping
    public List<Entrada> getAllEntradas(@RequestParam String userId) {
        return entradaRepository.findByUserId(userId);
    }

    @GetMapping("/total")
    public Map<String, BigDecimal> getTotalEntradas(@RequestParam String userId) {
        BigDecimal total = entradaRepository.sumValorByUserId(userId);
        return Map.of("total", total);
    }

    @PostMapping
    public Entrada createEntrada(@RequestBody Entrada entrada, @RequestParam String userId) {
        entrada.setUserId(userId);
        entrada.setSalario("salario".equals(entrada.getCategoria()));
        return entradaRepository.save(entrada);
    }

    @GetMapping("/{id}")
    public Entrada getEntradaById(@PathVariable Long id, @RequestParam String userId) {
        return entradaRepository.findById(id).filter(e -> e.getUserId().equals(userId)).orElse(null);
    }

    @PutMapping("/{id}")
    public Entrada updateEntrada(@PathVariable Long id, @RequestBody Entrada entradaDetails, @RequestParam String userId) {
        Entrada entrada = entradaRepository.findById(id).orElse(null);
        if (entrada != null && entrada.getUserId().equals(userId)) {
            entrada.setDescricao(entradaDetails.getDescricao());
            entrada.setValor(entradaDetails.getValor());
            entrada.setData(entradaDetails.getData());
            entrada.setCategoria(entradaDetails.getCategoria());
            entrada.setSalario("salario".equals(entradaDetails.getCategoria()));
            return entradaRepository.save(entrada);
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteEntrada(@PathVariable Long id, @RequestParam String userId) {
        Entrada entrada = entradaRepository.findById(id).orElse(null);
        if (entrada != null && entrada.getUserId().equals(userId)) {
            entradaRepository.deleteById(id);
        }
    }
}