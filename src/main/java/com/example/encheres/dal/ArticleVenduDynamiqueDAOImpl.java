
package com.example.encheres.dal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.encheres.bo.ArticleVendu;
import com.example.encheres.bo.Categorie;
import com.example.encheres.bo.Utilisateur;

@Repository
public class ArticleVenduDynamiqueDAOImpl implements ArticleVenduDynamiqueDAO {
	private NamedParameterJdbcTemplate jdbcTemplate;


	private final static String ACHAT                   = "achat";
	private final static String VENTES                  = "ventes";

	private final static String SELECT = "SELECT DISTINCT A.no_article, nom_article, description, date_debut_encheres , date_fin_encheres, prix_initial, prix_vente, no_utilisateur_vendeur, no_utilisateur_acheteur , no_categorie FROM ARTICLES_VENDUS AS A";
// union pour mes encheres en cours
	private final static String UNION  = " LEFT JOIN ENCHERES AS E ON E.no_article = A.no_article" ;

	private final static String WHERE    = " WHERE";
	private final static String OR       = " OR";
	private final static String POURCENT = "%";
	private final static String PARENTHESE_1 = " ( ";
	private final static String PARENTHESE_2 = " ) ";

// where des encheres_ouvertes
	private final static String ENCHERES_OUVERTES       = " ( date_debut_encheres <= GETDATE() AND date_fin_encheres  >= GETDATE() )";
	private final static String MES_ENCHERES_EN_COURS   = " ( date_debut_encheres <= GETDATE() AND date_fin_encheres >= GETDATE() AND E.no_utilisateur = :noUtilisateurAcheteur ) ";
	private final static String MES_ENCHERES_REMPORTEES = " ( date_fin_encheres < GETDATE() AND no_utilisateur_acheteur = :noUtilisateurAcheteur )";

	private final static String MES_VENTES_EN_COURS     = " ( date_debut_encheres <= GETDATE() AND no_utilisateur_vendeur = :noUtilisateurVendeur )";
	private final static String VENTES_NON_DEBUTEES     = " ( date_debut_encheres >= GETDATE() AND no_utilisateur_vendeur = :noUtilisateurVendeur )";
	 private final static String VENTES_TERMINEES       = " ( date_fin_encheres < GETDATE() AND no_utilisateur_vendeur = :noUtilisateurVendeur )";

	private final static String LIKE_NOM                = " AND nom_article like :nomArticle";
    private final static String CATEGORIE               = " AND no_categorie = :noCategorie";

	public ArticleVenduDynamiqueDAOImpl(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}



	@Override
	public List<ArticleVendu> findDynamique(String transactionType,int requete,  String nomArticle, int noCategorie, int noUtilisateurVendeur, int noUtilisateurAcheteur) {
				
		// ecriture de la requete
		String requeteFinale = preparationRequete( transactionType, requete, noCategorie, nomArticle, noUtilisateurVendeur, noUtilisateurAcheteur);

		MapSqlParameterSource mapParameterSource = new MapSqlParameterSource();

		mapParameterSource.addValue("noCategorie",noCategorie);
		mapParameterSource.addValue("nomArticle",POURCENT + nomArticle + POURCENT);
		mapParameterSource.addValue("noUtilisateurVendeur",noUtilisateurVendeur);
		mapParameterSource.addValue("noUtilisateurAcheteur",noUtilisateurAcheteur);
		
		return jdbcTemplate.query(requeteFinale,mapParameterSource,new ArticleVenduRowMapper());


	}

