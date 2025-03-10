package com.schoolproject.javaav.service;

import com.schoolproject.javaav.entity.Etudiant;
import com.schoolproject.javaav.repository.EtudiantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EtudiantService {
    @Autowired
    private EtudiantRepository etudiantRepository;

    public List<Etudiant> getAllEtudiants() {
        return etudiantRepository.findAll();
    }

    public Etudiant addEtudiant(Etudiant etudiant) {
        if (etudiantRepository.existsByNumEt(etudiant.getNumEt())) {
            throw new RuntimeException("Un étudiant avec ce numéro existe déjà!");
        }
        return etudiantRepository.save(etudiant);
    }

    public Etudiant updateEtudiant(Long id, Etudiant newEtudiant) {
        Etudiant etudiant = etudiantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        etudiant.setNumEt(newEtudiant.getNumEt());
        etudiant.setNom(newEtudiant.getNom());
        etudiant.setPrenom(newEtudiant.getPrenom());
        etudiant.setMoyenne(newEtudiant.getMoyenne());

        return etudiantRepository.save(etudiant);
    }


    public void deleteEtudiant(Long id) {
        etudiantRepository.deleteById(id);
    }
}

