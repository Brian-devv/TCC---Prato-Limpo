package com.restaurante.sistema.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Subtipo de Usuario. Igual a Aluno, mas com as datas de início e fim
 * de estágio, conforme o diagrama do projeto.
 */
@Entity
@Table(name = "estagiario")
@Getter
@Setter
@NoArgsConstructor
public class Estagiario extends Usuario {

    private String curso;
    private String instituicao;
    private Integer matricula;
    private LocalDate dataInicioEstagio;
    private LocalDate dataFimEstagio;
}