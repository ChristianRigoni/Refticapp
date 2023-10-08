/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cg.tic.refapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

/**
 *
 * @author chr
 * Spnego est un objet qui représente une entrée Active Directory
 */
public class Spnego {
    
    public static final String LDAP_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    public static final String LDAP_ATTRIBUTES_BINARY = "java.naming.ldap.attributes.binary";
    public static final String ATTRIBUT_SAMACCOUNTNAME = "sAMAccountName";
    public static final String ATTRIBUT_DISTINGUISHEDNAME = "distinguishedName";
    public static final String ATTRIBUT_TOKENGROUPS = "tokenGroups";
    public static final String ATTRIBUT_OBJECTSID = "objectSid";
    public static final String ATTRIBUT_OBJECTCLASS = "objectClass";
    public static final String ATTRIBUT_OBJECTGUID = "objectGUID";
    public static final String ATTRIBUT_ORGANIZATIONALUNIT = "organizationalUnit";
    public static final String ATTRIBUT_MEMBER = "member";
    public static final String ATTRIBUT_MAIL = "mail";
    public static final String ATTRIBUT_USER = "user";
    public static final String ATTRIBUT_NAME = "name";
    public static final String ATTRIBUT_DISPLAYNAME = "displayName";
    public static final String ATTRIBUT_EMPLOYEENUMBER = "employeeNumber";
    public static final String ATTRIBUT_EMPLOYEETYPE = "employeeType";
    public static final String ATTRIBUT_USERACCOUNTCONTROL = "userAccountControl";
    // Utilisé pour stocker le paie_service dans l'OU département
    // NON, on continue de prendre le service dans l'OU: utilisé aussi dans un compte compte pilote qui sert à travailler avec les droits dirdep sur le service
    public static final String ATTRIBUT_STREET = "street";
    // Utilisé pour filtrer les départements
    public static final String ATTRIBUT_DESTINATIONINDICATOR = "destinationIndicator";

    public final static int UAC_ACCOUNTDISABLE = 2;
    public final static int UAC_LOCKOUT = 16;
    public final static int UAC_PASSWORD_EXPIRED = 8388608;
    public final static int UAC_DONT_EXPIRE_PASSWORD = 65536;
    
    public String guid;
    //public String guid_dashed;
    public String nom;
    public String user_dn;
    public String display_name;
    public String email;
    public String employee_number;
    public String employee_type;
    //public String street;
    public ArrayList<String> al_groupes;
    public int user_account_control;
    
    public Spnego(String pGuidDashed, String pNom, String pEmail, String pUserDN, String pDisplayName, String pEmployeeNumber, String pEmployeeType, String pStreet, ArrayList<String> pAlGroupes) {

        guid = pGuidDashed;
        nom = pNom;
        email = pEmail;
        user_dn = pUserDN;
        display_name = pDisplayName;
        employee_number = pEmployeeNumber;
        employee_type = pEmployeeType;
        //street = pStreet;
        al_groupes = pAlGroupes;
        
    } // end constructor
    
    //public final static String[] ATTRIBUTS_SPNEGO = new String[]{ATTRIBUT_OBJECTGUID, ATTRIBUT_NAME, ATTRIBUT_MAIL, ATTRIBUT_DISTINGUISHEDNAME, ATTRIBUT_DISPLAYNAME, ATTRIBUT_EMPLOYEENUMBER, ATTRIBUT_EMPLOYEETYPE, ATTRIBUT_STREET};
    public final static String[] ATTRIBUTS_SPNEGO = new String[]{ATTRIBUT_OBJECTGUID, ATTRIBUT_NAME, ATTRIBUT_MAIL, ATTRIBUT_DISTINGUISHEDNAME, ATTRIBUT_DISPLAYNAME, ATTRIBUT_EMPLOYEENUMBER, ATTRIBUT_EMPLOYEETYPE, ATTRIBUT_USERACCOUNTCONTROL};
    
