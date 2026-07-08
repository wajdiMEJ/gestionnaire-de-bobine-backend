package com.cablemanager.dto;

import com.cablemanager.entity.Couleur;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BobineSuggestionDto {
    private Long bobineId;
    private String reference;
    private Couleur couleur;
    private float section;
    private float longueurInitiale;
    private float longueurRestante;
    private float metresAprelever;
    private boolean entamee;
}
