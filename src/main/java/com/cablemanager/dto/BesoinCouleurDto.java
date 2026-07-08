package com.cablemanager.dto;

import com.cablemanager.entity.Couleur;
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
public class BesoinCouleurDto {
    private Couleur couleur;
    private float section;
    private float besoin;
    private float disponible;
    private boolean suffisant;

    @Builder.Default
    private List<BobineSuggestionDto> suggestions = new ArrayList<>();
}