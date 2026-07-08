package com.cablemanager.service;

import com.cablemanager.entity.Bobine;
import com.cablemanager.entity.CommandeFabrication;
import com.cablemanager.entity.Couleur;
import com.cablemanager.entity.StatutBobine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cablemanager.repository.BobineRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RechercheStock {

    private final BobineRepository bobineRepository;

    @Autowired
    public RechercheStock(BobineRepository bobineRepository) {
        this.bobineRepository = bobineRepository;
    }

    public List<Bobine> chercherBobines(Couleur couleur, float section) {
        return bobineRepository.findByCouleurAndSection(couleur, section);
    }

    public float calculerTotalDisponible(Couleur couleur, float section) {
        List<Bobine> bobines = bobineRepository.findByCouleurAndSectionAndStatut(couleur, section, StatutBobine.DISPONIBLE);
        float total = 0f;
        for (Bobine b : bobines) {
            total += b.getLongueurRestante();
        }
        return total;
    }

    /**
     * Sélectionne les bobines nécessaires pour couvrir un besoin donné.
     *
     * RÈGLE : on regarde TOUTES les bobines disponibles (neuves ou déjà entamées confondues),
     * triées de la PLUS PETITE à la PLUS GRANDE.
     * - Si une seule bobine (la plus petite possible) suffit à couvrir le besoin, on la choisit.
     * - Sinon, on cumule en commençant par les plus petites jusqu'à couvrir le besoin.
     */
    public List<Bobine> suggererBobines(Couleur couleur, float section, float besoin) {
        List<Bobine> toutes = bobineRepository.findByCouleurAndSectionAndStatut(couleur, section, StatutBobine.DISPONIBLE);

        List<Bobine> disponibles = toutes.stream()
                .filter(b -> b.getLongueurRestante() > 0)
                .sorted(Comparator.comparingDouble(Bobine::getLongueurRestante)) // plus petite d'abord
                .collect(Collectors.toList());

        List<Bobine> suggestions = new ArrayList<>();
        if (besoin <= 0 || disponibles.isEmpty()) {
            return suggestions;
        }

        // Étape 1 : chercher la plus petite bobine qui suffit SEULE (best-fit global)
        Bobine bestFit = null;
        for (Bobine b : disponibles) {
            if (b.getLongueurRestante() >= besoin) {
                bestFit = b;
                break; // la liste est déjà triée croissante : la première qui convient est la plus petite suffisante
            }
        }

        if (bestFit != null) {
            suggestions.add(bestFit);
            return suggestions;
        }

        // Étape 2 : aucune bobine seule ne suffit → cumuler en commençant par les plus petites
        float reste = besoin;
        for (Bobine b : disponibles) {
            if (reste <= 0) break;
            suggestions.add(b);
            reste -= b.getLongueurRestante();
        }

        return suggestions;
    }

    public boolean verifierFaisabilite(CommandeFabrication commande) {
        if (commande == null) return false;

        float section = commande.getSection();
        Map<Couleur, Float> besoins = commande.getBesoinsParCouleur();

        for (Map.Entry<Couleur, Float> entry : besoins.entrySet()) {
            Couleur couleur = entry.getKey();
            float besoin = entry.getValue();
            float dispo = calculerTotalDisponible(couleur, section);
            if (dispo < besoin) {
                return false;
            }
        }
        return true;
    }
}