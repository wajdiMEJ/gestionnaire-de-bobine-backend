package entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TypeCableConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "type_cable_couleurs", joinColumns = @JoinColumn(name = "type_cable_config_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "couleur")
    private List<Couleur> couleurs = new ArrayList<>();

    private int nombreConducteurs;

    public int getNombreDeConducteurs() {
        if (couleurs != null) {
            return couleurs.size();
        }
        return nombreConducteurs;
    }
}
