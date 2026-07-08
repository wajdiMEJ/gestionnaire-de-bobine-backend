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
     * Sélectionne les bobines nécessaires : toutes les bobines dispo (neuves ou entamées confondues)
     * triées de la PLUS PETITE à la PLUS GRANDE. On prend la plus petite qui suffit seule,
     * sinon on cumule en commençant par les plus petites.
     */
    public List<Bobine> suggererBobines(Couleur couleur, float section, float besoin) {
        List<Bobine> toutes = bobineRepository.findByCouleurAndSectionAndStatut(couleur, section, StatutBobine.DISPONIBLE);

        List<Bobine> disponibles = toutes.stream()
                .filter(b -> b.getLongueurRestante() > 0)
                .sorted(Comparator.comparingDouble(Bobine::getLongueurRestante))
                .collect(Collectors.toList());

        List<Bobine> suggestions = new ArrayList<>();
        if (besoin <= 0 || disponibles.isEmpty()) {
            return suggestions;
        }

        Bobine bestFit = null;
        for (Bobine b : disponibles) {
            if (b.getLongueurRestante() >= besoin) {
                bestFit = b;
                break;
            }
        }

        if (bestFit != null) {
            suggestions.add(bestFit);
            return suggestions;
        }

        float reste = besoin;
        for (Bobine b : disponibles) {
            if (reste <= 0) break;
            suggestions.add(b);
            reste -= b.getLongueurRestante();
        }

        return suggestions;
    }

    /**
     * Vérifie la faisabilité en utilisant la section SPÉCIFIQUE à chaque couleur.
     */
    public boolean verifierFaisabilite(CommandeFabrication commande) {
        if (commande == null) return false;

        Map<Couleur, Float> besoins = commande.getBesoinsParCouleur();

        for (Map.Entry<Couleur, Float> entry : besoins.entrySet()) {
            Couleur couleur = entry.getKey();
            float besoin = entry.getValue();
            float section = commande.getSectionPourCouleur(couleur);
            float dispo = calculerTotalDisponible(couleur, section);
            if (dispo < besoin) {
                return false;
            }
        }
        return true;
    }
}