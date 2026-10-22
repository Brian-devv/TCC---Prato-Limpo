package com.restaurante.sistema.controller;

import com.restaurante.sistema.dto.CadastroUsuarioDTO;
import com.restaurante.sistema.dto.LoginDTO;
import com.restaurante.sistema.dto.RespostaApiDTO;
import com.restaurante.sistema.model.Usuario;
import com.restaurante.sistema.service.AutenticacaoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoints chamados pelo cadastro.js, entrar.js, administrador.js e painel.js via fetch().
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    @PostMapping("/cadastro")
    public ResponseEntity<RespostaApiDTO> cadastrar(@Valid @RequestBody CadastroUsuarioDTO dto) {
        Usuario usuario = autenticacaoService.cadastrarUsuario(dto);
        return ResponseEntity.ok(new RespostaApiDTO(
                true,
                "Conta criada com sucesso! Você já pode entrar.",
                Map.of("id", usuario.getIdUsuario(), "nome", usuario.getNome())
        ));
    }

    @PostMapping("/entrar")
    public ResponseEntity<RespostaApiDTO> entrar(@Valid @RequestBody LoginDTO dto, HttpSession sessao) {
        Usuario usuario = autenticacaoService.autenticar(dto);

        sessao.setAttribute("usuarioId", usuario.getIdUsuario());
        sessao.setAttribute("tipoPerfil", usuario.getTipoPerfil());

        return ResponseEntity.ok(new RespostaApiDTO(
                true,
                "Login realizado com sucesso!",
                Map.of("id", usuario.getIdUsuario(), "nome", usuario.getNome(), "tipoPerfil", usuario.getTipoPerfil())
        ));
    }

    @PostMapping("/administrador/entrar")
    public ResponseEntity<RespostaApiDTO> entrarComoAdministrador(@Valid @RequestBody LoginDTO dto, HttpSession sessao) {
        Usuario usuario = autenticacaoService.autenticarAdministrador(dto);

        sessao.setAttribute("usuarioId", usuario.getIdUsuario());
        sessao.setAttribute("tipoPerfil", usuario.getTipoPerfil());

        return ResponseEntity.ok(new RespostaApiDTO(
                true,
                "Login de administrador realizado com sucesso!",
                Map.of("id", usuario.getIdUsuario(), "nome", usuario.getNome(), "tipoPerfil", usuario.getTipoPerfil())
        ));
    }

    @PostMapping("/sair")
    public ResponseEntity<RespostaApiDTO> sair(HttpSession sessao) {
        sessao.invalidate();
        return ResponseEntity.ok(new RespostaApiDTO(true, "Sessão encerrada com sucesso."));
    }
}