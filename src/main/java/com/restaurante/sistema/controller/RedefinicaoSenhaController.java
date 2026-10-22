package com.restaurante.sistema.controller;

import com.restaurante.sistema.dto.ConfirmarRedefinicaoSenhaDTO;
import com.restaurante.sistema.dto.RespostaApiDTO;
import com.restaurante.sistema.dto.SolicitarRedefinicaoSenhaDTO;
import com.restaurante.sistema.service.RedefinicaoSenhaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints chamados por redefinir-senha.js e redefinir-senha-confirmar.js.
 */
@RestController
@RequestMapping("/api/auth/redefinir-senha")
@RequiredArgsConstructor
public class RedefinicaoSenhaController {

    private final RedefinicaoSenhaService redefinicaoSenhaService;

    @PostMapping("/solicitar")
    public ResponseEntity<RespostaApiDTO> solicitar(@Valid @RequestBody SolicitarRedefinicaoSenhaDTO dto) {
        redefinicaoSenhaService.solicitarRedefinicao(dto.getEmail());
        // Mensagem sempre igual, exista ou não o e-mail (não revela quais contas existem).
        return ResponseEntity.ok(new RespostaApiDTO(
                true,
                "Se o e-mail existir em nossa base, um link de redefinição foi enviado."
        ));
    }

    @PostMapping("/confirmar")
    public ResponseEntity<RespostaApiDTO> confirmar(@Valid @RequestBody ConfirmarRedefinicaoSenhaDTO dto) {
        redefinicaoSenhaService.confirmarRedefinicao(dto);
        return ResponseEntity.ok(new RespostaApiDTO(
                true,
                "Senha redefinida com sucesso! Você já pode entrar com a nova senha."
        ));
    }
}