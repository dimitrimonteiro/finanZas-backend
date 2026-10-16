package com.finanzas.finanzas.controller;

import com.finanzas.finanzas.model.VinculoWhatsapp;
import com.finanzas.finanzas.repository.VinculoWhatsappRepository;
import com.finanzas.finanzas.security.UsuarioAtual;
import com.finanzas.finanzas.service.CodigoVinculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsappController {

    @Autowired
    private VinculoWhatsappRepository vinculoRepository;

    @Autowired
    private CodigoVinculoService codigoService;

    @PostMapping("/codigo")
    public Map<String, Object> gerarCodigo() {
        exigirUsuarioWeb();
        String codigo = codigoService.gerar(UsuarioAtual.uid());
        return Map.of("codigo", codigo, "expiraEmSegundos", 600);
    }

    @GetMapping("/vinculos")
    public Map<String, Object> listar() {
        exigirUsuarioWeb();
        List<String> numeros = vinculoRepository.findByUserId(UsuarioAtual.uid()).stream()
                .map(v -> mascarar(v.getWhatsappId()))
                .toList();
        return Map.of("vinculado", !numeros.isEmpty(), "numeros", numeros);
    }

    @Transactional
    @DeleteMapping("/vinculos")
    public ResponseEntity<Void> desvincularTodos() {
        exigirUsuarioWeb();
        vinculoRepository.deleteByUserId(UsuarioAtual.uid());
        return ResponseEntity.noContent().build();
    }

    @Transactional
    @PostMapping("/vincular")
    public ResponseEntity<?> vincular(
            @RequestHeader("X-Whatsapp-Id") String whatsappId,
            @RequestBody Map<String, String> body) {

        if (!UsuarioAtual.ehBotSemUsuario()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado.");
        }
        if (codigoService.bloqueado(whatsappId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("erro", "muitas_tentativas"));
        }

        Optional<String> userId = codigoService.consumir(body.get("codigo"));
        if (userId.isEmpty()) {
            codigoService.registrarFalha(whatsappId);
            return ResponseEntity.badRequest().body(Map.of("erro", "codigo_invalido"));
        }

        VinculoWhatsapp vinculo = vinculoRepository.findByWhatsappId(whatsappId).orElseGet(VinculoWhatsapp::new);
        vinculo.setWhatsappId(whatsappId);
        vinculo.setUserId(userId.get());
        vinculoRepository.save(vinculo);

        return ResponseEntity.ok(Map.of("vinculado", true));
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        exigirBotComVinculo();
        return Map.of("vinculado", true);
    }

    @Transactional
    @DeleteMapping("/vinculo")
    public ResponseEntity<Void> desvincular(@RequestHeader("X-Whatsapp-Id") String whatsappId) {
        exigirBotComVinculo();
        vinculoRepository.deleteByWhatsappId(whatsappId);
        return ResponseEntity.noContent().build();
    }

    private void exigirUsuarioWeb() {
        if (UsuarioAtual.ehBot()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado.");
        }
    }

    private void exigirBotComVinculo() {
        if (!UsuarioAtual.ehBot() || UsuarioAtual.ehBotSemUsuario()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado.");
        }
    }

    private String mascarar(String whatsappId) {
        String digitos = whatsappId.replaceAll("\\D", "");
        return digitos.length() > 4 ? "•••• " + digitos.substring(digitos.length() - 4) : "••••";
    }
}