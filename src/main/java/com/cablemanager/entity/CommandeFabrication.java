package com.cablemanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommandeFabrication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroCommande;

    private String nomClient;

    /**
     * Section "legacy" gardée pour compatibilité/affichage.
     * La vraie logique utilise désormais sectionsParCouleur (une section par couleur).
     */
    private float section;

    private float metrageNecessaire;

    private Date dateCommande;

    private Date dateLivraison;

    @Enumerated(EnumType.STRING)
    private StatutCommande statut;

    private String observations;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_cable_id")
    @JsonIgnoreProperties({"couleurs"})
    private TypeCableConfig typeCable;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employe_id")
    @JsonIgnoreProperties({"commandes"})
    private Employe employe;

    @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("commande")
    private PlanFabrication planFabrication;

    /**
     * Section spécifique pour chaque couleur du câble.
     * Ex : NOIR -> 10mm², ROUGE -> 20mm²
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "commande_sections_couleur", joinColumns = @JoinColumn(name = "commande_id"))
    @MapKeyColumn(name = "couleur")
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "section")
    private Map<Couleur, Float> sectionsParCouleur = new HashMap<>();

    public Map<Couleur, Float> getBesoinsParCouleur() {
        Map<Couleur, Float> besoins = new HashMap<>();
        if (typeCable != null && typeCable.getCouleurs() != null) {
            for (Couleur couleur : typeCable.getCouleurs()) {
                besoins.put(couleur, this.metrageNecessaire);
            }
        }
        return besoins;
    }

    /**
     * Retourne la section à utiliser pour une couleur donnée.
     * Si aucune section spécifique n'est définie pour cette couleur,
     * on retombe sur la section "legacy" unique.
     */
    public float getSectionPourCouleur(Couleur couleur) {
        if (sectionsParCouleur != null && sectionsParCouleur.containsKey(couleur)) {
            return sectionsParCouleur.get(couleur);
        }
        return this.section;
    }

    public boolean estRealisable() {
        return planFabrication != null && planFabrication.isEstRealisable();
    }

    public void confirmer() {
        this.statut = StatutCommande.TERMINEE;
    }
}