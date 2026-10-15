package com.finanzas.finanzas.controller;

import com.finanzas.finanzas.model.Saida;
import com.finanzas.finanzas.repository.SaidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/saidas")
public class SaidaController {

    @Autowired
    private SaidaRepository saidaRepository;

    @GetMapping
    public List<Saida> getAllSaidas(@RequestParam String userId) {
        return saidaRepository.findByUserId(userId);
    }

    @GetMapping("/total")
    public Map<String, BigDecimal> getTotalSaidas(@RequestParam String userId) {
        BigDecimal total = saidaRepository.sumValorByUserId(userId);
        return Map.of("total", total);
    }

    @PostMapping
    public Saida createSaida(@RequestBody Saida saida, @RequestParam String userId) {
        saida.setUserId(userId);
        return saidaRepository.save(saida);
    }

    @GetMapping("/{id}")
    public Saida getSaidaById(@PathVariable Long id, @RequestParam String userId) {
        return saidaRepository.findById(id).filter(s -> s.getUserId().equals(userId)).orElse(null);
    }

    @PutMapping("/{id}")
    public Saida updateSaida(@PathVariable Long id, @RequestBody Saida saidaDetails, @RequestParam String userId) {
        Saida saida = saidaRepository.findById(id).orElse(null);
        if (saida != null && saida.getUserId().equals(userId) && saida.getMetaId() == null) {
            saida.setDescricao(saidaDetails.getDescricao());
            saida.setValor(saidaDetails.getValor());
            saida.setData(saidaDetails.getData());
            saida.setTipo(saidaDetails.getTipo());
            saida.setCategoria(saidaDetails.getCategoria());
            return saidaRepository.save(saida);
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteSaida(@PathVariable Long id, @RequestParam String userId) {
        Saida saida = saidaRepository.findById(id).orElse(null);
        if (saida != null && saida.getUserId().equals(userId) && saida.getMetaId() == null) {
            saidaRepository.deleteById(id);
        }
    }
}