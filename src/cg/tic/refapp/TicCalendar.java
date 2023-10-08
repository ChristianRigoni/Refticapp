/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cg.tic.refapp;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author chr
 */
public class TicCalendar {

    public final static String[] TB_MOIS = { "Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre" };
    public final static String[] TBJOUR_SEMAINE = { "Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi" };
    
    public static String integer_2_2digit(int pValeur) {
        if (pValeur < 10) { return "0" + Integer.toString(pValeur); }
        return Integer.toString(pValeur);
    }
    
    public static int[] chaine_parse(String pChaineDatetime) throws TicException {
        // La chaine date est supposée être de la forme: dd/MM/yyyy HH:mm:ss
        // On retourne une table int de 6 éléments: jour, mois, annee, heure, minute, seconde
        int jour;
        int mois;
        int annee;
        int heure;
        int minute;
        int seconde;
        
        if (pChaineDatetime.length() < 10) { 
            throw new TicException("chaine datetime trop courte: " + pChaineDatetime);
        }

        try {
            jour = Integer.valueOf(pChaineDatetime.substring(0, 2));
            if (jour < 1 || jour > 31) {
                throw new TicException("jour de la chaine datetime incorrect: " + pChaineDatetime);
            }

            mois = Integer.valueOf(pChaineDatetime.substring(3, 5));
            if (mois < 1 || mois > 12) {
                throw new TicException("mois de la chaine datetime incorrect: " + pChaineDatetime);
            }

            annee = Integer.valueOf(pChaineDatetime.substring(6, 10));
            if (annee <= 0) {
                throw new TicException("année de la chaine datetime incorrect: " + pChaineDatetime);
            }

            if (pChaineDatetime.length() <= 10) { return new int[] { jour, mois, annee, 0, 0, 0 }; }

            heure = Integer.valueOf(pChaineDatetime.substring(11, 13));
            if (heure < 0 || heure > 24) {
                throw new TicException("heure de la chaine datetime incorrect: " + pChaineDatetime);
            }

            minute = Integer.valueOf(pChaineDatetime.substring(14, 16));
            if (minute < 0 || minute > 60) {
                throw new TicException("minute de la chaine datetime incorrect: " + pChaineDatetime);
            }

            if (pChaineDatetime.length() <= 16) { return new int[] { jour, mois, annee, heure, minute, 0 }; }

            if (pChaineDatetime.length() < 19) { 
                throw new TicException("chaine datetime pour secondes trop courte: " + pChaineDatetime);
            }

            seconde = Integer.valueOf(pChaineDatetime.substring(17, 19));
            if (seconde < 0 || seconde > 60) {
                throw new TicException("seconde de la chaine datetime incorrect: " + pChaineDatetime);
            }

            return new int[] { jour, mois, annee, heure, minute, seconde };
            
        }
        catch (NumberFormatException nbex) {
            throw new TicException("Chaine datetime " + pChaineDatetime + " incorrecte: " + nbex.getMessage());
        }
        
    } // end chaine_parse()
    
    public static int[] chaineiso_parse(String pChaineDatetime) throws TicException {
        // La chaine date est supposée être de la forme: yyyy-MM-dd HH:mm:ss
        // On retourne une table int de 6 éléments: jour, mois, annee, heure, minute, seconde
        int jour;
        int mois;
        int annee;
        int heure;
        int minute;
        int seconde;
        
        if (pChaineDatetime.length() < 10) { 
            throw new TicException("chaine datetime trop courte: " + pChaineDatetime);
        }

        try {
            annee = Integer.valueOf(pChaineDatetime.substring(0, 4));
            if (annee <= 0) {
                throw new TicException("année de la chaine datetime ISO incorrect: " + pChaineDatetime);
            }

            mois = Integer.valueOf(pChaineDatetime.substring(5, 7));
            if (mois < 1 || mois > 12) {
                throw new TicException("mois de la chaine datetime ISO incorrect: " + pChaineDatetime);
            }

            jour = Integer.valueOf(pChaineDatetime.substring(8, 10));
            if (jour < 1 || jour > 31) {
                throw new TicException("jour de la chaine datetime ISO incorrect: " + pChaineDatetime);
            }

            if (pChaineDatetime.length() <= 10) { return new int[] { jour, mois, annee, 0, 0, 0 }; }

            heure = Integer.valueOf(pChaineDatetime.substring(11, 13));
            if (heure < 0 || heure > 24) {
                throw new TicException("heure de la chaine datetime ISO incorrect: " + pChaineDatetime);
            }

            minute = Integer.valueOf(pChaineDatetime.substring(14, 16));
            if (minute < 0 || minute > 60) {
                throw new TicException("minute de la chaine datetime ISO incorrect: " + pChaineDatetime);
            }

            if (pChaineDatetime.length() <= 16) { return new int[] { jour, mois, annee, heure, minute, 0 }; }

            if (pChaineDatetime.length() < 19) { 
                throw new TicException("chaine datetime ISO pour secondes trop courte: " + pChaineDatetime);
            }

            seconde = Integer.valueOf(pChaineDatetime.substring(17, 19));
            if (seconde < 0 || seconde > 60) {
                throw new TicException("seconde de la chaine datetime ISO incorrect: " + pChaineDatetime);
            }

            return new int[] { jour, mois, annee, heure, minute, seconde };
            
        }
        catch (NumberFormatException nbex) {
            throw new TicException("Chaine datetime ISO " + pChaineDatetime + " incorrecte: " + nbex.getMessage());
        }
        
    } // end chaineiso_parse()
    
