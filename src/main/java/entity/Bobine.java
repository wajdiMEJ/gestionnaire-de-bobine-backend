package entity;

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

    public boolean estDisponible() {
        return statut == StatutBobine.DISPONIBLE && longueurRestante > 0;
    }

    public void consommer(float metres) {
        this.longueurRestante -= metres;
        this.numeroCoupe += 1;
        if (this.longueurRestante <= 0) {
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
