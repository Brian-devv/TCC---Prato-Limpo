package com.restaurante.sistema.controller;

import com.restaurante.sistema.dto.AlterarSenhaDTO;
import com.restaurante.sistema.dto.AtualizarPerfilDTO;
import com.restaurante.sistema.dto.RespostaApiDTO;
import com.restaurante.sistema.dto.UsuarioRespostaDTO;
import com.restaurante.sistema.exception.AutenticacaoNecessariaException;
import com.restaurante.sistema.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de CRUD do perfil do usuário logado.
 *
 * Como ainda não usamos o Spring Security "completo" para
 * autenticação (só a sessão simples criada no login), cada endpoint
 * aqui confere manualmente se existe um usuário logado na sessão,
 * através de obterIdUsuarioLogado(). Se não houver, devolvemos 401.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public ResponseEntity<RespostaApiDTO> buscarPerfil(HttpSession sessao) {
        Long idUsuario = obterIdUsuarioLogado(sessao);
        UsuarioRespostaDTO perfil = usuarioService.buscarPerfil(idUsuario);
        return ResponseEntity.ok(new RespostaApiDTO(true, "Perfil carregado com sucesso.", perfil));
    }

    @PutMapping("/me")
    public ResponseEntity<RespostaApiDTO> atualizarPerfil(@Valid @RequestBody AtualizarPerfilDTO dto, HttpSession sessao) {
        Long idUsuario = obterIdUsuarioLogado(sessao);
        UsuarioRespostaDTO perfilAtualizado = usuarioService.atualizarPerfil(idUsuario, dto);
        return ResponseEntity.ok(new RespostaApiDTO(true, "Perfil atualizado com sucesso!", perfilAtualizado));
    }

    @PutMapping("/me/senha")
    public ResponseEntity<RespostaApiDTO> alterarSenha(@Valid @RequestBody AlterarSenhaDTO dto, HttpSession sessao) {
        Long idUsuario = obterIdUsuarioLogado(sessao);
        usuarioService.alterarSenha(idUsuario, dto);
        return ResponseEntity.ok(new RespostaApiDTO(true, "Senha alterada com sucesso!"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<RespostaApiDTO> excluirConta(HttpSession sessao) {
        Long idUsuario = obterIdUsuarioLogado(sessao);
        usuarioService.excluirConta(idUsuario);
        sessao.invalidate(); // encerra a sessão junto, já que a conta não existe mais
        return ResponseEntity.ok(new RespostaApiDTO(true, "Conta excluída com sucesso."));
    }

    /** Lê o id do usuário logado a partir da sessão criada no login (ver AutenticacaoController). */
    private Long obterIdUsuarioLogado(HttpSession sessao) {
        Object idUsuario = sessao.getAttribute("usuarioId");
        if (idUsuario == null) {
            throw new AutenticacaoNecessariaException("Você precisa estar logado para acessar este recurso.");
        }
        return (Long) idUsuario;
    }
}