    public static String get_short_date(String pChaineDatetime) {
        if (Base.chaine_is_vide(pChaineDatetime)) { return ""; }
        if (pChaineDatetime.length() <= 10) { return pChaineDatetime; }
        return pChaineDatetime.substring(0, 10);
    }
    
    public TicCalendar(String pChaineDatetime, boolean pIsDateiso) throws TicException {
        int[] tb_cal;
        
        if (Base.chaine_is_vide(pChaineDatetime)) {
            cal = new GregorianCalendar();
            return;
        }
        
        tb_cal = pIsDateiso ? chaineiso_parse(pChaineDatetime) : chaine_parse(pChaineDatetime);
        
        if (tb_cal == null || tb_cal.length != 6) { 
            throw new TicException("get_calendar, chaine datetime incorrecte: " + pChaineDatetime);
        }
        
        cal = new GregorianCalendar(tb_cal[2], tb_cal[1] - 1, tb_cal[0], tb_cal[3], tb_cal[4], tb_cal[5]);
        
    } // end TicCalendar()
    public TicCalendar(String pChaineDatetime) throws TicException {
        this(pChaineDatetime, false);
    }
    public TicCalendar(GregorianCalendar pGregorianCalendar) {
        cal = pGregorianCalendar;
    }
    public TicCalendar() {
        cal = new GregorianCalendar();
    }
    
    public GregorianCalendar cal;
    
//    public GregorianCalendar dernier_jour_du_mois() {
    public TicCalendar dernier_jour_du_mois() {
        int mois_date;
        GregorianCalendar mycal;

        mois_date = cal.get(java.util.Calendar.MONTH);
        mycal = (GregorianCalendar) cal.clone();
        while (mycal.get(java.util.Calendar.MONTH) == mois_date) {
            mycal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }

        mycal.add(java.util.Calendar.DAY_OF_MONTH, -1);
        
        return new TicCalendar(mycal);

    } // end dernier_jour_du_mois()
    
    public int get_trimestre() {
        int mois_date;

        mois_date = cal.get(java.util.Calendar.MONTH);
        
        if (mois_date < 3) {
            return 1;
        }
        
        if (mois_date < 6) {
            return 4;
        }
        
        if (mois_date < 9) {
            return 7;
        }
        
        return 10;

    } // end get_trimestre()
    
    public boolean after(TicCalendar pCal) {
        return cal.after(pCal.cal);
    }
    
    public boolean before(TicCalendar pCal) {
        return cal.before(pCal.cal);
    }
    
    public int compareTo(TicCalendar pCal) {
        return cal.compareTo(pCal.cal);
    }
    
    public int jsemaine_get() {
        int jsemaine;
        
        // Le day_of_week va de 1 à 7 pour D L M M J V S
        jsemaine = cal.get(Calendar.DAY_OF_WEEK);
        if (jsemaine == 1) { jsemaine = 8; }    // Faire passer le D en dernier
        jsemaine -= 2; // pour aller de 0 à 6, pour L M M J V S D
        return jsemaine;
    }
    