	private String preparationRequete (String transactionType,int requete, int noCategorie, String nomArticle, int noUtilisateurVendeur, int noUtilisateurAcheteur) {
		String requeteFinale = "";

		if (transactionType.equals(ACHAT)) {
			switch (requete) {
// enchere ouverte
			case 1 :
				requeteFinale = SELECT + WHERE + PARENTHESE_1 +  ENCHERES_OUVERTES + PARENTHESE_2 ;
				break;
// mes encheres en cours
			case 2 :
				requeteFinale = SELECT + UNION + WHERE + PARENTHESE_1 + MES_ENCHERES_EN_COURS + PARENTHESE_2;
				break;
//  encheres ouvertes + mes encheres en cours
			case 3 :
				requeteFinale = SELECT + UNION + WHERE +  PARENTHESE_1 + MES_ENCHERES_EN_COURS + OR + ENCHERES_OUVERTES + PARENTHESE_2;
				break;
// mes encheres remportees
			case 4 :
				requeteFinale = SELECT +  WHERE + PARENTHESE_1 + MES_ENCHERES_REMPORTEES + PARENTHESE_2;
				break;
// mes encheres ouvertes + mes encheres remportées
			case 5 :
				requeteFinale = SELECT + WHERE +  PARENTHESE_1 + ENCHERES_OUVERTES + OR + MES_ENCHERES_REMPORTEES + PARENTHESE_2;
				break;
// mes encheres en cours + mes encheres remportées
		    case 6 :
				requeteFinale = SELECT + UNION + WHERE + PARENTHESE_1 + MES_ENCHERES_EN_COURS + OR + MES_ENCHERES_REMPORTEES + PARENTHESE_2;
				break;
// mes enchere ouverte + mes encheres en cours + mes encheres remportées
			case 7 :
				requeteFinale = SELECT + UNION +  WHERE + PARENTHESE_1 + ENCHERES_OUVERTES + OR + MES_ENCHERES_EN_COURS + OR + MES_ENCHERES_REMPORTEES + PARENTHESE_2;
				break;

			}
		} else { if (transactionType.equals(VENTES )) {
			switch (requete) {
// mes ventes en cours
			case 1 :
				requeteFinale = SELECT + WHERE + PARENTHESE_1 + MES_VENTES_EN_COURS + PARENTHESE_2  ;
				break;
// ventes non debutées
			case 2 :
				requeteFinale = SELECT +  WHERE + PARENTHESE_1 + VENTES_NON_DEBUTEES +  PARENTHESE_2 ;
				break;
//  mes ventes en cours + ventes non debutées
			case 3 :
				requeteFinale = SELECT + WHERE + PARENTHESE_1 + MES_VENTES_EN_COURS + OR + VENTES_NON_DEBUTEES +  PARENTHESE_2   ;
				break;
// Ventes terminées
			case 4 :
				requeteFinale = SELECT + WHERE + PARENTHESE_1 + VENTES_TERMINEES + PARENTHESE_2  ;
				break;
// mes ventes en cours + ventes terminées
			case 5 :
				requeteFinale = SELECT + WHERE + PARENTHESE_1 + MES_VENTES_EN_COURS + OR + VENTES_TERMINEES +  PARENTHESE_2 ;
				break;
// ventes non debutées + ventes terminées
			case 6 :
				requeteFinale = SELECT + WHERE + PARENTHESE_1 + VENTES_NON_DEBUTEES + OR + VENTES_TERMINEES + PARENTHESE_2 ;
				break;
// mes ventes en cours + ventes non debutées + ventes terminées
			case 7 :
				requeteFinale = SELECT + WHERE + PARENTHESE_1 + MES_VENTES_EN_COURS + OR + VENTES_NON_DEBUTEES + OR + VENTES_TERMINEES + PARENTHESE_2 ;
				break;
//
			}

			}
		}


// ajout du like sur le nom si besoin
		if ( nomArticle != null  ) {
			requeteFinale = requeteFinale +  LIKE_NOM;
		}
// ajout du filtre sur categorie
		if (noCategorie != 0) {
			requeteFinale = requeteFinale +  CATEGORIE;
		}
		
//		System.out.println("requeteFinale" + requeteFinale );
		return requeteFinale;
	}

	public class ArticleVenduRowMapper implements RowMapper<ArticleVendu> {

		@Override
		public ArticleVendu mapRow(ResultSet rs, int rowNum) throws SQLException {
			ArticleVendu articleVendu = new ArticleVendu();

			articleVendu.setNoArticle(rs.getInt("no_article"));
			articleVendu.setNomArticle(rs.getString("nom_article"));
			articleVendu.setDescription(rs.getString("description"));
			articleVendu.setDateDebutEnchere(rs.getObject("date_debut_encheres", LocalDate.class));
			articleVendu.setDateFinEnchere(rs.getObject("date_fin_encheres", LocalDate.class));
			articleVendu.setPrixInitial(rs.getInt("prix_initial"));
			articleVendu.setPrixVente(rs.getInt("prix_vente"));

			// Map Categorie
			Categorie categorie = new Categorie();
			categorie.setNoCategorie(rs.getInt("no_categorie"));
			articleVendu.setCategorie(categorie);

			// Map Utilisateur Vendeur
			Utilisateur vendeur = new Utilisateur();
			vendeur.setNoUtilisateur(rs.getInt("no_utilisateur_vendeur"));
			articleVendu.setVendeur(vendeur);

			// Map Utilisateur Acheteur (peut être NULL)
			int noAcheteur = rs.getInt("no_utilisateur_acheteur");
			if (!rs.wasNull()) {
				Utilisateur acheteur = new Utilisateur();
				acheteur.setNoUtilisateur(noAcheteur);
				articleVendu.setAcheteur(acheteur);
			}

			return articleVendu;
		}
	}

}

