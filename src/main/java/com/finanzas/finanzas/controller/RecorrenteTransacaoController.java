package com.finanzas.finanzas.controller;

import com.finanzas.finanzas.model.Entrada;
import com.finanzas.finanzas.model.RecorrenteTransacao;
import com.finanzas.finanzas.model.Saida;
import com.finanzas.finanzas.repository.EntradaRepository;
import com.finanzas.finanzas.repository.RecorrenteTransacaoRepository;
import com.finanzas.finanzas.repository.SaidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public List<RecorrenteTransacao> getAll(@RequestParam String userId) {
        return recorrenteRepository.findByUserId(userId);
    }

    @PostMapping
    public RecorrenteTransacao create(@RequestBody RecorrenteTransacao r, @RequestParam String userId) {
        r.setUserId(userId);
        if (r.getAtiva() == null) r.setAtiva(true);
        return recorrenteRepository.save(r);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecorrenteTransacao> update(
            @PathVariable Long id,
            @RequestBody RecorrenteTransacao details,
            @RequestParam String userId) {

        return recorrenteRepository.findById(id)
                .map(r -> {
                    if (!r.getUserId().equals(userId)) {
                        return ResponseEntity.status(403).<RecorrenteTransacao>build();
                    }
                    r.setDescricao(details.getDescricao());
                    r.setValor(details.getValor());
                    r.setTipoTransacao(details.getTipoTransacao());
                    r.setTipoSaida(details.getTipoSaida());
                    r.setSalario(details.getSalario());
                    r.setDiaVencimento(details.getDiaVencimento());
                    r.setAtiva(details.getAtiva());
                    return ResponseEntity.ok(recorrenteRepository.save(r));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestParam String userId) {
        return recorrenteRepository.findById(id).map(r -> {
            if (!r.getUserId().equals(userId)) {
                return ResponseEntity.status(403).build();
            }
            recorrenteRepository.delete(r);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/gerar")
    public ResponseEntity<Map<String, Object>> gerar(@RequestParam String userId) {
        List<RecorrenteTransacao> ativas = recorrenteRepository.findByUserIdAndAtivaTrue(userId);
        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        LocalDate fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth());

        int criadas = 0;

        for (RecorrenteTransacao r : ativas) {
            int dia = Math.min(r.getDiaVencimento() != null ? r.getDiaVencimento() : 1, hoje.lengthOfMonth());
            LocalDate dataTransacao = LocalDate.of(hoje.getYear(), hoje.getMonth(), dia);

            if ("entrada".equals(r.getTipoTransacao())) {
                boolean existe = entradaRepository.existsByRecorrenteIdAndDataBetween(r.getId(), inicioMes, fimMes);
                if (!existe) {
                    Entrada e = new Entrada();
                    e.setDescricao(r.getDescricao());
                    e.setValor(r.getValor());
                    e.setData(dataTransacao);
                    e.setSalario(Boolean.TRUE.equals(r.getSalario()));
                    e.setUserId(userId);
                    e.setRecorrenteId(r.getId());
                    entradaRepository.save(e);
                    criadas++;
                }
            } else if ("saida".equals(r.getTipoTransacao())) {
                boolean existe = saidaRepository.existsByRecorrenteIdAndDataBetween(r.getId(), inicioMes, fimMes);
                if (!existe) {
                    Saida s = new Saida();
                    s.setDescricao(r.getDescricao());
                    s.setValor(r.getValor());
                    s.setData(dataTransacao);
                    s.setTipo(r.getTipoSaida() != null ? r.getTipoSaida() : "fixa");
                    s.setUserId(userId);
                    s.setRecorrenteId(r.getId());
                    saidaRepository.save(s);
                    criadas++;
                }
            }
        }

        return ResponseEntity.ok(Map.of("criadas", criadas));
    }
}