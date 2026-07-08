package com.cablemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfirmationFabricationResponse {
    private Long commandeId;
    private String numeroCommande;
    private String message;
    private List<BobineUtiliseeDetailDto> bobinesUtilisees;
}