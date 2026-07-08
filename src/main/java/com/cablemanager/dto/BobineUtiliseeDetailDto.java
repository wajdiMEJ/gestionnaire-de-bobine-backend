package com.cablemanager.dto;

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
public class BobineUtiliseeDetailDto {
    private Long bobineId;
    private String reference;
    private String lot;
    private String nTouret;
    private String couleur;
    private float section;
    private float ancienneLongueur;
    private float metresPreleves;
    private float nouvelleLongueur;
    private String qrCodeUrl;
}