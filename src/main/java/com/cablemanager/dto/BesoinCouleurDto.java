package com.cablemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import com.cablemanager.entity.Couleur;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BesoinCouleurDto {
    private Couleur couleur;
    private float besoin;
    private float disponible;
    private boolean suffisant;

    @Builder.Default
    private List<BobineSuggestionDto> suggestions = new ArrayList<>();
}
