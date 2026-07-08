package com.cablemanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Bobine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reference;

    @Enumerated(EnumType.STRING)
    private Couleur couleur;

    private float section;

    private float longueurInitiale;

    private float longueurRestante;

    private int numeroCoupe;

    private String lot;

    @JsonProperty("nTouret")
    @Column(name = "n_touret")
    private String nTouret;

    private Date dateAjout;

    @Enumerated(EnumType.STRING)
    private StatutBobine statut;

    public float getPourcentageRestant() {
        if (longueurInitiale <= 0) return 0;
        return (longueurRestante / longueurInitiale) * 100f;
    }

    /**
     * Recalcule et met à jour le pourcentage restant.
     * Appelé après consommation.
     */
    public void calculerPourcentageRestant() {
        // Le pourcentage est calculé à la volée par getPourcentageRestant()
        // Cette méthode force la cohérence des données
        if (this.longueurRestante < 0) {
            this.longueurRestante = 0f;
        }
        if (this.longueurRestante > this.longueurInitiale) {
            this.longueurRestante = this.longueurInitiale;
        }
    }

    public boolean estDisponible() {
        return statut == StatutBobine.DISPONIBLE && longueurRestante > 0;
    }

    /**
     * Consomme un métrage sur la bobine.
     * SOUSTRAIT le métrage de la longueur restante.
     */
    public void consommer(float metres) {
        if (metres <= 0) {
            throw new IllegalArgumentException("Le métrage à consommer doit être positif.");
        }
        if (metres > this.longueurRestante) {
            throw new IllegalStateException(
                String.format("Impossible de consommer %.2fm (reste: %.2fm)", metres, this.longueurRestante)
            );
        }
        
        this.longueurRestante -= metres;
        this.numeroCoupe += 1;
        
        if (this.longueurRestante <= 0.001f) {
            this.longueurRestante = 0f;
            this.statut = StatutBobine.EPUISEE;
        }
    }

    @Override
    public String toString() {
        return "Bobine{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", couleur=" + couleur +
                ", section=" + section +
                ", longueurRestante=" + longueurRestante +
                ", nTouret='" + nTouret + '\'' +
                '}';
    }
}