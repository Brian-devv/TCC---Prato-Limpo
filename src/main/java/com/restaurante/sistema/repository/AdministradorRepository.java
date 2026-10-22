package com.restaurante.sistema.repository;

import com.restaurante.sistema.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
}