package com.finanzas.finanzas.controller;

import com.finanzas.finanzas.model.Entrada;
import com.finanzas.finanzas.model.RecorrenteTransacao;
import com.finanzas.finanzas.model.Saida;
import com.finanzas.finanzas.repository.EntradaRepository;
import com.finanzas.finanzas.repository.RecorrenteTransacaoRepository;
import com.finanzas.finanzas.repository.SaidaRepository;
import com.finanzas.finanzas.security.UsuarioAtual;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recorrentes")
public class RecorrenteTransacaoController {

    @Autowired
    private RecorrenteTransacaoRepository recorrenteRepository;

    @Autowired
    private EntradaRepository entradaRepository;

    @Autowired
    private SaidaRepository saidaRepository;

    @GetMapping
    public List<RecorrenteTransacao> getAll() {
        return recorrenteRepository.findByUserId(UsuarioAtual.uid());
    }

    @PostMapping
    public RecorrenteTransacao create(@Valid @RequestBody RecorrenteTransacao r) {
        r.setId(null);
        r.setUserId(UsuarioAtual.uid());
        if (r.getAtiva() == null) r.setAtiva(true);
        return recorrenteRepository.save(r);
    }

    @PutMapping("/{id}")
    public RecorrenteTransacao update(@PathVariable Long id, @Valid @RequestBody RecorrenteTransacao detalhes) {
        RecorrenteTransacao r = buscar(id);
        r.setDescricao(detalhes.getDescricao());
        r.setValor(detalhes.getValor());
        r.setTipoTransacao(detalhes.getTipoTransacao());
        r.setTipoSaida(detalhes.getTipoSaida());
        r.setCategoriaSaida(detalhes.getCategoriaSaida());
        r.setSalario(detalhes.getSalario());
        r.setDiaVencimento(detalhes.getDiaVencimento());
        r.setAtiva(detalhes.getAtiva());
        return recorrenteRepository.save(r);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recorrenteRepository.delete(buscar(id));
        return ResponseEntity.noContent().build();
    }

    @Transactional
    @PostMapping("/gerar")
    public Map<String, Object> gerar() {
        String userId = UsuarioAtual.uid();
        List<RecorrenteTransacao> ativas = recorrenteRepository.findByUserIdAndAtivaTrue(userId);
        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        LocalDate fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth());

        int criadas = 0;

        for (RecorrenteTransacao r : ativas) {
            int dia = Math.min(r.getDiaVencimento() != null ? r.getDiaVencimento() : 1, hoje.lengthOfMonth());
            LocalDate dataTransacao = LocalDate.of(hoje.getYear(), hoje.getMonth(), dia);

            if ("entrada".equals(r.getTipoTransacao())) {
                if (!entradaRepository.existsByRecorrenteIdAndDataBetween(r.getId(), inicioMes, fimMes)) {
                    boolean salario = Boolean.TRUE.equals(r.getSalario());
                    Entrada e = new Entrada();
                    e.setDescricao(r.getDescricao());
                    e.setValor(r.getValor());
                    e.setData(dataTransacao);
                    e.setCategoria(salario ? "salario" : "outros");
                    e.setSalario(salario);
                    e.setUserId(userId);
                    e.setRecorrenteId(r.getId());
                    entradaRepository.save(e);
                    criadas++;
                }
            } else if ("saida".equals(r.getTipoTransacao())) {
                if (!saidaRepository.existsByRecorrenteIdAndDataBetween(r.getId(), inicioMes, fimMes)) {
                    Saida s = new Saida();
                    s.setDescricao(r.getDescricao());
                    s.setValor(r.getValor());
                    s.setData(dataTransacao);
                    s.setTipo(r.getTipoSaida() != null ? r.getTipoSaida() : "fixa");
                    s.setCategoria(r.getCategoriaSaida() != null ? r.getCategoriaSaida() : "outros");
                    s.setUserId(userId);
                    s.setRecorrenteId(r.getId());
                    saidaRepository.save(s);
                    criadas++;
                }
            }
        }

        return Map.of("criadas", criadas);
    }

    private RecorrenteTransacao buscar(Long id) {
        return recorrenteRepository.findByIdAndUserId(id, UsuarioAtual.uid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recorrência não encontrada."));
    }
}