    public final void initialiser_from_attributs(Attributes pAttributs) throws NamingException {
        
        if (pAttributs != null) {
            guid = attribut_valeur_get(pAttributs, ATTRIBUT_OBJECTGUID, true);
            nom = attribut_valeur_get(pAttributs, ATTRIBUT_NAME);
            email = attribut_valeur_get(pAttributs, ATTRIBUT_MAIL);
            user_dn = attribut_valeur_get(pAttributs, ATTRIBUT_DISTINGUISHEDNAME);
            display_name = attribut_valeur_get(pAttributs, ATTRIBUT_DISPLAYNAME);
            employee_number = attribut_valeur_get(pAttributs, ATTRIBUT_EMPLOYEENUMBER);
            employee_type = attribut_valeur_get(pAttributs, ATTRIBUT_EMPLOYEETYPE);
            user_account_control = Integer.parseInt(attribut_valeur_get(pAttributs, ATTRIBUT_USERACCOUNTCONTROL));
            //street = attribut_valeur_get(pAttributs, ATTRIBUT_STREET);
        }
        else {
            guid = "";
            nom = "";
            email = "";
            user_dn = "";
            display_name = "";
            employee_number = "";
            employee_type = "";
            user_account_control = 0;
            //street = "";
        }
        
    } // end initialiser_from_attributs
    public Spnego(LdapContext pLdapContext, String pRacineAD, Attributes pAttributs) throws NamingException {
        initialiser_from_attributs(pAttributs);
        al_groupes = groupes_get(pLdapContext, pRacineAD, user_dn);
    } // end initialiser_from_attributs
    public Spnego() {
        // Pour pouvoir initialiser le spnego dans le référentiel à partir du fichier de configuration XML
    }
    
    public final boolean is_user_in_group(String pGroupeSamaccountName) throws TicException {
        if (al_groupes == null) { return false; }
        return al_groupes.contains(pGroupeSamaccountName);
    }
    
    public String get_agentcode() {
        if (Base.chaine_is_vide(employee_number) || Base.chaine_is_vide(employee_type)) {
            return "";
        }
        return employee_type + employee_number;
    }
    
    public final static void ldap_context_close(LdapContext pLdapContext) throws TicException {
        try {
            pLdapContext.close();
        }
        catch (NamingException nmex) {
            throw new TicException("Erreur LDAP: " + nmex.getMessage());
        }
    } // end ldap_context_close
    
    public static String attribut_valeur_get(Attributes pAttributs, String pNom, boolean pIsTbByte) throws NamingException {
        Attribute attribut;
        
        attribut = pAttributs.get(pNom);
        if (attribut == null) { return ""; }

        if (pIsTbByte) {
            return guid_2_dashedstring((byte[])attribut.get());
        }
        
        return attribut.get().toString();
        
    } // end attribut_valeur_get
    public static String attribut_valeur_get(Attributes pAttributs, String pNom) throws NamingException {
        return attribut_valeur_get(pAttributs, pNom, false);
    }
    
    public static String attribut_get(Attributes pAttributs, String pAttributNom) throws NamingException {
        Attribute attribut = pAttributs.get(pAttributNom);
        if (attribut == null) { return ""; }
        return (String)attribut.get();
    }
    
    public static byte[] attribut_guid_get(Attributes pAttributs) throws NamingException {
        Attribute attribut = pAttributs.get(ATTRIBUT_OBJECTGUID);
        if (attribut == null) { return null; }
        return (byte[])attribut.get();
    }
    
    public static String sid_bin_2_string(byte[] pSID) {
        //convert the SID into string format
        String chaine_sid; 
        long version;
        long authority;
        long count;
        long rid;
       
        chaine_sid = "S";
        version = pSID[0];
        chaine_sid += "-" + Long.toString(version);
        authority = pSID[4];
        for (int i = 0; i < 4; i++) {
            authority <<= 8;
            authority += pSID[4+i] & 0xFF;
        }
        chaine_sid += "-" + Long.toString(authority);
        count = pSID[2];
        count <<= 8;
        count += pSID[1] & 0xFF;
        for (int j=0; j < count; j++) {
            rid = pSID[11 + (j*4)] & 0xFF;
            for (int k=1;k<4;k++) {
                rid <<= 8;
                rid += pSID[11-k + (j*4)] & 0xFF;
            }
            chaine_sid += "-" + Long.toString(rid);
        }
       
        return chaine_sid;
     
    } // end sid_bin_2_string()
 
