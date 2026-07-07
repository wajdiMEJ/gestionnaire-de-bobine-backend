package service;

import dto.BesoinCouleurDto;
import dto.BobineSuggestionDto;
import dto.PreparationFabricationResponse;
import entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FabricationService {

    private final RechercheStock rechercheStock;
    private final BobineRepository bobineRepository;
    private final PlanFabricationRepository planFabricationRepository;
    private final CommandeFabricationRepository commandeFabricationRepository;
    private final ActionHistoriqueRepository actionHistoriqueRepository;

    @Autowired
    public FabricationService(RechercheStock rechercheStock,
                              BobineRepository bobineRepository,
                              PlanFabricationRepository planFabricationRepository,
                              CommandeFabricationRepository commandeFabricationRepository,
                              ActionHistoriqueRepository actionHistoriqueRepository) {
        this.rechercheStock = rechercheStock;
        this.bobineRepository = bobineRepository;
        this.planFabricationRepository = planFabricationRepository;
        this.commandeFabricationRepository = commandeFabricationRepository;
        this.actionHistoriqueRepository = actionHistoriqueRepository;
    }

    public PlanFabrication preparerFabrication(CommandeFabrication commande) {
        if (commande == null) {
            throw new IllegalArgumentException("La commande ne peut pas être nulle.");
        }

        // Initialize PlanFabrication
        PlanFabrication plan = PlanFabrication.builder()
                .dateGeneration(new Date())
                .commande(commande)
                .metrageTotal(commande.getMetrageNecessaire())
                .section(commande.getSection())
                .nomTypeCable(commande.getTypeCable() != null ? commande.getTypeCable().getNom() : "Inconnu")
                .bobinesUtilisees(new ArrayList<>())
                .build();

        boolean estRealisable = rechercheStock.verifierFaisabilite(commande);
        plan.setEstRealisable(estRealisable);

        float section = commande.getSection();
        Map<Couleur, Float> besoins = commande.getBesoinsParCouleur();

        for (Map.Entry<Couleur, Float> entry : besoins.entrySet()) {
            Couleur couleur = entry.getKey();
            float besoinTotal = entry.getValue();
            float besoinRestant = besoinTotal;

            // Get suggested bobines for this color and section
            List<Bobine> bobinesSuggerees = rechercheStock.suggererBobines(couleur, section, besoinTotal);

            for (Bobine bobine : bobinesSuggerees) {
                if (besoinRestant <= 0) break;

                float aPrelever = Math.min(bobine.getLongueurRestante(), besoinRestant);
                besoinRestant -= aPrelever;

                BobineUtilisee bu = BobineUtilisee.builder()
                        .bobine(bobine)
                        .metresUtilises(aPrelever)
                        .dateUtilisation(new Date())
                        .plan(plan)
                        .build();

                plan.getBobinesUtilisees().add(bu);
            }
        }

        commande.setPlanFabrication(plan);
        commande.setStatut(StatutCommande.EN_COURS);
        
        // Save plan and update commande status
        planFabricationRepository.save(plan);
        commandeFabricationRepository.save(commande);

        return plan;
    }

    public PreparationFabricationResponse previsualiserFabrication(CommandeFabrication commande) {
        if (commande == null || commande.getTypeCable() == null) {
            throw new IllegalArgumentException("Les informations de la commande sont incomplètes.");
        }

        float section = commande.getSection();
        Map<Couleur, Float> besoins = commande.getBesoinsParCouleur();
        List<BesoinCouleurDto> besoinsParCouleur = new ArrayList<>();

        for (Map.Entry<Couleur, Float> entry : besoins.entrySet()) {
            Couleur couleur = entry.getKey();
            float besoin = entry.getValue();
            float disponible = rechercheStock.calculerTotalDisponible(couleur, section);
            float restant = besoin;

            List<BobineSuggestionDto> suggestions = new ArrayList<>();
            List<Bobine> bobinesSuggerees = rechercheStock.suggererBobines(couleur, section, besoin);
            for (Bobine bobine : bobinesSuggerees) {
                if (restant <= 0) {
                    break;
                }

                float metresAprelever = Math.min(bobine.getLongueurRestante(), restant);
                restant -= metresAprelever;

                suggestions.add(BobineSuggestionDto.builder()
                        .bobineId(bobine.getId())
                        .reference(bobine.getReference())
                        .couleur(bobine.getCouleur())
                        .section(bobine.getSection())
                        .longueurInitiale(bobine.getLongueurInitiale())
                        .longueurRestante(bobine.getLongueurRestante())
                        .metresAprelever(metresAprelever)
                        .entamee(bobine.getLongueurRestante() < bobine.getLongueurInitiale())
                        .build());
            }

            besoinsParCouleur.add(BesoinCouleurDto.builder()
                    .couleur(couleur)
                    .besoin(besoin)
                    .disponible(disponible)
                    .suffisant(disponible >= besoin)
                    .suggestions(suggestions)
                    .build());
        }

        return PreparationFabricationResponse.builder()
                .typeCableId(commande.getTypeCable().getId())
                .nomTypeCable(commande.getTypeCable().getNom())
                .section(commande.getSection())
                .metrageNecessaire(commande.getMetrageNecessaire())
                .couleursRequises(new ArrayList<>(commande.getTypeCable().getCouleurs()))
                .estRealisable(rechercheStock.verifierFaisabilite(commande))
                .besoinsParCouleur(besoinsParCouleur)
                .build();
    }

    public void confirmerFabrication(PlanFabrication plan) {
        if (plan == null) {
            throw new IllegalArgumentException("Le plan de fabrication ne peut pas être nul.");
        }

        PlanFabrication planPersiste = planFabricationRepository.findById(plan.getId())
                .orElseThrow(() -> new IllegalArgumentException("Plan de fabrication introuvable avec l'ID: " + plan.getId()));

        if (!planPersiste.isEstRealisable()) {
            throw new IllegalStateException("Le plan de fabrication n'est pas réalisable (stock insuffisant).");
        }

        // Apply consumption (reduces reel lengths and sets status)
        planPersiste.appliquer();

        // Save updated bobines and log Action for each spool consumed
        for (BobineUtilisee bu : planPersiste.getBobinesUtilisees()) {
            Bobine bobine = bu.getBobine();
            bobineRepository.save(bobine);

            // Log stock usage action
            ActionHistorique action = ActionHistorique.builder()
                    .type(TypeAction.UTILISATION_RESTE)
                    .description(String.format("Prélèvement de %.2fm pour la fabrication de la commande %s.",
                            bu.getMetresUtilises(), planPersiste.getCommande().getNumeroCommande()))
                    .date(new Date())
                    .metresConsommes(bu.getMetresUtilises())
                    .referenceCommande(planPersiste.getCommande().getNumeroCommande())
                    .employe(planPersiste.getCommande().getEmploye())
                    .bobine(bobine)
                    .build();

            actionHistoriqueRepository.save(action);
        }

        // Save updated order
        commandeFabricationRepository.save(planPersiste.getCommande());
        
        // Save plan state
        planFabricationRepository.save(planPersiste);
    }

    public void annulerFabrication(CommandeFabrication commande) {
        if (commande == null) {
            throw new IllegalArgumentException("La commande ne peut pas être nulle.");
        }

        CommandeFabrication cmdPersiste = commandeFabricationRepository.findById(commande.getId())
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable avec l'ID: " + commande.getId()));

        if (cmdPersiste.getStatut() == StatutCommande.TERMINEE) {
            throw new IllegalStateException("Impossible d'annuler une fabrication déjà terminée.");
        }

        // Log cancellation action
        ActionHistorique action = ActionHistorique.builder()
                .type(TypeAction.ANNULATION)
                .description("Annulation de la commande de fabrication " + cmdPersiste.getNumeroCommande())
                .date(new Date())
                .metresConsommes(0f)
                .referenceCommande(cmdPersiste.getNumeroCommande())
                .employe(cmdPersiste.getEmploye())
                .bobine(null)
                .build();

        actionHistoriqueRepository.save(action);

        // Remove generated plan if exists
        PlanFabrication plan = cmdPersiste.getPlanFabrication();
        if (plan != null) {
            cmdPersiste.setPlanFabrication(null);
            planFabricationRepository.delete(plan);
        }

        cmdPersiste.setStatut(StatutCommande.ANNULEE);
        commandeFabricationRepository.save(cmdPersiste);
    }
}
