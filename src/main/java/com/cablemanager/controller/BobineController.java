package com.cablemanager.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cablemanager.entity.Bobine;
import com.cablemanager.entity.Couleur;
import com.cablemanager.service.GestionnaireStock;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/bobines")
@CrossOrigin(origins = "*")
public class BobineController {

    private final GestionnaireStock gestionnaireStock;

    @Autowired
    public BobineController(GestionnaireStock gestionnaireStock) {
        this.gestionnaireStock = gestionnaireStock;
    }

    // ── GET ALL ───────────────────────────────────────────────
    @GetMapping
    @Operation(summary = "Lister toutes les bobines")
    @ApiResponse(responseCode = "200", description = "Liste récupérée")
    public List<Bobine> getAllBobines() {
        return gestionnaireStock.listerTout();
    }

    // ── POST — Ajouter ────────────────────────────────────────
    @PostMapping
    @Operation(summary = "Ajouter une bobine")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bobine créée"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<?> addBobine(
            @RequestBody Bobine bobine,
            @RequestParam(required = false) Long employeId) {

        if (bobine == null) {
            return ResponseEntity.badRequest()
                .body("Le corps de la requête est obligatoire.");
        }

        // Retourne la Bobine complète avec son ID généré
        Bobine saved = gestionnaireStock.ajouterBobine(bobine, employeId);
        return ResponseEntity.ok(saved);
    }

    // ── PUT — Modifier longueurRestante seulement ─────────────
    @PutMapping("/{id}/longueur")
    @Operation(summary = "Mettre à jour uniquement la longueur restante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Longueur mise à jour"),
            @ApiResponse(responseCode = "400", description = "Paramètres invalides"),
            @ApiResponse(responseCode = "404", description = "Bobine introuvable")
    })
    public ResponseEntity<String> updateBobineLength(
            @PathVariable Long id,
            @RequestParam float longueurRestante,
            @RequestParam(required = false) Long employeId) {

        if (longueurRestante < 0) {
            return ResponseEntity.badRequest()
                .body("longueurRestante doit être >= 0.");
        }

        try {
            gestionnaireStock.mettreAJour(id, longueurRestante, employeId);
            return ResponseEntity.ok("Bobine mise à jour avec succès.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── PUT — Modifier tous les champs ────────────────────────
    @PutMapping("/{id}")
    @Operation(summary = "Modifier tous les champs d'une bobine")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bobine modifiée"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Bobine introuvable")
    })
    public ResponseEntity<?> updateBobineComplet(
            @PathVariable Long id,
            @RequestBody Bobine bobineModifiee,
            @RequestParam(required = false) Long employeId) {

        if (bobineModifiee == null) {
            return ResponseEntity.badRequest()
                .body("Le corps de la requête est obligatoire.");
        }

        try {
            Bobine updated = gestionnaireStock
                .mettreAJourComplet(id, bobineModifiee, employeId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── DELETE — Supprimer ────────────────────────────────────
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une bobine")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bobine supprimée"),
            @ApiResponse(responseCode = "404", description = "Bobine introuvable")
    })
    public ResponseEntity<String> deleteBobine(@PathVariable Long id) {
        try {
            gestionnaireStock.supprimerBobine(id);
            return ResponseEntity.ok("Bobine supprimée avec succès.");
        } catch (EmptyResultDataAccessException | IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Impossible de supprimer la bobine car elle est encore utilisée.");
        }
    }

    // ── GET par couleur ───────────────────────────────────────
    @GetMapping("/couleur/{couleur}")
    @Operation(summary = "Lister les bobines par couleur")
    @ApiResponse(responseCode = "200", description = "Liste récupérée")
    public List<Bobine> getBobinesByCouleur(@PathVariable Couleur couleur) {
        return gestionnaireStock.listerParCouleur(couleur);
    }

    // ── GET par section ───────────────────────────────────────
    @GetMapping("/section/{section}")
    @Operation(summary = "Lister les bobines par section")
    @ApiResponse(responseCode = "200", description = "Liste récupérée")
    public List<Bobine> getBobinesBySection(@PathVariable float section) {
        return gestionnaireStock.listerParSection(section);
    }
}