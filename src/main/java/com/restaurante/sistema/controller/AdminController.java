package com.restaurante.sistema.controller;

import com.restaurante.sistema.dto.*;
import com.restaurante.sistema.exception.AcessoNegadoException;
import com.restaurante.sistema.exception.AutenticacaoNecessariaException;
import com.restaurante.sistema.model.Usuario;
import com.restaurante.sistema.service.AdminService;
import com.restaurante.sistema.service.AutenticacaoService;
import com.restaurante.sistema.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints exclusivos do painel administrativo: listar usuários,
 * cadastrar funcionário/gestor, editar dados de um usuário e
 * alternar o status dele (ativo/bloqueado).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AutenticacaoService autenticacaoService;
    private final UsuarioService usuarioService;

    /** Lista nome, e-mail, tipo e status de todos os usuários (sem dados sensíveis). */
    @GetMapping("/usuarios")
    public ResponseEntity<RespostaApiDTO> listarUsuarios(HttpSession sessao) {
        exigirAdministradorLogado(sessao);
        List<UsuarioResumoDTO> usuarios = adminService.listarUsuarios();
        return ResponseEntity.ok(new RespostaApiDTO(true, "Usuários carregados com sucesso.", usuarios));
    }

    /** Edita nome e e-mail de um usuário qualquer (botão "Editar" da tabela). */
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<RespostaApiDTO> editarUsuario(
            @PathVariable Long id, @Valid @RequestBody AtualizarPerfilDTO dto, HttpSession sessao) {
        exigirAdministradorLogado(sessao);
        UsuarioRespostaDTO usuarioAtualizado = usuarioService.atualizarPerfil(id, dto);
        return ResponseEntity.ok(new RespostaApiDTO(true, "Usuário atualizado com sucesso!", usuarioAtualizado));
    }

    /** Alterna o status do usuário entre ATIVO e BLOQUEADO (botão switch da tabela). */
    @PatchMapping("/usuarios/{id}/status")
    public ResponseEntity<RespostaApiDTO> alternarStatusUsuario(@PathVariable Long id, HttpSession sessao) {
        exigirAdministradorLogado(sessao);
        Long idAdministradorLogado = (Long) sessao.getAttribute("usuarioId");
        UsuarioResumoDTO usuarioAtualizado = adminService.alternarStatusUsuario(id, idAdministradorLogado);
        return ResponseEntity.ok(new RespostaApiDTO(true, "Status atualizado com sucesso!", usuarioAtualizado));
    }

    /**
     * Cadastra um Funcionário OU um Gestor, a partir da caixa
     * "Cadastrar funcionário ou gestor" do painel. Qual dos dois é
     * decidido pelo campo "tipoConta" do DTO — ver
     * AutenticacaoService.cadastrarColaborador().
     */
    @PostMapping("/colaboradores")
    public ResponseEntity<RespostaApiDTO> cadastrarColaborador(@Valid @RequestBody CadastroFuncionarioDTO dto, HttpSession sessao) {
        exigirAdministradorLogado(sessao);
        Usuario colaboradorCriado = autenticacaoService.cadastrarColaborador(dto);
        return ResponseEntity.ok(new RespostaApiDTO(
                true,
                "Cadastrado com sucesso!",
                new UsuarioResumoDTO(colaboradorCriado)
        ));
    }

    /** Exclui um usuário pelo id (mantido no backend mesmo sem botão correspondente hoje). */
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<RespostaApiDTO> excluirUsuario(@PathVariable Long id, HttpSession sessao) {
        exigirAdministradorLogado(sessao);
        Long idAdministradorLogado = (Long) sessao.getAttribute("usuarioId");
        adminService.excluirUsuario(id, idAdministradorLogado);
        return ResponseEntity.ok(new RespostaApiDTO(true, "Usuário excluído com sucesso."));
    }

    /** Confere sessão válida + papel ADMINISTRADOR; lança exceção apropriada se não. */
    private void exigirAdministradorLogado(HttpSession sessao) {
        Object idUsuario = sessao.getAttribute("usuarioId");
        if (idUsuario == null) {
            throw new AutenticacaoNecessariaException("Você precisa estar logado para acessar este recurso.");
        }

        Object tipoPerfil = sessao.getAttribute("tipoPerfil");
        if (!"ADMINISTRADOR".equals(tipoPerfil)) {
            throw new AcessoNegadoException("Este recurso é exclusivo para administradores.");
        }
    }
}