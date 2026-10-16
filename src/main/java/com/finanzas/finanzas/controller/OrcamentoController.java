package com.finanzas.finanzas.controller;

import com.finanzas.finanzas.model.Orcamento;
import com.finanzas.finanzas.repository.OrcamentoRepository;
import com.finanzas.finanzas.security.UsuarioAtual;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/orcamentos")
public class OrcamentoController {

    private static final Set<String> CATEGORIAS = Set.of(
            "moradia", "alimentacao", "transporte", "lazer", "saude", "educacao", "assinaturas", "outros");

    @Autowired
    private OrcamentoRepository orcamentoRepository;

    @GetMapping
    public List<Orcamento> getAll() {
        return orcamentoRepository.findByUserId(UsuarioAtual.uid());
    }

    @PutMapping("/{categoria}")
    public Orcamento upsert(@PathVariable String categoria, @Valid @RequestBody Orcamento body) {
        validarCategoria(categoria);
        String userId = UsuarioAtual.uid();

        Orcamento orcamento = orcamentoRepository
                .findByUserIdAndCategoria(userId, categoria)
                .orElseGet(Orcamento::new);

        orcamento.setUserId(userId);
        orcamento.setCategoria(categoria);
        orcamento.setLimite(body.getLimite());
        return orcamentoRepository.save(orcamento);
    }

    @DeleteMapping("/{categoria}")
    public ResponseEntity<Void> delete(@PathVariable String categoria) {
        validarCategoria(categoria);
        orcamentoRepository.findByUserIdAndCategoria(UsuarioAtual.uid(), categoria)
                .ifPresent(orcamentoRepository::delete);
        return ResponseEntity.noContent().build();
    }

    private void validarCategoria(String categoria) {
        if (!CATEGORIAS.contains(categoria)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria inválida.");
        }
    }
}