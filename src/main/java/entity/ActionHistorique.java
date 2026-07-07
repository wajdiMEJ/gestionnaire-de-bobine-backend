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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActionHistorique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypeAction type;

    private String description;

    private Date date;

    private float metresConsommes;

    private String referenceCommande;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employe_id")
    @JsonIgnoreProperties({"commandes"})
    private Employe employe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bobine_id")
    @JsonIgnoreProperties({"dateAjout", "statut"})
    private Bobine bobine;

    public String getResume() {
        String empName = (employe != null) ? (employe.getPrenom() + " " + employe.getNom()) : "Système";
        String bobRef = (bobine != null) ? bobine.getReference() : "N/A";
        return String.format("[%s] %s | Bobine: %s | Mètres: %.2fm | Par: %s",
                date, type, bobRef, metresConsommes, empName);
    }
}