    public int week_get() throws TicException {
        int no_week;
        int jour;
        String annee = formater("%Y");
        TicCalendar tic_cal = new TicCalendar("01/01/" + annee);
        
        no_week = 1;
        while (tic_cal.formater("%Y").equals(annee) && compareTo(tic_cal) >= 0) {
            no_week++;
            for (jour=0; jour < 7; jour++) {
                if (tic_cal.formater("%Y").equals(annee) && tic_cal.jsemaine_get() == jour) {
                    tic_cal.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
        }
        return no_week - 1;
    }
    
    public String formater(String pFormat) {
        String chaine;
        String mycar;
        int ichaine;
        int jour;
        int mois;
        
        if (Base.chaine_is_vide(pFormat)) { return ""; }
        
        ichaine = 0;
        chaine = "";
        while (ichaine < pFormat.length()) {
            mycar = pFormat.substring(ichaine++, ichaine);
            if ("%".equals(mycar)) {
                if (ichaine >= pFormat.length()) { return chaine + "%"; }
                mycar = pFormat.substring(ichaine++, ichaine);
                switch (mycar) {
                    case "d":
                        chaine += integer_2_2digit(cal.get(java.util.Calendar.DAY_OF_MONTH));
                        break;
                    case "m":
                        chaine += integer_2_2digit(cal.get(java.util.Calendar.MONTH) + 1);
                        break;
                    case "Y":
                        chaine += Integer.toString(cal.get(java.util.Calendar.YEAR));
                        break;
                    case "H":
                        chaine += integer_2_2digit(cal.get(java.util.Calendar.HOUR_OF_DAY));
                        break;
                    case "M":
                        chaine += integer_2_2digit(cal.get(java.util.Calendar.MINUTE));
                        break;
                    case "S":
                        chaine += integer_2_2digit(cal.get(java.util.Calendar.SECOND));
                        break;
                    case "A":
                        jour = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1;
                        chaine += (jour >= 0 && jour < TBJOUR_SEMAINE.length) ? TBJOUR_SEMAINE[jour] : integer_2_2digit(jour+1);
                        break;
                    case "B":
                        mois = cal.get(java.util.Calendar.MONTH);
                        chaine += (mois >= 0 && mois < TB_MOIS.length) ? TB_MOIS[mois] : integer_2_2digit(mois + 1);
                        break;
                    case "%":
                        chaine += "%";
                        break;
                    default:
                        chaine += "%" + mycar;
                        break;
                } // end switch
            }
            else {
                chaine += mycar;
            }
        } // end while()
        
        return chaine;
        
    } // end formater()

    public String short_formater() {
        return formater("%d/%m/%Y");
    }

    public String short_lettre_formater() {
        return formater("%A %d %B %Y");
    }

    public String long_formater() {
        return formater("%d/%m/%Y %H:%M");
    }

    public String long_lettre_formater() {
        return formater("%A %d %B %Y %H:%M");
    }

    public String full_formater() {
        return formater("%d/%m/%Y %H:%M:%S");
    }
    
    public void add(int pField, int pAmount) {
        cal.add(pField, pAmount);
    }
    
    public static String now_formater(String pFormat) throws TicException {
        TicCalendar tic_cal = new TicCalendar();
        return tic_cal.formater(pFormat);
    }
    public static String now_short_formater() throws TicException {
        TicCalendar tic_cal = new TicCalendar();
        return tic_cal.short_formater();
    }
    public static String now_short_lettre_formater() throws TicException {
        TicCalendar tic_cal = new TicCalendar();
        return tic_cal.short_lettre_formater();
    }
    public static String now_long_formater() throws TicException {
        TicCalendar tic_cal = new TicCalendar();
        return tic_cal.long_formater();
    }
    public static String now_long_lettre_formater() throws TicException {
        TicCalendar tic_cal = new TicCalendar();
        return tic_cal.long_lettre_formater();
    }
    public static String now_full_formater() throws TicException {
        TicCalendar tic_cal = new TicCalendar();
        return tic_cal.full_formater();
    }
    public static int now_get_year() throws TicException {
        TicCalendar tic_cal = new TicCalendar();
        return Integer.parseInt(tic_cal.formater("%Y"));
    }
    
    public static int get_days_diff(TicCalendar pTiccal1, TicCalendar pTiccal2) {
        int elapsed = 0;
        GregorianCalendar gc1, gc2;

        if (pTiccal1.after(pTiccal2)) {
           gc1 = (GregorianCalendar) pTiccal2.cal.clone();
           gc2 = (GregorianCalendar) pTiccal1.cal.clone();
        }
        else   {
           gc1 = (GregorianCalendar) pTiccal1.cal.clone();
           gc2 = (GregorianCalendar) pTiccal2.cal.clone();
        }
        
        gc1.clear(Calendar.MILLISECOND);
        gc1.clear(Calendar.SECOND);
        gc1.clear(Calendar.MINUTE);
        gc1.clear(Calendar.HOUR_OF_DAY);
        gc2.clear(Calendar.MILLISECOND);
        gc2.clear(Calendar.SECOND);
        gc2.clear(Calendar.MINUTE);
        gc2.clear(Calendar.HOUR_OF_DAY);
        
        while (gc1.before(gc2) ) {
           gc1.add(Calendar.DATE, 1);
           elapsed++;
        }
        
        return elapsed;

    } // end get_days_diff
    
    public static int get_months_diff(TicCalendar pTiccal1, TicCalendar pTiccal2) {
        int elapsed = 0;
        GregorianCalendar gc1, gc2;

        if (pTiccal1.after(pTiccal2)) {
           gc1 = (GregorianCalendar) pTiccal2.cal.clone();
           gc2 = (GregorianCalendar) pTiccal1.cal.clone();
        }
        else   {
           gc1 = (GregorianCalendar) pTiccal1.cal.clone();
           gc2 = (GregorianCalendar) pTiccal2.cal.clone();
        }
        
        gc1.clear(Calendar.MILLISECOND);
        gc1.clear(Calendar.SECOND);
        gc1.clear(Calendar.MINUTE);
        gc1.clear(Calendar.HOUR_OF_DAY);
        gc2.clear(Calendar.MILLISECOND);
        gc2.clear(Calendar.SECOND);
        gc2.clear(Calendar.MINUTE);
        gc2.clear(Calendar.HOUR_OF_DAY);
        
        while (gc1.before(gc2) ) {
           gc1.add(Calendar.MONTH, 1);
           elapsed++;
        }
        
        return elapsed;

    } // end get_months_diff
    
} // end class TicCalendar