    private static String prefixZeros(int value) {
        if (value <= 0xF) {
            StringBuilder sb = new StringBuilder("0");
            sb.append(Integer.toHexString(value));
            return sb.toString();
        } else {
            return Integer.toHexString(value);
        }
    } // end prefixZeros()
    
    public static String guid_get_bindingString(byte[] pObjGUID) {
        StringBuilder sb_guid = new StringBuilder();

        sb_guid.append("<GUID=");
        sb_guid.append(guid_2_dashedstring(pObjGUID));
        sb_guid.append(">");
        return sb_guid.toString();
        
    } // end guid_get_bindingString

    public static String guid_2_dashedstring(byte[] pObjGUID) {
        StringBuilder sb_guid = new StringBuilder();

        sb_guid.append(prefixZeros((int) pObjGUID[3] & 0xFF));
        sb_guid.append(prefixZeros((int) pObjGUID[2] & 0xFF));
        sb_guid.append(prefixZeros((int) pObjGUID[1] & 0xFF));
        sb_guid.append(prefixZeros((int) pObjGUID[0] & 0xFF));
        sb_guid.append("-");
        sb_guid.append(prefixZeros((int) pObjGUID[5] & 0xFF));
        sb_guid.append(prefixZeros((int) pObjGUID[4] & 0xFF));
        sb_guid.append("-");
        sb_guid.append(prefixZeros((int) pObjGUID[7] & 0xFF));
        sb_guid.append(prefixZeros((int) pObjGUID[6] & 0xFF));
        sb_guid.append("-");
        sb_guid.append(prefixZeros((int) pObjGUID[8] & 0xFF));
        sb_guid.append(prefixZeros((int) pObjGUID[9] & 0xFF));
        sb_guid.append("-");
        sb_guid.append(prefixZeros((int) pObjGUID[10] & 0xFF));
        sb_guid.append(prefixZeros((int) pObjGUID[11] & 0xFF));
        sb_guid.append(prefixZeros((int) pObjGUID[12] & 0xFF));
        sb_guid.append(prefixZeros((int) pObjGUID[13] & 0xFF));
        sb_guid.append(prefixZeros((int) pObjGUID[14] & 0xFF));
        sb_guid.append(prefixZeros((int) pObjGUID[15] & 0xFF));

        return sb_guid.toString();

    } // end guid_2_dashedstring
    
