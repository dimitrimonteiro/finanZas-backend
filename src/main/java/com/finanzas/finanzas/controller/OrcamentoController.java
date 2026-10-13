package com.finanzas.finanzas.controller;

import com.finanzas.finanzas.model.Orcamento;
import com.finanzas.finanzas.repository.OrcamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orcamentos")
public class OrcamentoController {

    @Autowired
    private OrcamentoRepository orcamentoRepository;

    @GetMapping
    public List<Orcamento> getAll(@RequestParam String userId) {
        return orcamentoRepository.findByUserId(userId);
    }

    @PutMapping("/{categoria}")
    public Orcamento upsert(
            @PathVariable String categoria,
            @RequestBody Orcamento body,
            @RequestParam String userId) {

        Orcamento orcamento = orcamentoRepository
                .findByUserIdAndCategoria(userId, categoria)
                .orElseGet(Orcamento::new);

        orcamento.setUserId(userId);
        orcamento.setCategoria(categoria);
        orcamento.setLimite(body.getLimite());
        return orcamentoRepository.save(orcamento);
    }

    @DeleteMapping("/{categoria}")
    public ResponseEntity<?> delete(@PathVariable String categoria, @RequestParam String userId) {
        orcamentoRepository.findByUserIdAndCategoria(userId, categoria)
                .ifPresent(orcamentoRepository::delete);
        return ResponseEntity.ok().build();
    }
}