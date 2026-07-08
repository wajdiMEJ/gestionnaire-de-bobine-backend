package com.cablemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PreparationFabricationRequest {
    private Long typeCableId;
    private float section;
    private float metrageNecessaire;
}