    public final static ArrayList<String> groupes_get(LdapContext pLdapContext, String pRacineAD, String pUserDN) throws NamingException {
        SearchControls ctrl_recherche;
        String filtre_recherche;
        NamingEnumeration reponse_ne;
        StringBuilder sb_filtre_groupes;
        SearchResult resultat_recherche;
        Attributes attributs;
        Attribute attribut;
        NamingEnumeration naming_enum;
        NamingEnumeration naming_enum_2;
        byte[] sid;
        SearchControls ctrl_recherche_groupes;
        ArrayList<String> my_al_groupes;
        
        //if (Base.chaine_is_vide(pRacineAD)) { throw new TicException("(groupes_get) Veuillez configurer Spnego/Active Directory pour pouvoir l'utiliser"); }
        if (Base.chaine_is_vide(pRacineAD)) { return null; }
 
        if (Base.chaine_is_vide(pUserDN)) { return null; }
        
        my_al_groupes = new ArrayList<>();
  
        //try {
            
            //Create the search controls   
            ctrl_recherche = new SearchControls();
            ctrl_recherche.setSearchScope(SearchControls.OBJECT_SCOPE);
            ctrl_recherche.setReturningAttributes(new String[]{ATTRIBUT_TOKENGROUPS});
            filtre_recherche = "(" + ATTRIBUT_OBJECTCLASS + "=" + ATTRIBUT_USER + ")";

            //Search for objects using the filter
            reponse_ne = pLdapContext.search(pUserDN, filtre_recherche, ctrl_recherche);
            
            //paceholder for an LDAP filter that will store SIDs of the groups the user belongs to
            sb_filtre_groupes = new StringBuilder();
            sb_filtre_groupes.append("(|");

            //Loop through the search results
            while (reponse_ne.hasMoreElements()) {
                resultat_recherche = (SearchResult)reponse_ne.next();
                attributs = resultat_recherche.getAttributes();
                if (attributs != null) {
                    for (naming_enum = attributs.getAll(); naming_enum.hasMore();) {
                        attribut = (Attribute)naming_enum.next();
                        for (naming_enum_2 = attribut.getAll(); naming_enum_2.hasMore();) {
                            sid = (byte[])naming_enum_2.next();
                            sb_filtre_groupes.append("(" + ATTRIBUT_OBJECTSID + "=").append(sid_bin_2_string(sid)).append(")");
                            //debug.add(Spnego.class.getName() + ".spnego_groupes_get", "loop userAnswer, sid: " + sid_bin_2_string(sid));
                        }
                    }
                }
            } // end while()
            sb_filtre_groupes.append(")");
            
            //debug.add(Spnego.class.getName() + ".spnego_groupes_get", "groupsSearchFilter: " + groupsSearchFilter.toString());
            
            //debug.add(Spnego.class.getName() + ".spnego_groupes_get", "Création controles de recherche des groupes");
                        
            // Search for groups the user belongs to in order to get their names 
            ctrl_recherche_groupes = new SearchControls();
            ctrl_recherche_groupes.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctrl_recherche_groupes.setReturningAttributes(new String[]{ATTRIBUT_SAMACCOUNTNAME});
            
            //debug.add(Spnego.class.getName() + ".spnego_groupes_get", "Lancement recherche groupes");

            //Search for objects using the filter
            reponse_ne = pLdapContext.search(pRacineAD, sb_filtre_groupes.toString(), ctrl_recherche_groupes);
                        
            //Loop through the search results
            while (reponse_ne.hasMoreElements()) {
                resultat_recherche = (SearchResult)reponse_ne.next();
                attributs = resultat_recherche.getAttributes();
                if (attributs != null) {
                    my_al_groupes.add(attributs.get(ATTRIBUT_SAMACCOUNTNAME).get().toString());
                    //tic_servlet.debug.add(Spnego.class.getName() + ".spnego_groupes_get", "groupe: " + attributs.get(ATTRIBUT_SAMACCOUNTNAME).get().toString());
                }
            }
            
            return my_al_groupes;
            
        //}
        //catch (NamingException nmex) {
        //    throw new TicException("Erreur SPNEGO: " + nmex.getMessage());
        //}
        
    } // end groupes_get()
    
    
    public static HashMap<String, Spnego> spnegos_get(ContexteAD pContexteAD) throws TicException {
        HashMap<String, Spnego> hm_spnegos;
        SearchControls ctrl_recherche;
        String filtre_recherche;
        NamingEnumeration reponse_ne;
        SearchResult resultat_recherche;
        Attributes attributs;
        LdapContext ldap_context;
        Spnego spnego;
        
        //ldap_context = ldap_context_get("simple", "glassfish", "1val1YAN", "ldap://vmsrvad.domchr.local:389");
        ldap_context = pContexteAD.ldap_context_get();

        try {
            
            hm_spnegos = new HashMap<>();
            
            ctrl_recherche = new SearchControls();
            ctrl_recherche.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctrl_recherche.setReturningAttributes(ATTRIBUTS_SPNEGO);
            filtre_recherche = "(" + ATTRIBUT_OBJECTCLASS + "=" + ATTRIBUT_USER + ")";

            reponse_ne = ldap_context.search(pContexteAD.racine_ad, filtre_recherche, ctrl_recherche);

            while (reponse_ne.hasMore()) {
                resultat_recherche = (SearchResult)reponse_ne.next();
                attributs = resultat_recherche.getAttributes();
                if (attributs != null) {
                    spnego = new Spnego(ldap_context, pContexteAD.racine_ad, attributs);
                    hm_spnegos.put(spnego.guid, spnego);
                }
            }
        }
        catch (NamingException nmex) {
            throw new TicException("Erreur LDAP: " + nmex.getMessage());
        }
        
        ldap_context_close(ldap_context);
        
        return hm_spnegos;
        
    } // end spnegos_get()
    
