/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cg.tic.refapp;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

/**
 *
 * @author chr
 */
public class ContexteAD {
    
    public String racine_ad;
    public String authentication;
    public String principal;
    public String credential;
    public String ldap_url;
    public String departement_no_pattern;
    
    public ContexteAD(String pRacineAD, String pAuthentication, String pPrincipal, String pCredential, String pLdapUrl, String pDepartementNopattern) throws TicException {
        // pDepartementNopattern permet de filtrer les départements pris en compte
        // On l'utilise pour la Qualité, on met noQualite dans pNopattern pour indiquer qu'on ne prends pas les départements qui contienne noQualite dans destinationIndicator
        
        racine_ad = pRacineAD;
        if (Base.chaine_is_vide(racine_ad)) { 
            // Si le paramètre racine AD n'est pas spécifié, on considère qu'il n'y a pas de lien avec Active directory
            return;  
        }
        
        authentication = pAuthentication;
        principal = pPrincipal;
        credential = pCredential;
        ldap_url = pLdapUrl;
        departement_no_pattern = pDepartementNopattern;
        
    } // end constructor
    
    public LdapContext ldap_context_get() throws TicException {
        // Create a Ldap Connection
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, Spnego.LDAP_CONTEXT_FACTORY);
        env.put(Context.SECURITY_AUTHENTICATION, authentication);
        env.put(Context.SECURITY_PRINCIPAL, principal);
        env.put(Context.SECURITY_CREDENTIALS, credential);
        env.put(Context.PROVIDER_URL, ldap_url);
        env.put(Spnego.LDAP_ATTRIBUTES_BINARY, Spnego.ATTRIBUT_TOKENGROUPS + " " + Spnego.ATTRIBUT_OBJECTSID + " " + Spnego.ATTRIBUT_OBJECTGUID);
        
        try {
            
            //debug.add(Spnego.class.getName() + ".ldap_context_get", "Création du contexte ldap initial");
            //Create the initial directory context
            return new InitialLdapContext(env, null);
        }
        catch (NamingException nmex) {
            throw new TicException("Erreur ldap_context_get: " + nmex.getMessage());
        }
        
    } // end ldap_context_get()
    
    public final String dn_get(LdapContext pLdapContext, String pSamaccountName) throws TicException {
        SearchControls ctrl_recherche;
        String filtre_recherche;
        NamingEnumeration reponse_ne;
        NameClassPair paire_nc;
        
        if (Base.chaine_is_vide(racine_ad)) { throw new TicException("(dn_get) Veuillez configurer Spnego/Active Directory dans le fichier web.xml pour pouvoir l'utiliser"); }
  
        try {

            // Rechercher le userad, il nous faut le distinguishedName
            ctrl_recherche = new SearchControls();
            ctrl_recherche.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctrl_recherche.setReturningAttributes(new String[]{Spnego.ATTRIBUT_DISTINGUISHEDNAME});
            filtre_recherche = "(" + Spnego.ATTRIBUT_SAMACCOUNTNAME + "=" + pSamaccountName + ")";

            reponse_ne = pLdapContext.search(racine_ad, filtre_recherche, ctrl_recherche);

            if (!reponse_ne.hasMore()) { return ""; }
            paire_nc = (NameClassPair)reponse_ne.next();
            
            return paire_nc.getName() + "," + racine_ad;
            
        }
        catch (NamingException nmex) {
            throw new TicException("Erreur SPNEGO: " + nmex.getMessage());
        }
        
    } // end dn_get()
    
} // end class ContexteAD
