package com.example.backendprojet.repository;

import com.example.backendprojet.entity.DossierExport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DossierExportRepository extends JpaRepository<DossierExport, UUID> {
}