    public static void spnegos_dump(HashMap<String, Spnego> pHmSpnegos) throws TicException {
        Spnego spnego;
        
        if (pHmSpnegos == null) {
            return;
        }
        
        for (Map.Entry<String, Spnego> entrySet : pHmSpnegos.entrySet()) {
            spnego = entrySet.getValue();
            if (spnego != null) {
                if (spnego.al_groupes != null) {
                    Iterator<String> myiterator = spnego.al_groupes.iterator();
                    String chainegrp = "";
                    String virgule = "";
                    while (myiterator.hasNext()) {
                        chainegrp += virgule + myiterator.next();
                        if (virgule.length() <= 0) { virgule = ", "; }
                    }
                    System.out.println(Spnego.class.getName() + ".spnegos_dump, spnego(" + spnego.nom + ").groupes: " + chainegrp);
                }
            }
        }
        
    } // end spnegos_dump()
    
    public static Spnego get_spnego(HashMap<String, Spnego> pHmSpnegos, String pAgentcode) {
        if (pHmSpnegos == null) { return null; }
        if (pHmSpnegos.containsKey(pAgentcode)) {
            return pHmSpnegos.get(pAgentcode);
        }
        return null;
    }

    public static String get_samaccountname(LdapContext pLdapContext, String pRacineAD, String pDN) throws TicException {
        SearchControls ctrl_recherche;
        SearchResult resultat;
        Attributes attributs;
        Attribute attribut;
        NamingEnumeration naming_enum; 
        
        if (Base.chaine_is_vide(pRacineAD)) { throw new TicException("(get_samaccountname) Veuillez configurer Spnego/Active Directory dans le fichier web.xml pour pouvoir l'utiliser"); }
  
        try {
            ctrl_recherche = new SearchControls();
            ctrl_recherche.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctrl_recherche.setReturningAttributes(new String[]{ATTRIBUT_SAMACCOUNTNAME});
            String filtre_recherche = "(" + ATTRIBUT_DISTINGUISHEDNAME + "=" + pDN + ")";

            NamingEnumeration reponse = pLdapContext.search(pRacineAD, filtre_recherche, ctrl_recherche);
            
            if (!reponse.hasMore()) { return ""; }
            resultat = (SearchResult)reponse.next();
            attributs = resultat.getAttributes();
            if (attributs == null) { return ""; }
            naming_enum = attributs.getAll(); 
            if (!naming_enum.hasMore()) { return ""; }
            attribut = (Attribute)naming_enum.next();
            naming_enum = attribut.getAll(); 
            if (!naming_enum.hasMore()) { return ""; }

            return (String)naming_enum.next();
        }
        catch (NamingException nmex) {
            throw new TicException("Erreur LDAP: " + nmex.getMessage());
        }
        
    } // end get_samaccountname()
    
