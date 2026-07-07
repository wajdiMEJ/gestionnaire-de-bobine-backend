package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
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

    public Map<Couleur, Float> getBesoinsParCouleur() {
        Map<Couleur, Float> besoins = new HashMap<>();
        if (typeCable != null && typeCable.getCouleurs() != null) {
            for (Couleur couleur : typeCable.getCouleurs()) {
                besoins.put(couleur, this.metrageNecessaire);
            }
        }
        return besoins;
    }

    public boolean estRealisable() {
        return planFabrication != null && planFabrication.isEstRealisable();
    }

    public void confirmer() {
        this.statut = StatutCommande.TERMINEE;
    }
}
