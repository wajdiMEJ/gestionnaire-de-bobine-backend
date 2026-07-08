package com.cablemanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cablemanager.entity.Bobine;
import com.cablemanager.entity.Couleur;
import com.cablemanager.service.RechercheStock;

import java.util.List;

@RestController
@RequestMapping("/api/fabrication")
@CrossOrigin(origins = "*")
public class FabricationController {

    private final RechercheStock rechercheStock;

    @Autowired
    public FabricationController(RechercheStock rechercheStock) {
        this.rechercheStock = rechercheStock;
    }

    @GetMapping("/total-disponible")
    @Operation(summary = "Calculer le stock total disponible")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total calculé"),
            @ApiResponse(responseCode = "400", description = "Paramètres invalides")
    })
    public ResponseEntity<?> getTotalDisponible(@RequestParam Couleur couleur, @RequestParam float section) {
        if (section <= 0) {
            return ResponseEntity.badRequest().body("section doit être > 0.");
        }
        return ResponseEntity.ok(rechercheStock.calculerTotalDisponible(couleur, section));
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Suggérer des bobines pour une fabrication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Suggestions calculées"),
            @ApiResponse(responseCode = "400", description = "Paramètres invalides")
    })
    public ResponseEntity<?> getSuggestions(@RequestParam Couleur couleur,
                                            @RequestParam float section,
                                            @RequestParam float besoin) {
        if (section <= 0 || besoin <= 0) {
            return ResponseEntity.badRequest().body("section et besoin doivent être > 0.");
        }
        return ResponseEntity.ok(rechercheStock.suggererBobines(couleur, section, besoin));
    }
}