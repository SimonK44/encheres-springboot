package com.example.encheres.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.example.encheres.bll.ArticleVenduService;
import com.example.encheres.bll.CategorieService;
import com.example.encheres.bll.EnchereService;
import com.example.encheres.bll.UtilisateurService;
import com.example.encheres.bo.ArticleVendu;
import com.example.encheres.bo.Categorie;
import com.example.encheres.bo.Enchere;
import com.example.encheres.bo.Utilisateur;
import com.example.encheres.dal.ArticleVenduDynamiqueDAO;

@Controller
@SessionAttributes({"utilisateurSession"})

public class HomeController {
	private ArticleVenduService articleVenduService;
	private UtilisateurService utilisateurService;
	private CategorieService categorieService; 

	
	public HomeController(ArticleVenduService articleVenduService, UtilisateurService utilisateurService,
			CategorieService categorieService) {
		this.articleVenduService = articleVenduService;
		this.utilisateurService = utilisateurService;
		this.categorieService = categorieService;
	}

	@Autowired
    private JdbcTemplate jdbcTemplate;
    @GetMapping("/test-connexion")
    public String testConnexion(Model model) {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            model.addAttribute("message", "Connexion à la base de données réussie");
        } catch (Exception e) {
            model.addAttribute("message", "Échec de la connexion à la base de données");
        }
        return "view-test-connexion";
    }

    @GetMapping({"/","/home","/encheres","/listes-articles"})
    public String home(
    	Model model ) {
    	System.out.println("before");
    	List<ArticleVendu> articles = this.articleVenduService.findAll();
    	System.out.println("a");
        List<Categorie> categories = this.categorieService.findAll();
    	System.out.println("after");

    	System.out.println("HomeControler articles"+ articles);
    	System.out.println("HomeControler categories" + categories);
    	
    	model.addAttribute("articles", articles);
    	model.addAttribute("categories", categories);

    	//dateDebutEnchere=2024-07-01, dateFinEnchere=2024-07-30, prixInitial=100.0, prixVente=150.0, 
    	//categorie=Categorie [noCategorie=1, libelle=null], acheteur=Utilisateur [noUtilisateur=2, pseudo=null, nom=null, prenom=null, email=null, telephone=null, rue=null, codePostal=null, ville=null, motDePasse=null, credit=0, administrateur=false], 
    	//vendeur=Utilisateur [noUtilisateur=1, pseudo=null, nom=null, prenom=null, email=null, telephone=null, rue=null, codePostal=null, ville=null, motDePasse=null, credit=0, administrateur=false], encheres=[]], 
    	
  	return "home";
    }

   //à developer
    
   @PostMapping({"/","home","encheres","listes-articles"})
   
   public String homeRecherche(
    		@RequestParam("filtre")String nomArticle,
    		@RequestParam("categorie") int noCategorie,
    		@RequestParam("transactionType")String transactionType,
    		@RequestParam("encheresOuvertes") int encheresOuvertes,
    		@RequestParam("encheresEnCours") int encheresEnCours,
    		@RequestParam("encheresRemportees") int encheresRemportees,
    		@RequestParam("venteCours") int venteCours,
    		@RequestParam("venteNonDebute") int venteNonDebute,
    		@RequestParam("venteTerminees") int venteTerminees,
    		@ModelAttribute("utilisateurSession") Utilisateur utilisateurSession,
    		Model model) {
	   System.out.println("nomArticle:"+ nomArticle + "noCategorie: " + noCategorie + "transactionType:" + transactionType + "encheresOuvertes: " +encheresOuvertes+"encheresEnCours" +"encheresEnCours"+encheresEnCours+"encheresRemportees: "+ encheresRemportees+ "venteCours: "+venteCours+"venteNonDebute: " +"venteNonDebute :"+ venteNonDebute+"venteTerminees: "+venteTerminees+ "utilisateurSession: "+utilisateurSession);
	   int requete = encheresOuvertes + encheresEnCours + encheresRemportees + venteCours + venteNonDebute + venteTerminees;
    	 
    	
	   //String transactionType, int requete,  String nomArticle, int noCategorie, int noUtilisateurVendeur, int noUtilisateurAcheteur
	   List<ArticleVendu> articlesRecherches = this.articleVenduService.findAllComplexe
   									(transactionType, requete, nomArticle, noCategorie,utilisateurSession.getNoUtilisateur() , utilisateurSession.getNoUtilisateur()); 	 	
    	List<Categorie> categories = this.categorieService.findAll();
    	System.out.println("homeRecherche" + articlesRecherches);
    	
    	model.addAttribute("articlesRecherches", articlesRecherches);
    	model.addAttribute("categories", categories);    	
    		
    	return "home";
	}
}


