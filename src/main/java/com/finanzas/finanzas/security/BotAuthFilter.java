package com.finanzas.finanzas.security;

import com.finanzas.finanzas.model.VinculoWhatsapp;
import com.finanzas.finanzas.repository.VinculoWhatsappRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;

public class BotAuthFilter extends OncePerRequestFilter {

    private final String botApiKey;
    private final VinculoWhatsappRepository vinculoRepository;

    public BotAuthFilter(String botApiKey, VinculoWhatsappRepository vinculoRepository) {
        this.botApiKey = botApiKey;
        this.vinculoRepository = vinculoRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String chave = request.getHeader("X-Bot-Key");
        if (chave == null) {
            chain.doFilter(request, response);
            return;
        }

        boolean chaveValida = !botApiKey.isBlank() && MessageDigest.isEqual(
                chave.getBytes(StandardCharsets.UTF_8), botApiKey.getBytes(StandardCharsets.UTF_8));
        if (!chaveValida) {
            recusar(response, "bot_key_invalida");
            return;
        }

        String whatsappId = request.getHeader("X-Whatsapp-Id");
        if (whatsappId == null || whatsappId.isBlank()) {
            recusar(response, "whatsapp_id_ausente");
            return;
        }

        if ("/api/whatsapp/vincular".equals(request.getRequestURI())) {
            autenticar("bot", "ROLE_BOT");
        } else {
            Optional<VinculoWhatsapp> vinculo = vinculoRepository.findByWhatsappId(whatsappId);
            if (vinculo.isEmpty()) {
                recusar(response, "nao_vinculado");
                return;
            }
            autenticar(vinculo.get().getUserId(), "ROLE_BOT_USER");
        }

        chain.doFilter(request, response);
    }

    private void autenticar(String nome, String role) {
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(new UsernamePasswordAuthenticationToken(
                nome, null, List.of(new SimpleGrantedAuthority(role))));
        SecurityContextHolder.setContext(contexto);
    }

    private void recusar(HttpServletResponse response, String codigo) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"erro\":\"" + codigo + "\"}");
    }
}