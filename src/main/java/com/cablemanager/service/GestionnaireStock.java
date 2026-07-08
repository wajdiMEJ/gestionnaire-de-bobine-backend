package com.cablemanager.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cablemanager.entity.ActionHistorique;
import com.cablemanager.entity.Bobine;
import com.cablemanager.entity.Couleur;
import com.cablemanager.entity.Employe;
import com.cablemanager.entity.StatutBobine;
import com.cablemanager.entity.TypeAction;
import com.cablemanager.repository.ActionHistoriqueRepository;
import com.cablemanager.repository.BobineRepository;
import com.cablemanager.repository.BobineUtiliseeRepository;
import com.cablemanager.repository.EmployeRepository;

@Service
@Transactional
public class GestionnaireStock {

    private final BobineRepository bobineRepository;
    private final ActionHistoriqueRepository actionHistoriqueRepository;
    private final BobineUtiliseeRepository bobineUtiliseeRepository;
    private final EmployeRepository employeRepository;

    @Autowired
    public GestionnaireStock(BobineRepository bobineRepository,
                              ActionHistoriqueRepository actionHistoriqueRepository,
                              BobineUtiliseeRepository bobineUtiliseeRepository,
                              EmployeRepository employeRepository) {
        this.bobineRepository = bobineRepository;
        this.actionHistoriqueRepository = actionHistoriqueRepository;
        this.bobineUtiliseeRepository = bobineUtiliseeRepository;
        this.employeRepository = employeRepository;
    }

    // ── AJOUTER ──────────────────────────────────────────────
    public Bobine ajouterBobine(Bobine bobine) {
        return ajouterBobine(bobine, null);
    }

    public Bobine ajouterBobine(Bobine bobine, Long employeId) {
        if (bobine.getLongueurRestante() <= 0) {
            bobine.setStatut(StatutBobine.EPUISEE);
        } else {
            bobine.setStatut(StatutBobine.DISPONIBLE);
        }

        Bobine saved = bobineRepository.save(bobine);

        Employe employe = (employeId != null)
            ? employeRepository.findById(employeId).orElse(null)
            : null;

        ActionHistorique action = ActionHistorique.builder()
                .type(TypeAction.NOUVELLE_BOBINE)
                .description("Nouvelle bobine enregistrée. Réf: "
                    + saved.getReference() + ", Lot: " + saved.getLot())
                .date(new Date())
                .metresConsommes(0f)
                .employe(employe)
                .bobine(saved)
                .build();

        actionHistoriqueRepository.save(action);

        // Retourne la bobine avec son ID généré pour le QR code
        return saved;
    }

    // ── SUPPRIMER ─────────────────────────────────────────────
    public void supprimerBobine(Long id) {
        Bobine bobine = bobineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bobine non trouvée avec l'ID: " + id));

        List<ActionHistorique> historiques = actionHistoriqueRepository.findByBobineId(id);
        if (!historiques.isEmpty()) {
            actionHistoriqueRepository.deleteAll(historiques);
        }

        List<com.cablemanager.entity.BobineUtilisee> utilisations = bobineUtiliseeRepository.findByBobineId(id);
        if (!utilisations.isEmpty()) {
            bobineUtiliseeRepository.deleteAll(utilisations);
        }

        bobineRepository.delete(bobine);
    }

    // ── METTRE À JOUR longueurRestante seulement ──────────────
    public void mettreAJour(Long id, float longueurRestante) {
        mettreAJour(id, longueurRestante, null);
    }

    public void mettreAJour(Long id, float longueurRestante, Long employeId) {
        Bobine bobine = bobineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Bobine non trouvée avec l'ID: " + id));

        float ancienneLongueur = bobine.getLongueurRestante();
        bobine.setLongueurRestante(longueurRestante);

        if (longueurRestante <= 0.01f) {
            bobine.setLongueurRestante(0f);
            bobine.setStatut(StatutBobine.EPUISEE);
        } else {
            bobine.setStatut(StatutBobine.DISPONIBLE);
        }

        Bobine updated = bobineRepository.save(bobine);

        Employe employe = (employeId != null)
            ? employeRepository.findById(employeId).orElse(null)
            : null;

        float diff = ancienneLongueur - longueurRestante;
        TypeAction typeAction = (diff > 0)
            ? TypeAction.UTILISATION_RESTE
            : TypeAction.AJOUT_RESTE;

        ActionHistorique action = ActionHistorique.builder()
                .type(typeAction)
                .description(String.format(
                    "Mise à jour manuelle du stock de %.2fm à %.2fm.",
                    ancienneLongueur, longueurRestante))
                .date(new Date())
                .metresConsommes(Math.abs(diff))
                .employe(employe)
                .bobine(updated)
                .build();

        actionHistoriqueRepository.save(action);
    }

    // ── MODIFIER TOUS LES CHAMPS ──────────────────────────────
    public Bobine mettreAJourComplet(Long id, Bobine bobineModifiee, Long employeId) {
        Bobine bobine = bobineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Bobine non trouvée avec l'ID: " + id));

        bobine.setReference(bobineModifiee.getReference());
        bobine.setCouleur(bobineModifiee.getCouleur());
        bobine.setSection(bobineModifiee.getSection());
        bobine.setLongueurInitiale(bobineModifiee.getLongueurInitiale());
        bobine.setLongueurRestante(bobineModifiee.getLongueurRestante());
        bobine.setNumeroCoupe(bobineModifiee.getNumeroCoupe());
        bobine.setLot(bobineModifiee.getLot());
        bobine.setNTouret(bobineModifiee.getNTouret());
        bobine.setDateAjout(bobineModifiee.getDateAjout());

        if (bobineModifiee.getLongueurRestante() <= 0.01f) {
            bobine.setLongueurRestante(0f);
            bobine.setStatut(StatutBobine.EPUISEE);
        } else {
            bobine.setStatut(StatutBobine.DISPONIBLE);
        }

        Bobine updated = bobineRepository.save(bobine);

        Employe employe = (employeId != null)
            ? employeRepository.findById(employeId).orElse(null)
            : null;

        ActionHistorique action = ActionHistorique.builder()
                .type(TypeAction.AJOUT_RESTE)
                .description("Modification complète de la bobine. Réf: "
                    + updated.getReference()
                    + " — Longueur restante: "
                    + updated.getLongueurRestante() + "m")
                .date(new Date())
                .metresConsommes(0f)
                .employe(employe)
                .bobine(updated)
                .build();

        actionHistoriqueRepository.save(action);

        return updated;
    }

    // ── LISTER ────────────────────────────────────────────────
    public List<Bobine> listerTout() {
        return bobineRepository.findAll();
    }

    public List<Bobine> listerParCouleur(Couleur couleur) {
        return bobineRepository.findByCouleur(couleur);
    }

    public List<Bobine> listerParSection(float section) {
        return bobineRepository.findBySection(section);
    }
}