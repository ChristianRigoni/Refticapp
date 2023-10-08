/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cg.tic.refapp;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author chr
 */
public class Sqltransaction {
    
    public static SqlResultat transaction_executer(Connection pConnection, String[] pTbTrans, boolean pSensInverse) throws TicException {
    //public static int transaction_executer(Connection pConnection, String[] pTbTrans, boolean pSensInverse) throws TicException {
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
    
    public ArrayList<String> tb_trans;
    public ArrayList<String> tb_pj2delete;
    public boolean sens_inverse;
    
    public Sqltransaction(boolean pSensInverse) {
        
        tb_trans = new ArrayList<>();
        tb_pj2delete = new ArrayList<>();
        sens_inverse = pSensInverse;
        
    } // end Sqltransaction()
    public Sqltransaction() {
        this(false);
    }
    
    public boolean add(String pChainesql) {
        return tb_trans.add(pChainesql);
    } // end add()
    
    public boolean add_pj2delete(String pPjPath) {
        return tb_pj2delete.add(pPjPath);
    }
    
    public int size() {
        return tb_trans.size();
    } // end add()
    
    public String get(int pIsql) {
        return tb_trans.get(pIsql);
    }
    
    public String set(int pIsql, String pChainesql) {
        return tb_trans.set(pIsql, pChainesql);
    }
    
    public void effacer() {
        tb_trans.clear();
        tb_pj2delete.clear();
        sens_inverse = false;
    } // end effacer()
    
    public void reunir(Sqltransaction pSqltransaction) {
        int itb;
        
        if (pSqltransaction == null){ return; }
        
        if (pSqltransaction.size() > 0) {
            if (pSqltransaction.sens_inverse) {
                for (itb=pSqltransaction.size()-1; itb >= 0; itb--) {
                    tb_trans.add(pSqltransaction.get(itb));
                }
            }
            else {
                for (itb=0; itb < pSqltransaction.size(); itb++) {
                    tb_trans.add(pSqltransaction.get(itb));
                }
            }
        }
        
        if (pSqltransaction.tb_pj2delete.size() > 0) {
            for (itb=0; itb < pSqltransaction.tb_pj2delete.size(); itb++) {
                tb_pj2delete.add(pSqltransaction.tb_pj2delete.get(itb));
            }
        }
        
    } // end reunir()

    public SqlResultat _executer(Sqlsource pSqlsource) throws TicException {
        SqlResultat resultat = new SqlResultat();

        if (tb_trans.size() <= 0) { return resultat; }

        resultat = transaction_executer(pSqlsource.connexion, tb_trans.toArray(new String[0]), sens_inverse);
        
        if (tb_pj2delete.size() > 0) {
            File file_pj;
            int ipj;
            for (ipj=0; ipj < tb_pj2delete.size(); ipj++) {
                try {
                    file_pj = new File(tb_pj2delete.get(ipj));
                    if (file_pj.exists()) {
                        //pTicServlet.debug.add(Sqltransaction.class.getName() + ".executer", "pj delete: " + file_pj.getName());
                        file_pj.delete();
                    }
                }
                catch (Exception ex) { }
            }
        }
        return resultat;
    }
    
} // end Sqltransaction
