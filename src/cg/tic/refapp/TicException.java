/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cg.tic.refapp;

/**
 *
 * @author chr
 */
public class TicException extends Exception {

    // Indique si les données sql doivent être complétées avec le formulaire posté s'il existe
    boolean sql_only;
    
    public TicException(String pMessage) {  
        super(pMessage); 
        sql_only = false;
    }  
    
    public TicException() { 
        super(); 
        sql_only = false;
    }  

    public TicException(Throwable pCause) {  
        super(pCause);
        sql_only = false;
    }  
    
    public TicException(String pMessage, Throwable pCause) {  
        super(pMessage, pCause); 
        sql_only = false;
    }
    
    public TicException(String pMessage, boolean pSqlOnly) {  
        super(pMessage); 
        sql_only = pSqlOnly;
    }  
    
    public TicException(boolean pSqlOnly) { 
        super(); 
        sql_only = pSqlOnly;
    }  

    public TicException(Throwable pCause, boolean pSqlOnly) {  
        super(pCause);
        sql_only = pSqlOnly;
    }  
    
    public TicException(String pMessage, Throwable pCause, boolean pSqlOnly) {  
        super(pMessage, pCause); 
        sql_only = pSqlOnly;
    }
    
    public boolean get_sqlonly() {
        return sql_only;
    }
        
} // end class TicException
