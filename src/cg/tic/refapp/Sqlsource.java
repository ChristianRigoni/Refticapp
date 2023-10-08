/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cg.tic.refapp;

import java.sql.Connection;

/**
 *
 * @author chr
 */
public class Sqlsource {
    
    public Connection connexion;
    public String string_op_plus;
    public String string_concat;
    public String stamp_type;
    public String string_type;
    public boolean stamp_java2sql_convertir;
    
    public Sqlsource(String pClassforname, String pConnectstring, String pDatabasenm, String pLogin, String pPassword, String pStringOpplus, String pStamptype, String pStringFctconcat
            , String pStringtype, String pStampJava2sqlConvertir) throws TicException {
        
        if (Base.chaine_is_vide(pDatabasenm)) { return; }

        if (Base.chaine_is_vide(pClassforname) || Base.chaine_is_vide(pConnectstring)) { 
            throw new TicException("Veuillez fournir ClassforName et ConnectString pour la base " + pDatabasenm); 
        }
        
        if (!Base.chaine_is_vide(pStringOpplus) && !Base.chaine_is_vide(pStringFctconcat)) {
            throw new TicException("Vous avez défini à la fois l'opérateur plus (" + pStringOpplus + ") et la fonction concat (" + pStringFctconcat + ") pour les chaines, un seul ne peut être défini");
        }
        
        connexion = Sql.connection_open(pClassforname, pConnectstring, pLogin, pPassword);
        string_op_plus = Base.chaine_is_vide(pStringOpplus) ? "+" : pStringOpplus;
        string_concat = Base.chaine_is_vide(pStringFctconcat) ? "" : pStringFctconcat;
        stamp_type = Base.chaine_is_vide(pStamptype) ? SqlVue.SQLSERVER_STAMP_TYPE : pStamptype;
        string_type = Base.chaine_is_vide(pStringtype) ? SqlVue.SQLSERVER_STRING_TYPE : pStringtype;
        stamp_java2sql_convertir = !Base.chaine_is_vide(pStampJava2sqlConvertir);
        
    } // end Sqlsource
    public Sqlsource(String pClassforname, String pConnectstring, String pDatabasenm) throws TicException {
        this(pClassforname, pConnectstring, pDatabasenm, "", "", "", "", "", "", "");
    } // end Sqlsource
    
    public String stamp_java2sql(String pStamp) {
        if (stamp_java2sql_convertir) {
            return SqlVue.stamp_java2sql(pStamp);
        }
        return pStamp;
    } // end stamp_java2sql
    
    public String champs_concat(String[] pChamps) {
        String chaine;
        int ichamp;
        String virgule;
        
        if (pChamps == null || pChamps.length <= 0) { return "''"; }
        
        if (pChamps.length == 1) {
            return pChamps[0];
        }

        if (!Base.chaine_is_vide(string_concat)) {
            chaine = string_concat + "(";
            virgule = "";
            for (ichamp=0; ichamp < pChamps.length; ichamp++) {
                chaine += virgule + pChamps[ichamp];
                if (ichamp == 0) { virgule = ","; }
            }
            chaine += ")";
        }
        else {
            chaine = "";
            virgule = "";
            for (ichamp=0; ichamp < pChamps.length; ichamp++) {
                chaine += virgule + pChamps[ichamp];
                if (ichamp == 0) { virgule = " " + string_op_plus + " "; }
            }
        }
        
        return chaine;
        
    } // end champs_concat()
    
} // end class Sqlsource
