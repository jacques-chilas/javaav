package com.schoolproject.javaav.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "etudiant", uniqueConstraints = @UniqueConstraint(columnNames = "numEt"))
public class Etudiant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numEt;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = true)
    private String prenom;

    @Column(nullable = false)
    private double moyenne;

    // Constructeurs, Getters et Setters

    public Etudiant() {

    }

    public Etudiant(long id, String numEt, String nom, String prenom, double moyenne) {
        this.id = id;
        this.numEt = numEt;
        this.moyenne = moyenne;
        this.prenom = prenom;
        this.nom = nom;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumEt() {
        return numEt;
    }

    public void setNumEt(String numEt) {
        this.numEt = numEt;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public double getMoyenne() {
        return moyenne;
    }

    public void setMoyenne(double moyenne) {
        this.moyenne = moyenne;
    }

    public String getInfo(){
        return "NumEt: " + this.getNumEt() + "\nNom: " + this.nom + "\nPrenom: " + this.prenom + "\nMoyenne: " + this.getMoyenne();
    }
}

