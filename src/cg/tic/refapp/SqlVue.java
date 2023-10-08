/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cg.tic.refapp;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Administrateur
 */
public class SqlVue {
    
    public final static String SQLSERVER_STAMP_TYPE = "datetime";
    public final static String SQLSERVER_STRING_TYPE = "nvarchar";
    
    static int last_id = 1;
    
    Statement stmt;
    ResultSet rs;
    ResultSetMetaData rsmd;
    int irs;
    boolean deja_next;
    boolean first_en_cours;
    int id;
    public String stamp_type;
    public String string_type;
    
    public SqlVue(Sqlsource pSqlsource, String pChaineSql, boolean pWithMetaData) throws TicException {
        
        if (pSqlsource == null) {
            throw new TicException("La sql source n'a pas été initialisée");
        }
        
        rsmd = null;
        irs = 0;
        deja_next = false;
        first_en_cours = false;
        id = last_id++;
        stamp_type = Base.chaine_is_vide(pSqlsource.stamp_type) ? SQLSERVER_STAMP_TYPE : pSqlsource.stamp_type;
        string_type = Base.chaine_is_vide(pSqlsource.string_type) ? SQLSERVER_STRING_TYPE : pSqlsource.string_type;
        
        try {
            stmt = pSqlsource.connexion.createStatement();
            rs = stmt.executeQuery(pChaineSql);
            if (pWithMetaData) {
                rsmd = rs.getMetaData();
            }
        }
        catch (SQLException e) { throw new TicException("Erreur new SqlVue: " + e.getMessage()); }
        
        //pTicServlet.debug.add(SqlVue.class.getName() + ".constructor", "id: " + id + ", chainesql: " + pChaineSql);
        
    } // end constructor()
    
    public SqlVue(Sqlsource pSqlsource, String pChaineSql) throws TicException {
        this(pSqlsource, pChaineSql, false);
    } // end constructor()
    
    public boolean isAfterLast() throws TicException {

        try {
            return rs.isAfterLast();
        }
        catch (SQLException e) { 
            throw new TicException("Erreur isAfterLast: " + e.getMessage()); 
        }

    } // end isAfterLast()
    
    public boolean isLast() throws TicException {

        try {
            return rs.isLast();
        }
        catch (SQLException e) { 
            throw new TicException("Erreur isLast: " + e.getMessage()); 
        }

    } // end isLast()
    
    public boolean isVide() throws TicException {
        
        if (!deja_next) {
            throw new TicException("Veuillez ouvrir la vue pour savoir si elle est vide (par first() ou next())");
        }
        
        return (irs <= 0);
        
    } // end isVide()
    
    public int get_irs() throws TicException {
        
        if (!deja_next) {
            throw new TicException("Veuillez ouvrir la vue (par first() ou next() pour obtenir le n° de record courant)");
        }
        
        return irs;
        
    } // end get_irs()
    
    public int get_nb_records() throws TicException {

        try {
            if (rs.isLast() || rs.isAfterLast()) {
                return irs;
            }
        }
        catch (SQLException e) { 
            throw new TicException("Erreur isLast/isAfterLast: " + e.getMessage()); 
        }
        
        throw new TicException("tous les records de la vue n'ont pas été lus");

    } // end get_nb_records()
    
    public int getColumnCount() throws TicException {
        int nb_cols = 0;

        try {
            if (rsmd == null) { rsmd = rs.getMetaData(); }
            nb_cols = rsmd.getColumnCount();
        }
        catch (SQLException e) { throw new TicException("Erreur getColumnCount: " + e.getMessage()); }

        return nb_cols;

    } // end getColumnCount()
    
    public String getColumnName(int pCol) throws TicException {
        String col_nm = "";

        try {
            if (rsmd == null) { rsmd = rs.getMetaData(); }
            col_nm = rsmd.getColumnName(pCol);
        }
        catch (SQLException e) { throw new TicException("Erreur getColumnName: " + e.getMessage()); }

        return col_nm;
        
    } // end getColumnName()
    
    public String getColumnTypeName(int pCol) throws TicException {
        String type_nm = "";

        try {
            if (rsmd == null) { rsmd = rs.getMetaData(); }
            type_nm = rsmd.getColumnTypeName(pCol);
        }
        catch (SQLException e) { throw new TicException("Erreur getColumnTypeName: " + e.getMessage()); }

        if (Base.chaine_is_vide(type_nm)) {
            throw new TicException("Erreur getColumnTypeName colonne " + pCol + " inconnue");
        }

        return type_nm;
        
    } // end getColumnTypeName()
    
