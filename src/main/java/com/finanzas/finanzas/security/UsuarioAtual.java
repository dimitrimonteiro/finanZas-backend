package com.finanzas.finanzas.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

public final class UsuarioAtual {

    private UsuarioAtual() {}

    public static String uid() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não autenticado.");
        }
        if (tem(auth, "ROLE_BOT")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado.");
        }
        return auth.getName();
    }

    public static boolean ehBot() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && (tem(auth, "ROLE_BOT") || tem(auth, "ROLE_BOT_USER"));
    }

    public static boolean ehBotSemUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && tem(auth, "ROLE_BOT");
    }

    private static boolean tem(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority()));
    }
}