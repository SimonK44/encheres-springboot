package com.example.encheres.controllers;

import com.example.encheres.bll.UtilisateurService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import com.example.encheres.bo.Utilisateur;
import com.example.encheres.exception.BusinessException;

import jakarta.validation.Valid;


@Controller
@SessionAttributes({"utilisateurSession"})
@RequestMapping("/utilisateurs")
public class UtilisateurControlleur {


	UtilisateurService utilisateurService;

	public UtilisateurControlleur (UtilisateurService utilisateurService) {
		this.utilisateurService = utilisateurService;
	}

	//Méthode permettant d'afficher le formulaire de creation d'un utilisateur
	@GetMapping("/creer")
	public String creerUtilisateur(Model model) {
		//Etape 1 : Création d'une instance d'utilisateur
		Utilisateur utilisateur = new Utilisateur();

		//Etape 2 : Placer l'utilisateur dans le modèle
		model.addAttribute("utilisateur", utilisateur);

		return "view-profil-creation";
	}

	//Affichage de la page modification de profil
	@GetMapping("/modifier")
	public String modifierUtilisateurParId(@RequestParam(name="noUtilisateur") int id, Model model,
			@ModelAttribute("utilisateur") Utilisateur utilisateur) {

		Utilisateur u = this.utilisateurService.lectureUtilisateur(id);
		model.addAttribute("utilisateur", u); // 1 objet utilisateur avec tous ses paramètres

		return "view-profil-modification";

	}

	//Afficher une page de profil simple
	@GetMapping("/afficher")
	public String afficherUtilisateurParId(
			Model model,
			@SessionAttribute(value = "utilisateurSession", required = false) Utilisateur utilisateurSession,
			@RequestParam(value = "id", required = false) Integer noUtilisateur // Utilisez Integer au lieu de int
	) {

		System.out.println("\n \n");
		System.out.println("utilisateurSession"+utilisateurSession);
		System.out.println("noUtilisateur"+noUtilisateur);
		// Si aucun utilisateur en session et aucun ID fourni, redirigez vers la page de connexion
		if (utilisateurSession == null && noUtilisateur == null) {
			return "redirect:/login";
		}

		// Si aucun utilisateur en session mais un ID est fourni
		if (utilisateurSession == null) {
			Utilisateur utilisateur = this.utilisateurService.lectureUtilisateur(noUtilisateur);
			model.addAttribute("utilisateur", utilisateur);
			model.addAttribute("isDifferentUser", true); // Ajoutez cette variable
			return "view-utilisateur";
		}

		// Si un utilisateur est en session
		Utilisateur utilisateur;
		boolean isDifferentUser = false;
		if (noUtilisateur == null || noUtilisateur.equals(utilisateurSession.getNoUtilisateur())) {
			utilisateur = this.utilisateurService.lectureUtilisateur(utilisateurSession.getNoUtilisateur());
		} else {
			utilisateur = this.utilisateurService.lectureUtilisateur(noUtilisateur);
			isDifferentUser = true; // L'utilisateur demandé est différent de l'utilisateur en session
		}

		if (utilisateur != null) {
			model.addAttribute("utilisateur", utilisateur); // 1 objet utilisateur avec tous ses paramètres
		}
		model.addAttribute("isDifferentUser", isDifferentUser); // Ajoutez cette variable au modèle

		return "view-utilisateur";
	}


	@PostMapping("/modifier")
	public String modifUtilisateurParId(@Valid @ModelAttribute("utilisateur") Utilisateur utilisateur, BindingResult bindingResult) {

		try {
			this.utilisateurService.modifierUtilisateur(utilisateur);
			System.out.println("Utilisateur = "+utilisateur);
			return "redirect:/home";
		} catch (BusinessException e) {
			e.getErreurs().forEach(err -> {
				ObjectError error = new ObjectError("globalError", err);
				bindingResult.addError(error);
				System.err.println(error);
				}
			);
			return "view-profil-modification";
		}
	}

	//Création d'un utilisateur
	@PostMapping("/creer")
	public String creerUtilisateur(@Valid @ModelAttribute("utilisateur") Utilisateur utilisateur, BindingResult bindingResult) {
		Utilisateur u = new Utilisateur();

		if (bindingResult.hasErrors()) {
			return "view-profil-creation";
		} else {
			System.out.println("Création de l'utilisateur = " + utilisateur);
			//Appel du service en charge de la création de l'utilisateur
			try {
				this.utilisateurService.creerUtilisateur(utilisateur);
				return "redirect:/home";
			} catch (BusinessException e) {
				e.getErreurs().forEach(err -> {
					ObjectError error = new ObjectError("globalError", err);
					bindingResult.addError(error);
					}
				);
				return "view-profil-creation";
			}
		}
	}

}
