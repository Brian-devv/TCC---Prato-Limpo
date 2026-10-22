package com.restaurante.sistema.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Subtipo de Usuario. Assim como Administrador, não é criada por
 * autocadastro público — só o administrador pode cadastrar um
 * Gestor, pela caixa "Cadastrar funcionário ou gestor" do painel.
 */
@Entity
@Table(name = "gestor")
@Getter
@Setter
@NoArgsConstructor
public class Gestor extends Usuario {

    private String registroProfissional;
}