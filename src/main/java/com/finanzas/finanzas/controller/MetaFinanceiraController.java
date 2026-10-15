package com.finanzas.finanzas.controller;

import com.finanzas.finanzas.model.MetaFinanceira;
import com.finanzas.finanzas.model.Saida;
import com.finanzas.finanzas.repository.MetaFinanceiraRepository;
import com.finanzas.finanzas.repository.SaidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/metas")
@CrossOrigin(origins = "http://localhost:3000")
public class MetaFinanceiraController {

    @Autowired
    private MetaFinanceiraRepository metaFinanceiraRepository;

    @Autowired
    private SaidaRepository saidaRepository;

    public record AporteRequest(BigDecimal valor, LocalDate data, String descricao) {}

    @GetMapping
    public List<MetaFinanceira> getAllMetas(@RequestParam String userId) {
        return metaFinanceiraRepository.findByUserId(userId);
    }

    @PostMapping
    public MetaFinanceira createMeta(@RequestBody MetaFinanceira meta, @RequestParam String userId) {
        meta.setUserId(userId);
        meta.setValorAtual(meta.getValorAtual() != null ? meta.getValorAtual() : new java.math.BigDecimal("0.00"));
        return metaFinanceiraRepository.save(meta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MetaFinanceira> updateMeta(
            @PathVariable Long id,
            @RequestBody MetaFinanceira metaDetails,
            @RequestParam String userId) {

        return metaFinanceiraRepository.findById(id)
                .map(meta -> {
                    if (!meta.getUserId().equals(userId)) {
                        return ResponseEntity.status(403).<MetaFinanceira>build();
                    }

                    meta.setNomeMeta(metaDetails.getNomeMeta());
                    meta.setValorObjetivo(metaDetails.getValorObjetivo());
                    meta.setValorAtual(metaDetails.getValorAtual());
                    meta.setDataLimite(metaDetails.getDataLimite());
                    meta.setCategoria(metaDetails.getCategoria());

                    MetaFinanceira updatedMeta = metaFinanceiraRepository.save(meta);
                    return ResponseEntity.ok(updatedMeta);
                })
                .orElseGet(() -> ResponseEntity.notFound().<MetaFinanceira>build());
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMeta(@PathVariable Long id, @RequestParam String userId) {
        return metaFinanceiraRepository.findById(id).map(meta -> {
            if (!meta.getUserId().equals(userId)) {
                return ResponseEntity.status(403).build(); // Proibido
            }

            // Os aportes já feitos viram saídas comuns (continuam no histórico)
            List<Saida> aportes = saidaRepository.findByUserIdAndMetaIdOrderByDataDesc(userId, id);
            aportes.forEach(s -> {
                s.setMetaId(null);
                s.setTipo("variável");
                s.setCategoria("outros");
            });
            saidaRepository.saveAll(aportes);

            metaFinanceiraRepository.delete(meta);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/aportes")
    public ResponseEntity<?> listarAportes(@PathVariable Long id, @RequestParam String userId) {
        MetaFinanceira meta = metaFinanceiraRepository.findById(id).orElse(null);
        if (meta == null) return ResponseEntity.notFound().build();
        if (!meta.getUserId().equals(userId)) return ResponseEntity.status(403).build();

        return ResponseEntity.ok(saidaRepository.findByUserIdAndMetaIdOrderByDataDesc(userId, id));
    }

    @Transactional
    @PostMapping("/{id}/aportes")
    public ResponseEntity<?> criarAporte(
            @PathVariable Long id,
            @RequestBody AporteRequest req,
            @RequestParam String userId) {

        MetaFinanceira meta = metaFinanceiraRepository.findById(id).orElse(null);
        if (meta == null) return ResponseEntity.notFound().build();
        if (!meta.getUserId().equals(userId)) return ResponseEntity.status(403).build();

        if (req.valor() == null || req.valor().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("O valor do aporte deve ser maior que zero.");
        }

        BigDecimal atual = meta.getValorAtual() != null ? meta.getValorAtual() : BigDecimal.ZERO;
        BigDecimal objetivo = meta.getValorObjetivo() != null ? meta.getValorObjetivo() : BigDecimal.ZERO;
        BigDecimal restante = objetivo.subtract(atual);

        if (restante.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Esta meta já foi alcançada.");
        }
        if (req.valor().compareTo(restante) > 0) {
            return ResponseEntity.badRequest()
                    .body("O aporte não pode ser maior que o valor que falta para a meta (R$ " + restante.toPlainString() + ").");
        }

        String obs = req.descricao() != null ? req.descricao().trim() : "";

        Saida saida = new Saida();
        saida.setDescricao("Aporte: " + meta.getNomeMeta() + (obs.isEmpty() ? "" : " - " + obs));
        saida.setValor(req.valor());
        saida.setData(req.data() != null ? req.data() : LocalDate.now());
        saida.setTipo("aporte");
        saida.setCategoria("metas");
        saida.setUserId(userId);
        saida.setMetaId(id);
        saidaRepository.save(saida);

        meta.setValorAtual(atual.add(req.valor()));
        return ResponseEntity.ok(metaFinanceiraRepository.save(meta));
    }

    @Transactional
    @DeleteMapping("/{id}/aportes/{saidaId}")
    public ResponseEntity<?> removerAporte(
            @PathVariable Long id,
            @PathVariable Long saidaId,
            @RequestParam String userId) {

        MetaFinanceira meta = metaFinanceiraRepository.findById(id).orElse(null);
        if (meta == null) return ResponseEntity.notFound().build();
        if (!meta.getUserId().equals(userId)) return ResponseEntity.status(403).build();

        Saida saida = saidaRepository.findById(saidaId).orElse(null);
        if (saida == null || !userId.equals(saida.getUserId()) || !id.equals(saida.getMetaId())) {
            return ResponseEntity.notFound().build();
        }

        BigDecimal atual = meta.getValorAtual() != null ? meta.getValorAtual() : BigDecimal.ZERO;
        BigDecimal novo = atual.subtract(saida.getValor());
        meta.setValorAtual(novo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : novo);

        saidaRepository.delete(saida);
        return ResponseEntity.ok(metaFinanceiraRepository.save(meta));
    }
}