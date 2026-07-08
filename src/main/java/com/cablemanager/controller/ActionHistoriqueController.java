package com.cablemanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cablemanager.entity.ActionHistorique;
import com.cablemanager.repository.ActionHistoriqueRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/historique")
@CrossOrigin(origins = "*")
public class ActionHistoriqueController {

    private final ActionHistoriqueRepository actionHistoriqueRepository;

    @Autowired
    public ActionHistoriqueController(ActionHistoriqueRepository actionHistoriqueRepository) {
        this.actionHistoriqueRepository = actionHistoriqueRepository;
    }

    @GetMapping
    @Operation(summary = "Lister l'historique des actions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historique récupéré")
    })
    public List<ActionHistorique> getHistorique() {
        return actionHistoriqueRepository.findByOrderByDateDesc();
    }

    @GetMapping("/resumes")
    @Operation(summary = "Lister les résumés textuels de l'historique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Résumés récupérés")
    })
    public List<String> getResumes() {
        return actionHistoriqueRepository.findByOrderByDateDesc().stream()
                .map(ActionHistorique::getResume)
                .collect(Collectors.toList());
    }
}
