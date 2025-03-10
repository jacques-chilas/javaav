package com.schoolproject.javaav.repository;

import com.schoolproject.javaav.entity.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

    boolean existsByNumEt(String numEt);
}
