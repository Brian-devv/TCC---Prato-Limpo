package com.restaurante.sistema.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Subtipo de Usuario. Campo extra conforme o diagrama do projeto. */
@Entity
@Table(name = "nutricionista")
@Getter
@Setter
@NoArgsConstructor
public class Nutricionista extends Usuario {

    private String registroProfissional;
}