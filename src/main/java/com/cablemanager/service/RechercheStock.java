package com.cablemanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cablemanager.entity.Bobine;
import com.cablemanager.entity.CommandeFabrication;
import com.cablemanager.entity.Couleur;
import com.cablemanager.entity.StatutBobine;
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

    public List<Bobine> suggererBobines(Couleur couleur, float section, float besoin) {
        List<Bobine> toutes = bobineRepository.findByCouleurAndSectionAndStatut(couleur, section, StatutBobine.DISPONIBLE);
        
        // Filter out empty reels just in case
        List<Bobine> disponibles = toutes.stream()
                .filter(b -> b.getLongueurRestante() > 0)
                .collect(Collectors.toList());

        List<Bobine> suggestions = new ArrayList<>();
        if (besoin <= 0 || disponibles.isEmpty()) {
            return suggestions;
        }

        // Separate open (entamées) vs new (neuves) reels
        List<Bobine> entamees = disponibles.stream()
                .filter(b -> b.getLongueurRestante() < b.getLongueurInitiale())
                .sorted(Comparator.comparingDouble(Bobine::getLongueurRestante)) // Sort ascending (empty small first)
                .collect(Collectors.toList());

        List<Bobine> neuves = disponibles.stream()
                .filter(b -> b.getLongueurRestante() == b.getLongueurInitiale())
                .sorted((b1, b2) -> Float.compare(b2.getLongueurRestante(), b1.getLongueurRestante())) // Sort descending (largest new first)
                .collect(Collectors.toList());

        // Strategy 1: Check if there's a SINGLE open reel that satisfies the need (Best Fit)
        Bobine bestSingleFit = null;
        float minOverplus = Float.MAX_VALUE;
        for (Bobine b : entamees) {
            if (b.getLongueurRestante() >= besoin) {
                float overplus = b.getLongueurRestante() - besoin;
                if (overplus < minOverplus) {
                    minOverplus = overplus;
                    bestSingleFit = b;
                }
            }
        }

        if (bestSingleFit != null) {
            suggestions.add(bestSingleFit);
            return suggestions;
        }

        // Strategy 2: If no single open reel fits, accumulate open reels (from smallest to largest to empty stock)
        float accumule = 0f;
        for (Bobine b : entamees) {
            suggestions.add(b);
            accumule += b.getLongueurRestante();
            if (accumule >= besoin) {
                return suggestions; // We have enough from open reels
            }
        }

        // Strategy 3: Accumulation of open reels is not enough, open new reels
        float reste = besoin - accumule;
        for (Bobine b : neuves) {
            suggestions.add(b);
            reste -= b.getLongueurRestante();
            if (reste <= 0) {
                break;
            }
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
