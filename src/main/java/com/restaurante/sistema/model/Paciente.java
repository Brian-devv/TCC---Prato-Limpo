package com.restaurante.sistema.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Subtipo de Usuario. Campos extras conforme o diagrama do projeto. */
@Entity
@Table(name = "paciente")
@Getter
@Setter
@NoArgsConstructor
public class Paciente extends Usuario {

    private String restricoesAlimentares;
    private Integer numeroProntuario;
}