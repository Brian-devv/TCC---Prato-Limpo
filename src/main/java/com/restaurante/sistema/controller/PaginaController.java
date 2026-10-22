package com.restaurante.sistema.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller "de páginas": só devolve os templates Thymeleaf.
 * Toda a lógica de dados fica em endpoints REST separados (ex: /api/auth/**,
 * /api/usuarios/**, /api/admin/**), chamados via fetch() pelos arquivos js/*.js.
 */
@Controller
public class PaginaController {

    @GetMapping("/cadastro")
    public String paginaCadastro() {
        return "cadastro";
    }

    @GetMapping("/entrar")
    public String paginaEntrar() {
        return "entrar";
    }

    @GetMapping("/painel")
    public String paginaPainel() {
        return "painel";
    }

    @GetMapping("/perfil")
    public String paginaPerfil() {
        return "perfil";
    }

    @GetMapping("/redefinir-senha")
    public String paginaRedefinirSenha() {
        return "redefinir-senha";
    }

    @GetMapping("/redefinir-senha/confirmar")
    public String paginaRedefinirSenhaConfirmar() {
        return "redefinir-senha-confirmar";
    }

    /** Login exclusivo de administrador (não é cadastro público). */
    @GetMapping("/administrador")
    public String paginaAdministrador() {
        return "administrador";
    }

    /** Painel do administrador, acessível após o login em /administrador. */
    @GetMapping("/administrador/painel")
    public String paginaAdministradorPainel() {
        return "administrador-painel";
    }
}