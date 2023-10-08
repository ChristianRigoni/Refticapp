/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cg.tic.refapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author chr
 */
public class Sql {

    public final static String SQL_ACTION_INSERT = "insert";
    public final static String SQL_ACTION_UPDATE = "update";
    public final static String SQL_ACTION_DELETE = "delete";
    
    public final static String HTML_DOUBLEQUOTE = "&quot;";
    
    public static String chainesql_proteger(String pChainesql, String pSqlid) {
        String sql_id;
        if (Base.chaine_is_vide(pChainesql)) { return ""; }
        sql_id = Base.chaine_is_vide(pSqlid) ? "t" : pSqlid;
        
        return "select " + sql_id + ".* from (" + pChainesql + ") " + sql_id;
        
    } // end chainesql_proteger
    public static String chainesql_proteger(String pChainesql) {
        return chainesql_proteger(pChainesql, "");
    }
    
    public static String chainesql_reduire_a_vide(String pChainesql) {
        if (Base.chaine_is_vide(pChainesql)) { return ""; }
        return 
                "select * from (" +
                    "select t.* from (" + pChainesql + ") t where 1 = 0" +
                ") t" +
                "";
    }
    
    public static String chaine_2_sqlvalue(String pChaine, boolean pNonNull) {
        if (Base.chaine_is_vide(pChaine)) { 
            if (pNonNull) { return "''"; }
            return "NULL"; 
        }
        //return "N'" + pChaine.replace(Domnode.HTML_DOUBLEQUOTE, "\"").replace("'", "''") + "'";
        return "'" + pChaine.replace(HTML_DOUBLEQUOTE, "\"").replace("'", "''") + "'";
    }
    public static String chaine_2_sqlvalue(String pChaine) {
        return chaine_2_sqlvalue(pChaine, false);
    }
        
    public static String get_dbchaine(String pDbvaleur) {
        if (Base.chaine_is_vide(pDbvaleur)){ return ""; }
        return pDbvaleur;
    }

    public static String get_dbstamp(String pDbvaleur, int pPrecision) {
        if (Base.chaine_is_vide(pDbvaleur)){ return ""; }
        if (pDbvaleur.length() <= pPrecision) { return pDbvaleur; }
        return pDbvaleur.substring(0, pPrecision);
    }

    public static Connection connection_open(String pClassForName, String pSqlConnectString, String pLogin, String pPassword) throws TicException {
        
        // initialiser sql_source si nécessaire
        if (Base.chaine_is_vide(pClassForName) || Base.chaine_is_vide(pSqlConnectString)) { return null; }
        
        try {
            Class.forName(pClassForName);
            if (Base.chaine_is_vide(pLogin)) {
                return DriverManager.getConnection(pSqlConnectString);
            }
            else {
                return DriverManager.getConnection(pSqlConnectString, pLogin, pPassword);
            }
        }
        catch (ClassNotFoundException|SQLException e) {
            throw new TicException("Classe " + pClassForName + " inconnue, erreur: " + e.getMessage());
        }

     } // end connection_open()
    public static Connection connection_open(String pClassForName, String pSqlConnectString) throws TicException {
        return connection_open(pClassForName, pSqlConnectString, "", "");
    }

    public static String champ_get_from_chainesql(Sqlsource pSqlsource, String pChaineSql, boolean pIgnoreSiNoRecord) throws TicException {
        SqlVue myvue;

        myvue = new SqlVue(pSqlsource, pChaineSql);
        
        try {
            if (!myvue.next()) {
                if (pIgnoreSiNoRecord) { return ""; }
                throw new TicException("aucun enregistrement trouvé pour la chaine sql: " + pChaineSql);
            }
            return myvue.getColumnString(1);
        }
        catch (TicException ticex) {
            throw ticex;
        }
        finally {
            myvue.close();
        }
        
    } // end champ_get_from_chainesql()
    public static String champ_get_from_chainesql(Sqlsource pSqlsource, String pChaineSql) throws TicException {
        return champ_get_from_chainesql(pSqlsource, pChaineSql, false);
    }
    
    public static void execute_update(Connection pConnection, String pChaineSql) throws TicException {
        // Pour les opérations insert, update, delete
        Statement stmt;
        
        try {
            stmt = pConnection.createStatement();
            stmt.executeUpdate(pChaineSql);
        }
        catch (SQLException e) { throw new TicException("Erreur execute_update: " + e.getMessage()); }
        
    } // end executer()
    
    public static boolean execute(Connection pConnection, String pChaineSql) throws TicException {
        // Pour les procédures stockées ou autres...
        Statement stmt;
        
        try {
            stmt = pConnection.createStatement();
            return stmt.execute(pChaineSql);
        }
        catch (SQLException e) { throw new TicException("Erreur execute: " + e.getMessage()); }
        
    } // end executer()
    
    public static SqlResultat transaction_executer(Connection pConnection, String[] pTbTrans, boolean pSensInverse) throws TicException {
        int itrans;
        Statement stmt = null;
        boolean autocommit_keep = false;
        SqlResultat resultat = new SqlResultat();

        try {
            //Switch to manual transaction mode by setting autocommit to false. Note that this starts the first manual transaction.
            autocommit_keep = pConnection.getAutoCommit();
            pConnection.setAutoCommit(false);
            stmt = pConnection.createStatement();
            if (pSensInverse) {
                for (itrans=pTbTrans.length - 1; itrans >= 0; itrans--) {
                    resultat.add(stmt.executeUpdate(pTbTrans[itrans]), 1);
                }
            }
            else {
                for (itrans=0; itrans < pTbTrans.length; itrans++) {
                    resultat.add(stmt.executeUpdate(pTbTrans[itrans]), 1);
                }
            }
            pConnection.commit(); //This commits the transaction and starts a new one.
            stmt.close(); //This turns off the transaction.
        }
        catch (SQLException e) { 
          resultat.reset();
          try { pConnection.rollback(); } catch (SQLException e2) { }
          throw new TicException("Erreur transaction_executer: " + e.getMessage());
        }
        finally {
            if (stmt != null) { try { stmt.close(); pConnection.setAutoCommit(autocommit_keep); } catch (SQLException e){} }
        }
        
        return resultat;

    } // end transaction_executer()

    public static boolean is_dbvaleur_vide(String pDbvaleur) {

        if (pDbvaleur == null) { return true; }

        if (pDbvaleur.length() != 4) { return false; }

        return "null".equals(pDbvaleur.toLowerCase());

    } // end is_dbvaleur_vide()

    // Convertir le champ dans une collation sans accent ni majuscule
    // CI: Case Insensitive
    // AI: Accent Insensitive
    public static String collate_ci_ai(String pChamp) {
        if (Base.chaine_is_vide(pChamp)) { return ""; }
        return pChamp + "  COLLATE Latin1_general_CI_AI";
    }
    
} // end class Sql
