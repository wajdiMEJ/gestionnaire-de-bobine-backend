package com.cablemanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cablemanager.entity.Employe;
import com.cablemanager.repository.EmployeRepository;

import java.util.List;

@RestController
@RequestMapping("/api/employes")
@CrossOrigin(origins = "*")
public class EmployeController {

    private final EmployeRepository employeRepository;

    @Autowired
    public EmployeController(EmployeRepository employeRepository) {
        this.employeRepository = employeRepository;
    }

    @GetMapping
    @Operation(summary = "Lister tous les employés")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée")
    })
    public List<Employe> getAllEmployes() {
        return employeRepository.findAll();
    }

    @PostMapping
    @Operation(summary = "Créer un employé")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employé créé"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<?> createEmploye(@RequestBody Employe employe) {
        if (employe == null) {
            return ResponseEntity.badRequest().body("Le corps de la requête est obligatoire.");
        }
        Employe saved = employeRepository.save(employe);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un employé par ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employé trouvé"),
            @ApiResponse(responseCode = "404", description = "Employé introuvable")
    })
    public ResponseEntity<Employe> getEmployeById(@PathVariable Long id) {
        return employeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un employé")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employé supprimé"),
            @ApiResponse(responseCode = "404", description = "Employé introuvable")
    })
    public ResponseEntity<String> deleteEmploye(@PathVariable Long id) {
        if (employeRepository.existsById(id)) {
            employeRepository.deleteById(id);
            return ResponseEntity.ok("Employé supprimé avec succès.");
        }
        return ResponseEntity.notFound().build();
    }
}
