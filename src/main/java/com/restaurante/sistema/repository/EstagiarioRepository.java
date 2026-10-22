package com.restaurante.sistema.repository;

import com.restaurante.sistema.model.Estagiario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstagiarioRepository extends JpaRepository<Estagiario, Long> {
}