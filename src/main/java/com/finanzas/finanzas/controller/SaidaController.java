package com.finanzas.finanzas.controller;

import com.finanzas.finanzas.model.Saida;
import com.finanzas.finanzas.repository.SaidaRepository;
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
@RequestMapping("/api/saidas")
public class SaidaController {

    @Autowired
    private SaidaRepository saidaRepository;

    @GetMapping
    public List<Saida> getAllSaidas() {
        return saidaRepository.findByUserId(UsuarioAtual.uid());
    }

    @GetMapping("/total")
    public Map<String, BigDecimal> getTotalSaidas() {
        return Map.of("total", saidaRepository.sumValorByUserId(UsuarioAtual.uid()));
    }

    @GetMapping("/{id}")
    public Saida getSaidaById(@PathVariable Long id) {
        return buscar(id);
    }

    @PostMapping
    public Saida createSaida(@Valid @RequestBody Saida saida) {
        rejeitarValoresReservados(saida);
        saida.setId(null); // impede sobrescrever um registro existente enviando um id
        saida.setUserId(UsuarioAtual.uid());
        saida.setRecorrenteId(null); // só o sistema define
        saida.setMetaId(null);       // só o sistema define (aportes)
        if (saida.getTipo() == null) saida.setTipo("variável");
        if (saida.getCategoria() == null) saida.setCategoria("outros");
        return saidaRepository.save(saida);
    }

    @PutMapping("/{id}")
    public Saida updateSaida(@PathVariable Long id, @Valid @RequestBody Saida detalhes) {
        Saida saida = buscar(id);
        if (saida.getMetaId() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Aportes de metas só podem ser alterados pela tela de Metas.");
        }
        rejeitarValoresReservados(detalhes);

        saida.setDescricao(detalhes.getDescricao());
        saida.setValor(detalhes.getValor());
        saida.setData(detalhes.getData());
        saida.setTipo(detalhes.getTipo() != null ? detalhes.getTipo() : "variável");
        saida.setCategoria(detalhes.getCategoria() != null ? detalhes.getCategoria() : "outros");
        return saidaRepository.save(saida);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSaida(@PathVariable Long id) {
        Saida saida = buscar(id);
        if (saida.getMetaId() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Aportes de metas só podem ser removidos pela tela de Metas.");
        }
        saidaRepository.delete(saida);
        return ResponseEntity.noContent().build();
    }

    private Saida buscar(Long id) {
        return saidaRepository.findByIdAndUserId(id, UsuarioAtual.uid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saída não encontrada."));
    }
    
    private void rejeitarValoresReservados(Saida s) {
        if ("aporte".equals(s.getTipo()) || "metas".equals(s.getCategoria())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor reservado para o sistema.");
        }
    }
}