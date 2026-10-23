package com.finanzas.finanzas.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CodigoVinculoService {

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TAMANHO = 6;
    private static final Duration VALIDADE = Duration.ofMinutes(10);

    private static final Duration JANELA = Duration.ofMinutes(10);
    private static final int MAX_FALHAS_POR_NUMERO = 5;
    private static final int MAX_FALHAS_GLOBAIS = 50;

    private record Registro(String userId, Instant expiraEm) {}

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Registro> codigos = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> falhas = new HashMap<>();

    public String gerar(String userId) {
        limpar();
        codigos.values().removeIf(r -> r.userId().equals(userId));

        String codigo;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < TAMANHO; i++) {
                sb.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
            }
            codigo = sb.toString();
        } while (codigos.containsKey(codigo));

        codigos.put(codigo, new Registro(userId, Instant.now().plus(VALIDADE)));
        return codigo;
    }

    public Optional<String> consumir(String codigo) {
        if (codigo == null) return Optional.empty();
        Registro registro = codigos.remove(codigo.trim().toUpperCase());
        if (registro == null || registro.expiraEm().isBefore(Instant.now())) return Optional.empty();
        return Optional.of(registro.userId());
    }

    public synchronized boolean bloqueado(String whatsappId) {
        return contar(whatsappId) >= MAX_FALHAS_POR_NUMERO || contar("*") >= MAX_FALHAS_GLOBAIS;
    }

    public synchronized void registrarFalha(String whatsappId) {
        Instant agora = Instant.now();
        falhas.computeIfAbsent(whatsappId, k -> new ArrayDeque<>()).add(agora);
        falhas.computeIfAbsent("*", k -> new ArrayDeque<>()).add(agora);
    }

    private int contar(String chave) {
        Deque<Instant> fila = falhas.get(chave);
        if (fila == null) return 0;
        Instant limite = Instant.now().minus(JANELA);
        while (!fila.isEmpty() && fila.peekFirst().isBefore(limite)) fila.pollFirst();
        return fila.size();
    }

    private synchronized void limpar() {
        Instant agora = Instant.now();
        codigos.values().removeIf(r -> r.expiraEm().isBefore(agora));
        for (String chave : falhas.keySet().toArray(new String[0])) contar(chave);
        falhas.values().removeIf(Deque::isEmpty);
    }
}