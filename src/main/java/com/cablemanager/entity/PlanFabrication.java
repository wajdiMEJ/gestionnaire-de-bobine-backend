package com.cablemanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanFabrication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date dateGeneration;

    private boolean estRealisable;

    private float metrageTotal;

    private float section;

    private String nomTypeCable;

    @OneToOne
    @JoinColumn(name = "commande_id")
    @JsonIgnoreProperties("planFabrication")
    private CommandeFabrication commande;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnoreProperties("plan")
    private List<BobineUtilisee> bobinesUtilisees = new ArrayList<>();

    public Map<Couleur, List<BobineUtilisee>> getAffectations() {
        Map<Couleur, List<BobineUtilisee>> map = new HashMap<>();
        if (bobinesUtilisees != null) {
            for (BobineUtilisee bu : bobinesUtilisees) {
                if (bu.getBobine() != null) {
                    Couleur c = bu.getBobine().getCouleur();
                    map.computeIfAbsent(c, k -> new ArrayList<>()).add(bu);
                }
            }
        }
        return map;
    }

    public List<Couleur> getCouleurManquantes() {
        List<Couleur> manquantes = new ArrayList<>();
        if (commande == null) return manquantes;
        Map<Couleur, Float> besoins = commande.getBesoinsParCouleur();
        Map<Couleur, List<BobineUtilisee>> affs = getAffectations();

        for (Map.Entry<Couleur, Float> entry : besoins.entrySet()) {
            Couleur c = entry.getKey();
            float besoin = entry.getValue();
            List<BobineUtilisee> list = affs.get(c);
            float totalAffecte = 0;
            if (list != null) {
                for (BobineUtilisee bu : list) {
                    totalAffecte += bu.getMetresUtilises();
                }
            }
            if (totalAffecte < besoin) {
                manquantes.add(c);
            }
        }
        return manquantes;
    }

    /**
     * Applique la consommation du métrage sur chaque bobine utilisée.
     * SOUSTRAIT le métrage utilisé de la longueur restante.
     */
    public void appliquer() {
        if (!estRealisable) {
            throw new IllegalStateException("Le plan n'est pas réalisable. Impossible d'appliquer la consommation.");
        }
        
        if (bobinesUtilisees != null) {
            for (BobineUtilisee bu : bobinesUtilisees) {
                if (bu.getBobine() != null && bu.getMetresUtilises() > 0) {
                    // Consommer le métrage (soustraction)
                    bu.getBobine().consommer(bu.getMetresUtilises());
                    
                    // Forcer la cohérence des données
                    bu.getBobine().calculerPourcentageRestant();
                }
            }
        }
        
        // Confirmer la commande
        if (commande != null) {
            commande.confirmer();
        }
    }
}