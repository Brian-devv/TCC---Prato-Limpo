package com.restaurante.sistema.controller;

import com.restaurante.sistema.dto.CadastroFuncionarioDTO;
import com.restaurante.sistema.dto.RespostaApiDTO;
import com.restaurante.sistema.dto.UsuarioResumoDTO;
import com.restaurante.sistema.exception.AcessoNegadoException;
import com.restaurante.sistema.exception.AutenticacaoNecessariaException;
import com.restaurante.sistema.model.Funcionario;
import com.restaurante.sistema.service.AdminService;
import com.restaurante.sistema.service.AutenticacaoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints exclusivos do painel administrativo: listar usuários,
 * cadastrar funcionário e excluir usuário.
 *
 * Cada método chama exigirAdministradorLogado() antes de qualquer
 * outra coisa — ela confere DUAS condições na sessão: (1) existe um
 * usuário logado? e (2) esse usuário é do tipo ADMINISTRADOR? Um
 * usuário comum logado recebe 403 (Forbidden): está autenticado, só
 * não tem permissão para este recurso.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AutenticacaoService autenticacaoService;

    public AdminController(AdminService adminService, AutenticacaoService autenticacaoService) {
        this.adminService = adminService;
        this.autenticacaoService = autenticacaoService;
    }

    /** Lista nome, e-mail, tipo e status de todos os usuários (sem dados sensíveis). */
    @GetMapping("/usuarios")
    public ResponseEntity<RespostaApiDTO> listarUsuarios(HttpSession sessao) {
        exigirAdministradorLogado(sessao);
        List<UsuarioResumoDTO> usuarios = adminService.listarUsuarios();
        return ResponseEntity.ok(new RespostaApiDTO(true, "Usuários carregados com sucesso.", usuarios));
    }

    /**
     * Cadastra um novo funcionário a partir da caixa "Cadastrar
     * funcionário" do painel administrativo. Reaproveita a mesma
     * regra de negócio (AutenticacaoService.cadastrarFuncionario) que
     * valida CPF, e-mail duplicado e faz o hash da senha.
     */
    @PostMapping("/funcionarios")
    public ResponseEntity<RespostaApiDTO> cadastrarFuncionario(@Valid @RequestBody CadastroFuncionarioDTO dto, HttpSession sessao) {
        exigirAdministradorLogado(sessao);
        Funcionario funcionarioCriado = autenticacaoService.cadastrarFuncionario(dto);
        return ResponseEntity.ok(new RespostaApiDTO(
                true,
                "Funcionário cadastrado com sucesso!",
                new UsuarioResumoDTO(funcionarioCriado)
        ));
    }

    /**
     * Exclui um usuário pelo id. O AdminService impede que o
     * administrador exclua a própria conta por este caminho.
     */
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