package com.restaurante.sistema.repository;

import com.restaurante.sistema.model.Gestor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GestorRepository extends JpaRepository<Gestor, Long> {
}