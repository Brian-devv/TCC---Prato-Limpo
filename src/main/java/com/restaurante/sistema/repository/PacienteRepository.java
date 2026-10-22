package com.restaurante.sistema.repository;

import com.restaurante.sistema.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
}