package com.cablemanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
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
public class BobineUtilisee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private float metresUtilises;

    private Date dateUtilisation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bobine_id")
    @JsonIgnoreProperties({"statut", "dateAjout"})
    private Bobine bobine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_fabrication_id")
    @JsonIgnoreProperties("bobinesUtilisees")
    private PlanFabrication plan;
}