    public static void attributs_dump(LdapContext pLdapContext, String pRacineAD, String pSamaccountName) throws TicException {
        SearchControls ctrl_recherche;
        String filtre_recherche;
        NamingEnumeration reponse_ne;
        SearchResult resultat;
        Attributes attributs;
        Attribute attribut;
        NamingEnumeration naming_enum;
        
        if (Base.chaine_is_vide(pRacineAD)) { throw new TicException("(attributs_dump) Veuillez configurer Spnego/Active Directory dans le fichier web.xml pour pouvoir l'utiliser"); }
  
        try {
            
            ctrl_recherche = new SearchControls();
            filtre_recherche = "(" + ATTRIBUT_SAMACCOUNTNAME + "=" + pSamaccountName + ")";

            reponse_ne = pLdapContext.search(pRacineAD, filtre_recherche, ctrl_recherche);

            while (reponse_ne.hasMore()) {
                resultat = (SearchResult)reponse_ne.next();
                attributs = resultat.getAttributes();
                if (attributs != null) {
                    for (naming_enum = attributs.getAll(); naming_enum.hasMore();) {
                        attribut = (Attribute)naming_enum.next();
                        System.out.println(Spnego.class.getName() + ".attributs_dump, attribut.id: " + attribut.getID() + ", attribut.size: " + attribut.size() + ", attribut.toString: " + attribut.toString());
                        /*
                        for (NamingEnumeration ne = attr.getAll(); ne.hasMore();) {
                            String membre = (String)ne.next();
                            al_membres.add(membre);
                            String samaccount = get_samaccountname(pLdapContext, pRacineAd, membre);
                            debug.add(Spnego.class.getName() + ".attributs_dump", "membre: " + membre + ", samaccount: " + samaccount);
                            String[] tb_membres = membres_get(pLdapContext, pRacineAd, samaccount);
                            if (tb_membres != null) {
                                for (String mymembre : tb_membres) {
                                    al_membres.add(mymembre);
                                }
                            }
                        }
                        */
                    }
                }
            }
            
        }
        catch (NamingException nmex) {
            throw new TicException("Erreur LDAP: " + nmex.getMessage());
        }
        
    } // end attributs_dump
    
    public static String[] membres_get(LdapContext pLdapContext, String pRacineAD, String pGroupeSamaccountName) throws TicException {
        ArrayList<String> al_membres = new ArrayList<>();
        NamingEnumeration reponse_ne;
        String filtre_recherche;
        SearchResult resultat;
        Attributes attributs;
        Attribute attribut;
        NamingEnumeration naming_enum;
        NamingEnumeration naming_enum_2;
        String[] tb_membres;
        
        if (Base.chaine_is_vide(pRacineAD)) { throw new TicException("(membres_get) Veuillez fournir la racine Active Directory"); }
  
        try {
            
            SearchControls ctrl_recherche = new SearchControls();
            ctrl_recherche.setReturningAttributes(new String[]{ATTRIBUT_MEMBER});
            filtre_recherche = "(" + ATTRIBUT_SAMACCOUNTNAME + "=" + pGroupeSamaccountName + ")";

            reponse_ne = pLdapContext.search(pRacineAD, filtre_recherche, ctrl_recherche);

            while (reponse_ne.hasMore()) {
                resultat = (SearchResult)reponse_ne.next();
                attributs = resultat.getAttributes();
                if (attributs != null) {
                    for (naming_enum = attributs.getAll(); naming_enum.hasMore();) {
                        attribut = (Attribute)naming_enum.next();
                        for (naming_enum_2 = attribut.getAll(); naming_enum_2.hasMore();) {
                            String membre = (String)naming_enum_2.next();
                            al_membres.add(membre);
                            String samaccount_name = get_samaccountname(pLdapContext, pRacineAD, membre);
                            //debug.add(Spnego.class.getName() + ".membres_get", "membre: " + membre + ", samaccount: " + samaccount);
                            tb_membres = membres_get(pLdapContext, pRacineAD, samaccount_name);
                            if (tb_membres != null) {
                                al_membres.addAll(Arrays.asList(tb_membres));
                            }
                        }
                    }
                }
            }
            
            return al_membres.toArray(new String[al_membres.size()]);
        }
        catch (NamingException nmex) {
            throw new TicException("Erreur LDAP: " + nmex.getMessage());
        }
        
    } // end membres_get
    
