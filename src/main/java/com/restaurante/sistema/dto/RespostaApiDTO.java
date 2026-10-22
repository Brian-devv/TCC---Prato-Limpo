package com.restaurante.sistema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Envelope padrão de resposta da API. O front (cadastro.js) sempre
 * vai receber um JSON no formato:
 * { "sucesso": true/false, "mensagem": "...", "dados": {...} }
 */
@Getter
@Setter
@AllArgsConstructor
public class RespostaApiDTO {
    private boolean sucesso;
    private String mensagem;
    private Object dados;

    public RespostaApiDTO(boolean sucesso, String mensagem) {
        this(sucesso, mensagem, null);
    }
}