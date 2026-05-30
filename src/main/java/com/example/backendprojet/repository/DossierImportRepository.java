package com.example.backendprojet.repository;

import com.example.backendprojet.entity.DossierImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DossierImportRepository extends JpaRepository<DossierImport, UUID> {
}