    public static void entry_dump_from_dn(LdapContext pLdapContext, String pRacineAD, String pDN) throws TicException {
        SearchControls ctrl_recherche;
        String filtre_recherche;
        NamingEnumeration reponse_ne;
        SearchResult resultat;
        Attributes attributs;
        Attribute attribut;
        NamingEnumeration naming_enum;
        
        if (Base.chaine_is_vide(pRacineAD)) { throw new TicException("(entry_dump_from_dn) Veuillez fournir la racine Active Directory"); }
  
        try {
            
            ctrl_recherche = new SearchControls();
            ctrl_recherche.setSearchScope(SearchControls.SUBTREE_SCOPE);
            filtre_recherche = "(" + ATTRIBUT_DISTINGUISHEDNAME + "=" + pDN + ")";

            reponse_ne = pLdapContext.search(pRacineAD, filtre_recherche, ctrl_recherche);

            while (reponse_ne.hasMore()) {
                resultat = (SearchResult)reponse_ne.next();
                attributs = resultat.getAttributes();
                if (attributs == null) { return; }
                naming_enum = attributs.getAll(); 
                while (naming_enum.hasMore()) {
                    attribut = (Attribute)naming_enum.next();
                    System.out.println(Spnego.class.getName() + ".dn_dump, attribut.id: " + attribut.getID() + ", attribut.size: " + attribut.size() + ", attribut.toString: " + attribut.toString());
                }
            }
        }
        catch (NamingException nmex) {
            throw new TicException("Erreur LDAP: " + nmex.getMessage());
        }
        
    } // end entry_dump_from_dn

    public static String guid_get(LdapContext pLdapContext, String pRacineAD, String pDN) throws TicException {
        SearchControls ctrl_recherche;
        String filtre_recherche;
        NamingEnumeration reponse_ne;
        SearchResult resultat;
        
        if (Base.chaine_is_vide(pRacineAD)) { throw new TicException("(guid_get) Veuillez fournir la racine Active Directory"); }
  
        try {
            
            // Rechercher les users du groupe chef_service
            ctrl_recherche = new SearchControls();
            ctrl_recherche.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctrl_recherche.setReturningAttributes(new String[]{ATTRIBUT_OBJECTGUID});
            filtre_recherche = "(" + ATTRIBUT_DISTINGUISHEDNAME + "=" + pDN + ")";

            reponse_ne = pLdapContext.search(pRacineAD, filtre_recherche, ctrl_recherche);

            if (!reponse_ne.hasMore()) { return ""; }
            resultat = (SearchResult)reponse_ne.next();
            return guid_2_dashedstring((byte[]) resultat.getAttributes().get(ATTRIBUT_OBJECTGUID).get());

        }
        catch (NamingException nmex) {
            throw new TicException("Erreur LDAP: " + nmex.getMessage());
        }
        
    } // end guid_get()

    public static void entry_dump_guid(LdapContext pLdapContext, String pRacineAD, String pDN) throws TicException {
        SearchControls ctrl_recherche;
        String filtre_recherche;
        NamingEnumeration reponse_ne;
        SearchResult resultat;
        
        if (Base.chaine_is_vide(pRacineAD)) { throw new TicException("(entry_dump_guid) Veuillez configurer Spnego/Active Directory dans le fichier web.xml pour pouvoir l'utiliser"); }
  
        try {
            
            // Rechercher les users du groupe chef_service
            ctrl_recherche = new SearchControls();
            ctrl_recherche.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctrl_recherche.setReturningAttributes(new String[]{ATTRIBUT_OBJECTGUID});
            filtre_recherche = "(" + ATTRIBUT_DISTINGUISHEDNAME + "=" + pDN + ")";

            reponse_ne = pLdapContext.search(pRacineAD, filtre_recherche, ctrl_recherche);

            if (!reponse_ne.hasMore()) { return; }
            resultat = (SearchResult)reponse_ne.next();
            byte[] myguid = (byte[]) resultat.getAttributes().get(ATTRIBUT_OBJECTGUID).get();
            System.out.println(Spnego.class.getName() + ".entry_dump_guid, guid.length: " + myguid.length);
            System.out.println(Spnego.class.getName() + ".entry_dump_guid, dashed guid: " + guid_2_dashedstring(myguid));
        }
        catch (NamingException nmex) {
            throw new TicException("Erreur LDAP: " + nmex.getMessage());
        }
        
    } // end entry_dump_guid()
    
} // end class Spnego