    public String getColumnTypeName(String pColNm) throws TicException {
        String type_nm = "";
        int col;
        
        if (rs == null) {
            throw new TicException("le resultset est null, peut-être a t'il été fermé");
        }

        try {
            if (rsmd == null) { rsmd = rs.getMetaData(); }
            for (col = 1; col <= rsmd.getColumnCount(); col++) {
                if (pColNm.equals(rsmd.getColumnName(col))) {
                    type_nm = rsmd.getColumnTypeName(col);
                    break;
                }
            }
        }
        catch (SQLException e) { throw new TicException("Erreur getColumnTypeName: " + e.getMessage()); }

        if (Base.chaine_is_vide(type_nm)) {
            throw new TicException("Erreur getColumnTypeName colonne " + pColNm + " inconnue");
        }
        
        return type_nm;
        
    } // end getColumnTypeName()
    
    public boolean haveColumnName(String pColNm) throws TicException {
        int col;
        
        if (rs == null) {
            throw new TicException("le resultset est null, peut-être a t'il été fermé");
        }

        try {
            if (rsmd == null) { rsmd = rs.getMetaData(); }
            for (col = 1; col <= rsmd.getColumnCount(); col++) {
                if (pColNm.equals(rsmd.getColumnName(col))) {
                    return true;
                }
            }
        }
        catch (SQLException e) { throw new TicException("Erreur haveColumnName: " + e.getMessage()); }

        return false;
        
    } // end haveColumnName()
    
    public String getColumnString(int pCol) throws TicException {
        String col_value = "";

        try {
            if (getColumnTypeName(pCol).equals(stamp_type)) {
                col_value = datetime_sql2java(rs.getString(pCol));
            }
            else {
                col_value = rs.getString(pCol);
            }
        }
        catch (SQLException e) { throw new TicException("Erreur getColumnString: " + e.getMessage()); }
        
        return col_value;
        
    } // end getColumnString()
    
    public String getColumnString(int pCol, int pLg) throws TicException {
        String chaine = getColumnString(pCol);
        if (Base.chaine_is_vide(chaine) || chaine.length() <= pLg || pLg <= 0){ return chaine; }
        return chaine.substring(0, pLg);
    }
    
    public String getColumnString(String pColNm) throws TicException {
        String col_value;

        try {
            if (getColumnTypeName(pColNm).equals(stamp_type)) {
                col_value = datetime_sql2java(rs.getString(pColNm));
            }
            else {
                col_value = rs.getString(pColNm);
            }
        }
        catch (SQLException e) { throw new TicException("Erreur getColumnString: " + e.getMessage()); }

        return col_value;
        
    } // end getColumnString()
    
    public String getColumnDateisoformater(String pColNm, String pFormat) throws TicException {
        TicCalendar tic_cal;

        try {
            tic_cal = new TicCalendar(dateiso_sql2java(rs.getString(pColNm)));
        }
        catch (SQLException e) { throw new TicException("Erreur getColumnDateisoformater: " + e.getMessage()); }

        return tic_cal.formater(pFormat);
        
    } // end getColumnString()
    
    public String getColumnDateformater(String pColNm, String pFormat) throws TicException {
        TicCalendar tic_cal;

        try {
            tic_cal = new TicCalendar(rs.getString(pColNm));
        }
        catch (SQLException e) { throw new TicException("Erreur getColumnDateformater: " + e.getMessage()); }

        return tic_cal.formater(pFormat);
        
    } // end getColumnString()
    
    public String getColumnDateiso(String pColNm) throws TicException {
        String col_value;

        try {
            col_value = dateiso_sql2java(rs.getString(pColNm));
        }
        catch (SQLException e) { throw new TicException("Erreur getColumnString: " + e.getMessage()); }

        return col_value;
        
    } // end getColumnDateiso()
    
    public String getColumnString(String pColNm, int pLg) throws TicException {
        String chaine = getColumnString(pColNm);
        if (Base.chaine_is_vide(chaine) || chaine.length() <= pLg || pLg <= 0){ return chaine; }
        return chaine.substring(0, pLg);
    }
    
    public String getColumnStringNonull(String pColNm, int pLg) throws TicException {
        String chaine;
        
        chaine = getColumnString(pColNm);
        if (Base.chaine_is_vide(chaine)) { return ""; }
        if (pLg <= 0 || chaine.length() <= pLg){ return chaine; }
        return chaine.substring(0, pLg);
        
    } // end getColumnStringNonull()
    public String getColumnStringNonull(String pColNm) throws TicException {
        return getColumnStringNonull(pColNm, -1);
    }
    
    public long getColumnLong(int pCol) throws TicException {
        long col_value = -1;

        try {
            col_value = rs.getLong(pCol);
        }
        catch (SQLException e) { throw new TicException("Erreur getColumnLong: " + e.getMessage()); }

        return col_value;
        
    } // end getColumnLong()
    
