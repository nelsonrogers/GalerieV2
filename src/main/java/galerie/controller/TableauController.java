/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package galerie.controller;

import galerie.dao.TableauRepository;
import galerie.entity.Artiste;
import galerie.entity.Tableau;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author nelsonrogers
 */
@Controller
@RequestMapping(path = "/tableau")
public class TableauController {
    
    @Autowired
    private TableauRepository tabDAO;
    
    private HashSet<Artiste> auteurs;
    
    @GetMapping(path = "show")
    public String afficheTousLesTableaux(Model model) {
        // On ajoute la liste des tableaux au modele
        model.addAttribute("tableaux", tabDAO.findAll());
        return "afficheTableaux";
    }
    
    
    @GetMapping(path = "add")
    public String montreLeFormulairePourAjout(@ModelAttribute("tableau") Tableau tableau, Model model) {
        // On récupère les artistes connus et on les met dans un set
        auteurs = new HashSet<>();
        for (Tableau oeuvre : tabDAO.findAll()){
            if (oeuvre.getAuteur() != null)
                auteurs.add(oeuvre.getAuteur());
        }
        // On ajoute l'ensemble de noms d'artistes en tant que paramètre du formulaire
        model.addAttribute("auteurs", auteurs);
        return "formulaireTableau";
    }
    
    
    @PostMapping(path = "save")
    public String ajouteLeTableauPuisMontreLaListe(Tableau tableau, RedirectAttributes redirectInfo) {
        String message;
        try {
            // cf. https://www.baeldung.com/spring-data-crud-repository-save
            tabDAO.save(tableau);
            message = "Le tableau '" + tableau.getTitre() + "' a été correctement enregistré";
        } catch (DataIntegrityViolationException e) {
            // Les titres sont définis comme 'UNIQUE'
            // En cas de doublon, JPA lève une exception de violation de contrainte d'intégrité
            message = "Erreur : Le tableau '" + tableau.getTitre() + "' existe déjà";
        }
        // RedirectAttributes permet de transmettre des informations lors d'une redirection,
        // Ici on transmet un message de succès ou d'erreur
        // Ce message est accessible et affiché dans la vue 'afficheTableau.html'
        redirectInfo.addFlashAttribute("message", message);
        return "redirect:show"; // POST-Redirect-GET : on se redirige vers l'affichage de la liste		
    }
    
    @GetMapping(path = "delete")
    public String supprimeUneCategoriePuisMontreLaListe(@RequestParam("id") Tableau tableau, RedirectAttributes redirectInfo) {
        String message = "Le tableau '" + tableau.getTitre() + "' a bien été supprimé";
        try {
            tabDAO.delete(tableau); // Ici on peut avoir une erreur (Si il y a un auteur pour ce tableau par exemple)
        } catch (DataIntegrityViolationException e) {
            // violation de contrainte d'intégrité si on essaie de supprimer un tableau qui a un auteur
            message = "Erreur : Impossible de supprimer le tableau '" + tableau.getTitre() + "', il faut d'abord supprimer l'artiste";
        }
        // RedirectAttributes permet de transmettre des informations lors d'une redirection,
        // Ici on transmet un message de succès ou d'erreur
        // Ce message est accessible et affiché dans la vue 'afficheTableau.html'
        redirectInfo.addFlashAttribute("message", message);
        return "redirect:show"; // on se redirige vers l'affichage de la liste
    }
}
