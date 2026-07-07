package controller;

import dto.PreparationFabricationRequest;
import dto.PreparationFabricationResponse;
import entity.CommandeFabrication;
import entity.Employe;
import entity.PlanFabrication;
import entity.StatutCommande;
import entity.TypeCableConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repository.CommandeFabricationRepository;
import repository.EmployeRepository;
import repository.TypeCableConfigRepository;
import service.FabricationService;

import java.util.List;

@RestController
@RequestMapping("/api/commandes")
@CrossOrigin(origins = "*")
public class CommandeFabricationController {

    private final CommandeFabricationRepository commandeFabricationRepository;
    private final TypeCableConfigRepository typeCableConfigRepository;
    private final EmployeRepository employeRepository;
    private final FabricationService fabricationService;

    @Autowired
    public CommandeFabricationController(CommandeFabricationRepository commandeFabricationRepository,
                                         TypeCableConfigRepository typeCableConfigRepository,
                                         EmployeRepository employeRepository,
                                         FabricationService fabricationService) {
        this.commandeFabricationRepository = commandeFabricationRepository;
        this.typeCableConfigRepository = typeCableConfigRepository;
        this.employeRepository = employeRepository;
        this.fabricationService = fabricationService;
    }

    @GetMapping
    @Operation(summary = "Lister toutes les commandes de fabrication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée")
    })
    public List<CommandeFabrication> getAllCommandes() {
        return commandeFabricationRepository.findAll();
    }

    @PostMapping
    @Operation(summary = "Créer une commande de fabrication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Commande créée"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<?> createCommande(@RequestBody CommandeFabrication commande) {
        if (commande.getTypeCable() == null || commande.getTypeCable().getId() == null) {
            return ResponseEntity.badRequest().body("Le type de câble configuré est requis.");
        }
        
        TypeCableConfig config = typeCableConfigRepository.findById(commande.getTypeCable().getId())
                .orElse(null);
                
        if (config == null) {
            return ResponseEntity.badRequest().body("Configuration type de câble introuvable.");
        }
        
        commande.setTypeCable(config);
        commande.setStatut(StatutCommande.EN_ATTENTE);
        
        // Resolve Employe if passed
        if (commande.getEmploye() != null && commande.getEmploye().getId() != null) {
            Employe emp = employeRepository.findById(commande.getEmploye().getId()).orElse(null);
            commande.setEmploye(emp);
        }
        
        try {
            CommandeFabrication saved = commandeFabricationRepository.save(commande);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la création de la commande : " + e.getMessage());
        }
    }

    @PostMapping("/preview")
    @Operation(summary = "Prévisualiser les besoins et les bobines suggérées")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prévisualisation générée"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<?> previewFabrication(@RequestBody PreparationFabricationRequest request) {
        if (request == null || request.getTypeCableId() == null) {
            return ResponseEntity.badRequest().body("Le type de câble est requis.");
        }
        if (request.getSection() <= 0 || request.getMetrageNecessaire() <= 0) {
            return ResponseEntity.badRequest().body("La section et le métrage doivent être supérieurs à 0.");
        }

        TypeCableConfig config = typeCableConfigRepository.findById(request.getTypeCableId()).orElse(null);
        if (config == null) {
            return ResponseEntity.badRequest().body("Configuration type de câble introuvable.");
        }

        try {
            CommandeFabrication commande = new CommandeFabrication();
            commande.setTypeCable(config);
            commande.setSection(request.getSection());
            commande.setMetrageNecessaire(request.getMetrageNecessaire());

            PreparationFabricationResponse response = fabricationService.previsualiserFabrication(commande);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/preparer")
    @Operation(summary = "Préparer la fabrication d'une commande")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan généré"),
            @ApiResponse(responseCode = "404", description = "Commande introuvable"),
            @ApiResponse(responseCode = "400", description = "Préparation impossible")
    })
    public ResponseEntity<?> preparerFabrication(@PathVariable Long id) {
        try {
            CommandeFabrication commande = commandeFabricationRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Commande introuvable avec l'ID: " + id));
            
            PlanFabrication plan = fabricationService.preparerFabrication(commande);
            return ResponseEntity.ok(plan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/confirmer")
    @Operation(summary = "Confirmer la fabrication d'une commande")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fabrication confirmée"),
            @ApiResponse(responseCode = "404", description = "Commande introuvable"),
            @ApiResponse(responseCode = "400", description = "Confirmation impossible")
    })
    public ResponseEntity<?> confirmerFabrication(@PathVariable Long id) {
        try {
            CommandeFabrication commande = commandeFabricationRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Commande introuvable avec l'ID: " + id));
            
            PlanFabrication plan = commande.getPlanFabrication();
            if (plan == null) {
                return ResponseEntity.badRequest().body("Aucun plan de fabrication généré pour cette commande.");
            }
            
            fabricationService.confirmerFabrication(plan);
            return ResponseEntity.ok("Fabrication confirmée avec succès. Le stock a été mis à jour.");
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/annuler")
                @Operation(summary = "Annuler la fabrication d'une commande")
                @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Fabrication annulée"),
                        @ApiResponse(responseCode = "404", description = "Commande introuvable"),
                        @ApiResponse(responseCode = "400", description = "Annulation impossible")
                })
    public ResponseEntity<?> annulerFabrication(@PathVariable Long id) {
        try {
            CommandeFabrication commande = commandeFabricationRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Commande introuvable avec l'ID: " + id));
            
            fabricationService.annulerFabrication(commande);
            return ResponseEntity.ok("Fabrication annulée avec succès.");
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