    public long getColumnLong(String pCol) throws TicException {
        long col_value = -1;

        try {
            col_value = rs.getLong(pCol);
        }
        catch (SQLException e) { throw new TicException("Erreur getColumnLong: " + e.getMessage()); }

        return col_value;
        
    } // end getColumnLong()
    
    public int getColumnInt(int pCol) throws TicException {
        int col_value = -1;

        try {
            col_value = rs.getInt(pCol);
        }
        catch (SQLException e) { throw new TicException("Erreur getColumnInt: " + e.getMessage()); }

        return col_value;
        
    } // end getColumnInt()
    
    public int getColumnInt(String pCol) throws TicException {
        int col_value = -1;

        try {
            col_value = rs.getInt(pCol);
        }
        catch (SQLException e) { throw new TicException("Erreur getColumnInt: " + e.getMessage()); }

        return col_value;
        
    } // end getColumnInt()
    
    public double getColumnDouble(int pCol) throws TicException {
        double col_value = -1;

        try {
            col_value = rs.getDouble(pCol);
        }
        catch (SQLException e) { throw new TicException("Erreur getColumnDouble: " + e.getMessage()); }

        return col_value;
        
    } // end getColumnDouble()
    
    public double getColumnDouble(String pCol) throws TicException {
        double col_value = -1;

        try {
            col_value = rs.getDouble(pCol);
        }
        catch (SQLException e) { throw new TicException("Erreur getColumnDouble: " + e.getMessage()); }

        return col_value;
        
    } // end getColumnDouble()
    
    public byte[] getBytes(int pCol) throws TicException {
        byte[] col_value = null;

        try {
            col_value = rs.getBytes(pCol);
        }
        catch (SQLException e) { throw new TicException("Erreur getBytes: " + e.getMessage()); }

        return col_value;
        
    } // end getBytes()
    
    public byte[] getBytes(String pCol) throws TicException {
        byte[] col_value = null;

        try {
            col_value = rs.getBytes(pCol);
        }
        catch (SQLException e) { throw new TicException("Erreur getBytes: " + e.getMessage()); }

        return col_value;
        
    } // end getBytes()
    
    public static String dateiso_sql2java(String pChaine) {

        if (pChaine == null || pChaine.length() < 10) { return pChaine; }
        
        return pChaine.substring(8, 10) + "/" + pChaine.substring(5, 7) + "/" + pChaine.substring(0, 4);
        
    } // end datetime_sql2java()
    
    public static String datetime_sql2java(String pChaine) {

        if (pChaine == null || pChaine.length() < 19) { return pChaine; }
        
        return dateiso_sql2java(pChaine) //pChaine.substring(8, 10) + "/" + pChaine.substring(5, 7) + "/" + pChaine.substring(0, 4) 
            + " " 
            + pChaine.substring(11, 13) + ":" + pChaine.substring(14, 16) + ":" + pChaine.substring(17, 19);
        
    } // end datetime_sql2java()
    
    public static String stamp_java2sql(String pChaine) {
        String chaine;
        
        if (pChaine == null) { return pChaine; }

        if (pChaine.length() < 10) { return pChaine; }

        chaine = pChaine.substring(6, 10) + "-" + pChaine.substring(3, 5) + "-" + pChaine.substring(0, 2);
        if (pChaine.length() < 16) { return chaine; }
        chaine += " " + pChaine.substring(11, 13) + ":" + pChaine.substring(14, 16);
        if (pChaine.length() < 19) { return chaine; }
        chaine += ":" + pChaine.substring(17, 19);

        return chaine;

    } // end stamp_java2sql()
    
    public boolean first() throws TicException {
        
        if (irs == 1) { return true; }
        if (irs <= 0 && deja_next) { return false; }
        
        if (irs <= 0) {
            first_en_cours = next(); 
            return first_en_cours;
        }

        throw new TicException("Erreur first: jeux de résultats avant uniquement");
        
    } // end first()
    
    public boolean next() throws TicException {
        boolean next_ok;
        
        if (deja_next && irs <= 0) { return false; }
        
        if (irs == 1 && first_en_cours) {
            first_en_cours = false;
            return true;
        }

        try {
            next_ok = rs.next();
        }
        catch (SQLException e) { 
            throw new TicException("Erreur next: " + e.getMessage()); 
        }

        if (next_ok) { irs++; }
        
        deja_next = true;
        
        return next_ok;
        
    } // end next()
    
    public void close() {
        if (rs != null) { try { rs.close(); } catch (SQLException e) {} }
        if (stmt != null) { try { stmt.close(); } catch (SQLException e) {} }
        rs = null;
        stmt = null;
        rsmd = null;
    } // end close()
    
} // end SqlVue
