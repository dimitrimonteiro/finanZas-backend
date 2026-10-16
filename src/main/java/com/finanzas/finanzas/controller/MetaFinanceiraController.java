package com.finanzas.finanzas.controller;

import com.finanzas.finanzas.model.MetaFinanceira;
import com.finanzas.finanzas.model.Saida;
import com.finanzas.finanzas.repository.MetaFinanceiraRepository;
import com.finanzas.finanzas.repository.SaidaRepository;
import com.finanzas.finanzas.security.UsuarioAtual;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/metas")
public class MetaFinanceiraController {

    @Autowired
    private MetaFinanceiraRepository metaFinanceiraRepository;

    @Autowired
    private SaidaRepository saidaRepository;

    public record AporteRequest(
            @NotNull @DecimalMin("0.01") BigDecimal valor,
            LocalDate data,
            String descricao) {}

    @GetMapping
    public List<MetaFinanceira> getAllMetas() {
        return metaFinanceiraRepository.findByUserId(UsuarioAtual.uid());
    }

    @PostMapping
    public MetaFinanceira createMeta(@Valid @RequestBody MetaFinanceira meta) {
        meta.setId(null);
        meta.setUserId(UsuarioAtual.uid());
        if (meta.getValorAtual() == null) meta.setValorAtual(BigDecimal.ZERO);
        validarProgresso(meta.getValorAtual(), meta.getValorObjetivo());
        return metaFinanceiraRepository.save(meta);
    }

    @PutMapping("/{id}")
    public MetaFinanceira updateMeta(@PathVariable Long id, @Valid @RequestBody MetaFinanceira detalhes) {
        MetaFinanceira meta = buscar(id);
        BigDecimal atual = detalhes.getValorAtual() != null ? detalhes.getValorAtual() : BigDecimal.ZERO;
        validarProgresso(atual, detalhes.getValorObjetivo());

        meta.setNomeMeta(detalhes.getNomeMeta());
        meta.setValorObjetivo(detalhes.getValorObjetivo());
        meta.setValorAtual(atual);
        meta.setDataLimite(detalhes.getDataLimite());
        meta.setCategoria(detalhes.getCategoria());
        return metaFinanceiraRepository.save(meta);
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeta(@PathVariable Long id) {
        String userId = UsuarioAtual.uid();
        MetaFinanceira meta = buscar(id);

        List<Saida> aportes = saidaRepository.findByUserIdAndMetaIdOrderByDataDesc(userId, id);
        aportes.forEach(s -> {
            s.setMetaId(null);
            s.setTipo("variável");
            s.setCategoria("outros");
        });
        saidaRepository.saveAll(aportes);

        metaFinanceiraRepository.delete(meta);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}/aportes")
    public List<Saida> listarAportes(@PathVariable Long id) {
        buscar(id);
        return saidaRepository.findByUserIdAndMetaIdOrderByDataDesc(UsuarioAtual.uid(), id);
    }

    @Transactional
    @PostMapping("/{id}/aportes")
    public ResponseEntity<?> criarAporte(@PathVariable Long id, @Valid @RequestBody AporteRequest req) {
        String userId = UsuarioAtual.uid();
        MetaFinanceira meta = buscar(id);

        BigDecimal atual = meta.getValorAtual() != null ? meta.getValorAtual() : BigDecimal.ZERO;
        BigDecimal restante = meta.getValorObjetivo().subtract(atual);

        if (restante.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Esta meta já foi alcançada.");
        }
        if (req.valor().compareTo(restante) > 0) {
            return ResponseEntity.badRequest()
                    .body("O aporte não pode ser maior que o valor que falta para a meta (R$ " + restante.toPlainString() + ").");
        }

        String obs = req.descricao() != null ? req.descricao().trim() : "";
        if (obs.length() > 80) obs = obs.substring(0, 80);

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
    public MetaFinanceira removerAporte(@PathVariable Long id, @PathVariable Long saidaId) {
        String userId = UsuarioAtual.uid();
        MetaFinanceira meta = buscar(id);

        Saida saida = saidaRepository.findByIdAndUserId(saidaId, userId)
                .filter(s -> id.equals(s.getMetaId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aporte não encontrado."));

        BigDecimal atual = meta.getValorAtual() != null ? meta.getValorAtual() : BigDecimal.ZERO;
        BigDecimal novo = atual.subtract(saida.getValor());
        meta.setValorAtual(novo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : novo);

        saidaRepository.delete(saida);
        return metaFinanceiraRepository.save(meta);
    }

    private MetaFinanceira buscar(Long id) {
        return metaFinanceiraRepository.findByIdAndUserId(id, UsuarioAtual.uid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meta não encontrada."));
    }

    private void validarProgresso(BigDecimal atual, BigDecimal objetivo) {
        if (atual.compareTo(objetivo) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O valor economizado não pode ser maior que o objetivo.");
        }
    }
}