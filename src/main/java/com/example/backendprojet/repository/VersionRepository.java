// VersionRepository.java
package com.example.backendprojet.repository;

import com.example.backendprojet.entity.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VersionRepository extends JpaRepository<Version, Long> {

    // Récupère toutes les versions d'une règle, de la plus récente à la plus ancienne
    List<Version> findByRegleMetierIdOrderByNumeroVersionDesc(Long regleId);
}