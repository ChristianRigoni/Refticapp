/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cg.tic.refapp;

/**
 *
 * @author chr
 */
public class SqlResultat {
    
    int nb_rows;
    int nb_isql;
    
    public SqlResultat() {
        nb_rows = 0;
        nb_isql = 0;
    }
    
    public void add(int pNbrows, int pNbisql) {
        nb_rows += pNbrows;
        nb_isql += pNbisql;
    }

    public void reset() {
        nb_rows = 0;
        nb_isql = 0;
    }
    
    public int get_nbrows() {
        return nb_rows;
    }
    
    public int get_nbisql() {
        return nb_isql;
    }
    
} // end class SqlResultat
