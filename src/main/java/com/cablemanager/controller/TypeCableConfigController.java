package com.cablemanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cablemanager.entity.TypeCableConfig;
import com.cablemanager.repository.TypeCableConfigRepository;

import java.util.List;

@RestController
@RequestMapping("/api/type-cables")
@CrossOrigin(origins = "*")
public class TypeCableConfigController {

    private final TypeCableConfigRepository typeCableConfigRepository;

    @Autowired
    public TypeCableConfigController(TypeCableConfigRepository typeCableConfigRepository) {
        this.typeCableConfigRepository = typeCableConfigRepository;
    }

    @GetMapping
    @Operation(summary = "Lister tous les types de câbles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée")
    })
    public List<TypeCableConfig> getAllTypeCables() {
        return typeCableConfigRepository.findAll();
    }

    @PostMapping
    @Operation(summary = "Créer un type de câble")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Type de câble créé"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<?> addTypeCable(@RequestBody TypeCableConfig typeCableConfig) {
        if (typeCableConfig == null) {
            return ResponseEntity.badRequest().body("Le corps de la requête est obligatoire.");
        }
        TypeCableConfig saved = typeCableConfigRepository.save(typeCableConfig);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un type de câble par ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Type de câble trouvé"),
            @ApiResponse(responseCode = "404", description = "Type de câble introuvable")
    })
    public ResponseEntity<TypeCableConfig> getTypeCableById(@PathVariable Long id) {
        return typeCableConfigRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un type de câble")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Type de câble supprimé"),
            @ApiResponse(responseCode = "404", description = "Type de câble introuvable")
    })
    public ResponseEntity<String> deleteTypeCable(@PathVariable Long id) {
        if (typeCableConfigRepository.existsById(id)) {
            typeCableConfigRepository.deleteById(id);
            return ResponseEntity.ok("Type de câble supprimé avec succès.");
        }
        return ResponseEntity.notFound().build();
    }
}
