package dto;

import entity.Couleur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PreparationFabricationResponse {
    private Long typeCableId;
    private String nomTypeCable;
    private float section;
    private float metrageNecessaire;
    private boolean estRealisable;

    @Builder.Default
    private List<Couleur> couleursRequises = new ArrayList<>();

    @Builder.Default
    private List<BesoinCouleurDto> besoinsParCouleur = new ArrayList<>();
}
