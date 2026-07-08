package com.cablemanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cablemanager.entity.Couleur;
import com.cablemanager.entity.TypeCableConfig;
import com.cablemanager.repository.TypeCableConfigRepository;

import java.util.List;
import java.util.Objects;

@Service
public class TypeCableConfigService {

    private final TypeCableConfigRepository repository;

    @Autowired
    public TypeCableConfigService(TypeCableConfigRepository repository) {
        this.repository = repository;
    }

    /**
     * Trouve un type existant par nom + couleurs, ou crée un nouveau.
     * Évite les doublons en base de données.
     */
    public TypeCableConfig trouverOuCreer(String nom, List<Couleur> couleurs, 
                                           String description, int nombreConducteurs) {
        // Recherche exacte par nom et couleurs
        List<TypeCableConfig> existants = repository.findAll();
        for (TypeCableConfig t : existants) {
            if (t.getNom() != null 
                && t.getNom().equalsIgnoreCase(nom)
                && Objects.equals(t.getCouleurs(), couleurs)) {
                return t; // Réutilise l'existant
            }
        }
        
        // Crée un nouveau type
        TypeCableConfig nouveau = new TypeCableConfig();
        nouveau.setNom(nom);
        nouveau.setDescription(description);
        nouveau.setCouleurs(couleurs);
        nouveau.setNombreConducteurs(nombreConducteurs);
        return repository.save(nouveau);
    }
}