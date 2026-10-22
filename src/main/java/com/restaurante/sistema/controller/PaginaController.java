package com.restaurante.sistema.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller "de páginas": só devolve os templates Thymeleaf.
 * Toda a lógica de dados fica em endpoints REST separados (ex: /api/auth/**,
 * /api/usuarios/**, /api/admin/**), chamados via fetch() pelos arquivos js/*.js.
 */
@Controller
public class PaginaController {

    /**
     * Rota raiz: para onde o clique na marca (logo) leva.
     *
     * Não é uma página em si — é um "despachante" que decide o
     * destino certo com base em quem está acessando:
     *   - Sem sessão válida  -> tela de login (/entrar)
     *   - Administrador logado -> painel administrativo
     *   - Qualquer outro usuário logado -> painel comum
     *
     * Assim, clicar na marca nunca "desloga" ninguém nem pede login
     * de novo para quem já está autenticado — só leva para a home
     * de cada tipo de usuário.
     */
    @GetMapping("/")
    public String raiz(HttpSession sessao) {
        Object tipoPerfil = sessao.getAttribute("tipoPerfil");

        if (tipoPerfil == null) {
            return "redirect:/entrar";
        }
        if ("ADMINISTRADOR".equals(tipoPerfil)) {
            return "redirect:/administrador/painel";
        }
        return "redirect:/painel";
    }

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