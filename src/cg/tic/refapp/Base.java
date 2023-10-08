/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cg.tic.refapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 *
 * @author Administrateur
 */
public class Base {
    
    public final static String VERSION = "Ticrefapp020";

    public static String chaine_enlever_simple_quote(String pChaine) {

        if (pChaine.length() <= 1) { return pChaine; }

        if (pChaine.charAt(0) != '\'' || pChaine.charAt(pChaine.length()-1) != '\'') { return pChaine; }

        return pChaine.substring(1, pChaine.length() - 1);

    } // end chaine_enlever_simple_quote()

    public static String randomString_get(int pSize) {
	    String tb_car = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	    String chaine;
            int pos;
            int icar;
            int lg;
            
            chaine = "";
            lg = tb_car.length() - 1;
	    for(pos=0; pos < pSize; pos++) {
	       icar = (int)Math.floor(Math.random() * lg);
	       chaine += tb_car.charAt(icar);
	    }
            
            return chaine;
            
    } // end randomString_get()

    public static String filename_extension_get(String pFilename) {
        int pos;
        String extension;

        extension = "";

        pos = pFilename.length() - 1;
        while (pos > 0) {
            if (pFilename.charAt(pos) != '.') { extension = pFilename.substring(pos, pos + 1) + extension; }
            else { return extension; }
            pos--;
        } // end while

        return "";

    } // end filename_extension_get()

    public static String basename_get(String pFilename) {
        int pos;
        
        pos = pFilename.lastIndexOf('.');
        return pos == -1 ? pFilename : pFilename.substring(0, pos);

    } // end basename_get()
    
    public static String fichier_lire(String pFilePath) throws TicException {
        String contenu = "";
 
        try {
            contenu = new String(Files.readAllBytes(Paths.get(pFilePath)));
        }
        catch (IOException ioex) {
            throw new TicException("fichier_lire) Erreur lecture " + pFilePath + ": "+ ioex.getMessage());
        }
 
        return contenu;
        
    } // end fichier_lire
    
    public static void fichier_creer(String pFilePath, String pContenu) throws TicException {

        java.io.BufferedWriter fd_out = null;
        try {
            fd_out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(pFilePath), "UTF-8"));
            fd_out.write(pContenu);
        }
        catch (IOException ioex) {
            System.err.println("fichier_creer) Erreur de création du fichier javascript: " + pFilePath + ", erreur: " + ioex.getMessage());
        }
        finally {
            if (fd_out != null) {
                try { fd_out.close(); } catch (IOException e) {}
            }
        }
        
    } // end fichier_creer
    
    public static void fichier_copier(String pFileFromPath, String pFileToPath) throws TicException {
        FileChannel fd_in = null; // canal d'entrée
        FileChannel fd_out = null; // canal de sortie
 
        try {
            // Init
            fd_in = new FileInputStream(pFileFromPath).getChannel();
            fd_out = new FileOutputStream(pFileToPath).getChannel();
 
            // Copie
            fd_in.transferTo(0, fd_in.size(), fd_out);
        } 
        catch (IOException e) {
            throw new TicException("erreur copie de " + pFileFromPath + " vers " + pFileToPath + ": " + e.getMessage());
        } 
        finally { // finalement on ferme
            if (fd_in != null) {
                try { fd_in.close(); } catch (IOException e) {}
            }
            if (fd_out != null) {
                try { fd_out.close(); } catch (IOException e) {}
            }
        }        
        
    } // end fichier_copier()

    public static void dossier_fichiers_supprimer(String pDir, String pFilesystemSeparator) throws TicException {
        int ienfant;
        File enfant;
        
        if (chaine_is_vide(pDir)) {
            throw new TicException("dossier_fichiers_supprimer: impossible de supprimer les fichiers, pDir est vide");
        }
        
	File dir = new File(pDir);
	if (!dir.exists()) {
            throw new TicException("Impossible de trouver le dossier " + pDir);
        }

        String[] enfants = dir.list();
	if (enfants == null) { return; }

        for (ienfant=0; ienfant < enfants.length; ienfant++) {
            enfant = new File(dir.getAbsolutePath() + pFilesystemSeparator + enfants[ienfant]);
            if (enfant.exists() && enfant.isFile()) {
                if (!enfant.delete()) { throw new TicException("problème delete fichier " + enfant.getPath()); }
            }
        }

    } // end dossier_fichiers_supprimer()
    public static void dossier_fichiers_supprimer(String pDir) throws TicException {
        dossier_fichiers_supprimer(pDir, "\\");
    }

    public static void dossier_fichiers_copier(String pDirFrom, String pDirTo, boolean pCleanDirTo, String pFilesystemSeparator) throws TicException {
        int ienfant;
        File enfant;
        
        if (chaine_is_vide(pDirFrom) || chaine_is_vide(pDirTo)) {
            throw new TicException("dossier_fichiers_copier: dossier pDirFrom ou pDirTo vide");
        }
        
        // Nettoyer le dossier destination avant de copier
        if (pCleanDirTo) {
            dossier_fichiers_supprimer(pDirTo, pFilesystemSeparator);
        }

        // Vérifier l'existence des fichiers
	File dir_from = new File(pDirFrom);
	if (!dir_from.exists()) {
            throw new TicException("Impossible de trouver le dossier from " + pDirFrom);
        }
	File dir_to = new File(pDirTo);
	if (!dir_to.exists()) {
            throw new TicException("Impossible de trouver le dossier to " + pDirTo);
        }

        String[] enfants = dir_from.list();
	if (enfants == null) { return; }

        for (ienfant=0; ienfant < enfants.length; ienfant++) {
            enfant = new File(dir_from.getAbsolutePath() + pFilesystemSeparator + enfants[ienfant]);
            if (enfant.exists() && enfant.isFile()) {
                fichier_copier(dir_from.getAbsolutePath() + pFilesystemSeparator + enfants[ienfant], dir_to.getAbsolutePath() + pFilesystemSeparator + enfants[ienfant]);
            }
        }

    } // end dossier_fichiers_copier()
    public static void dossier_fichiers_copier(String pDirFrom, String pDirTo, boolean pCleanDirTo) throws TicException {
        dossier_fichiers_copier(pDirFrom, pDirTo, pCleanDirTo, "\\");
    }

    public final static String[] TB_CHIFFRES = new String[]{"zéro", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf", "dix", "onze", "douze", "treize", "quatorze", "quinze", "seize", "dix-sept", "dix-huit", "dix-neuf"};
    public final static String[] TB_DIZAINES = new String[]{"zéro", "dix", "vingt", "trente", "quarante", "cinquante", "soixante", "soixante-dix", "quatre-vingt", "quatre-vingt-dix"};
    public final static String[] TB_MILLE = new String[]{"zéro", "mille", "million", "milliard", "billion", "billiard", "trillion", "trilliard"};
    
    public static String nb2letters(long pNb, boolean pUCaseFirstLetter) {
        long mynb;
        int r_mille;
        int q_cent;
        int r_cent;
        int q_dix;
        int r_dix;
        String liaison_dizaine;
        int q_mille;
        String chaine_add;
        String chaine;

        chaine = "";
        q_mille = 0;
        mynb = pNb;

        while (mynb > 0) {
            r_mille = (int)(mynb % 1000);
            //pTicServlet.debug.add(Base.class.getName() + ".nb2letters", "r_mille: " + r_mille + ", q_mille: " + q_mille);
            if (r_mille > 0 && q_mille > 0) {
                chaine_add = TB_MILLE[q_mille];
                if (r_mille > 1 && q_mille > 1) { chaine_add += "s"; }
                //pTicServlet.debug.add(Base.class.getName() + ".nb2letters", "chaine_add: " + chaine_add);
                if (chaine.length() > 0) { chaine = " " + chaine; }
                chaine = chaine_add + chaine;
            }
            switch (r_mille) {
            case 0:
                break;
            case 1:
                if (q_mille != 1) {
                    if (chaine.length() > 0) { chaine = " " + chaine; }
                    chaine = TB_CHIFFRES[r_mille] + chaine; 
                }
                break;
            default:
                chaine_add = "";
                r_cent = r_mille % 100;
                q_cent = (r_mille - r_cent) / 100;
                //pTicServlet.debug.add(Base.class.getName() + ".nb2letters", "r_cent: " + r_cent + ", q_cent: " + q_cent);
                if (r_cent > 0) {
                    liaison_dizaine = "-";
                    r_dix = r_cent % 10;
                    q_dix = (r_cent - r_dix) / 10;
                    if ((q_dix > 1) && (r_dix == 1)) { liaison_dizaine = " et "; }
                    if (r_cent == 91 || r_cent == 81) { liaison_dizaine = "-"; }
                    //pTicServlet.debug.add(Base.class.getName() + ".nb2letters", "r_dix: " + r_dix + ", q_dix: " + q_dix + ", liaison dizaine: " + liaison_dizaine);
                    switch (q_dix) {
                        case 0:
                            if (chaine.length() > 0) { chaine = " " + chaine; }
                            chaine = TB_CHIFFRES[r_cent] + chaine;
                            break;
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 8:
                            //pTicServlet.debug.add(Base.class.getName() + ".nb2letters", "case 2,3,4,5,6,8");
                            if (r_dix == 0 && q_dix == 8 && q_mille != 1) { chaine_add = "s" + chaine_add; }
                            if (r_dix > 0) { chaine_add = TB_CHIFFRES[r_dix] + chaine_add; }
                            if (r_dix > 0 && q_dix > 0) { chaine_add = liaison_dizaine + chaine_add; }
                            if (chaine.length() > 0) { chaine = " " + chaine; }
                            chaine = TB_DIZAINES[q_dix] + chaine_add + chaine;
                            break;
                        case 1:
                        case 7:
                        case 9:
                            //pTicServlet.debug.add(Base.class.getName() + ".nb2letters", "case 1,7,9");
                            q_dix--;
                            r_dix += 10;
                            chaine_add = TB_CHIFFRES[r_dix] + chaine_add;
                            if (q_dix > 0) { chaine_add = TB_DIZAINES[q_dix] + liaison_dizaine + chaine_add; }
                            if (chaine.length() > 0) { chaine = " " + chaine; }
                            chaine = chaine_add + chaine;
                    } // end switch()
                    //pTicServlet.debug.add(Base.class.getName() + ".nb2letters", "r_cent > 0, chaine: " + chaine);
                } // end if (r_cent > 0)
                if (q_cent > 0) {
                    chaine_add = "cent";
                    if (q_cent > 1 && r_cent == 0 && q_mille != 1) { chaine_add += "s"; }
                    if (q_cent > 1) { chaine_add = TB_CHIFFRES[q_cent] + " " + chaine_add; }
                    if (chaine.length() > 0) { chaine = " " + chaine; }
                    chaine = chaine_add + chaine;
                }
            } // end switch (r_mille)
            mynb = (mynb - r_mille) / 1000;
            //pTicServlet.debug.add(Base.class.getName() + ".nb2letters", "avant loop, mynb: " + mynb);
            q_mille++;
        } // end while (mynb > 0)
        if (chaine_is_vide(chaine)) { chaine = "zéro"; }
        
        if (pUCaseFirstLetter) {
            String premiere_lettre = chaine.substring(0, 1).toUpperCase();
            chaine = premiere_lettre + chaine.substring(1);

        }
        
        return chaine;

    } // end nb2letters()
    public static String nb2letters(long pNb) {
        return nb2letters(pNb, false);
    }

    public static String doublejava_2_valeurjs(double pValeur) {
        String chaine_double;

        chaine_double = String.valueOf(pValeur);
        chaine_double = chaine_double.replace(" ", "");
        chaine_double = chaine_double.replace("[^0-9,]", "");

        return chaine_double.replace(",", ".");

    } // end doublejava_2_valeurjs()

    public static String hashmap_key_get(HashMap<String, String> pHashMap, String pKey) {

        if (!pHashMap.containsKey(pKey)) { return ""; }
        
        return pHashMap.get(pKey);

    } // end hashmap_key_get()

    public static void hashmap_key_set(HashMap<String, String> pHashMap, String pKey, String pKeyValue) {

        if (pHashMap.containsKey(pKey)) { pHashMap.remove(pKey); }

        pHashMap.put(pKey, pKeyValue);

    } // end hashmap_key_set()
    
    public static boolean chaine_is_vide(String pChaine, boolean pGarderEspaces) {
        if (pChaine == null || pChaine.length() <= 0) { return true; }
        if (pGarderEspaces) { return false; }
        if (pChaine.replaceAll("[ \t]", "").length() <= 0) { return true; }
        return false;
    }
    public static boolean chaine_is_vide(String pChaine) {
        return chaine_is_vide(pChaine, false);
    }
    
    public static void std_purge(String pTitreln, InputStream pIs) throws IOException, TicException {
        InputStreamReader isr = new InputStreamReader(pIs);
        BufferedReader br = new BufferedReader(isr);
        String ligne;

        while ((ligne = br.readLine()) != null) {
            System.out.println(Base.class.getName() + ".std_purge, " + pTitreln + ligne);
        }
    } // end std_purge()
    
    public static void commande_lancer(String pCommande, String pArgs) throws TicException {
        try {
            Runtime rt = Runtime.getRuntime();
            //Process proc = rt.exec("perl --help");
            //Process proc = rt.exec("perl -w c:\\perl64\\eg\\minify_js.pl" + " " + pCheminFichier);
            Process proc = rt.exec(pCommande + " " + pArgs);
            
            std_purge("std_error: ", proc.getErrorStream());
            
            std_purge("std_out: ", proc.getInputStream());
            
            int exitVal = proc.waitFor();
            System.out.println(Base.class.getName() + ".commande_lancer, status: " + exitVal);
            
        } catch (IOException | InterruptedException ex) {
            throw new TicException("Erreur commande_lancer: " + ex.getMessage());
        }
    } // end commande_lancer()
    
    public static void commande_bat_lancer(String pCommande) throws TicException {
        
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();

            // Run this on Windows, cmd, /c = terminate after this run
            //processBuilder.command("cmd.exe", "/c", "ping -n 3 google.com");
            
            System.out.println(Base.class.getName() + ".commande_bat_lancer: " + pCommande);

            processBuilder.command("cmd.exe", "/c", pCommande);
            Process process = processBuilder.start();

            // blocked :(
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("\nExited with error code : " + exitCode);

        }          
        /*
            ne sort pas du script .bat...
            String path="cmd /c start " + pCommande + " 2>&1";
            Runtime rn = Runtime.getRuntime();
            Process pr = rn.exec(path);
            InputStream stdout = pr.getInputStream();
            while( stdout.read() >= 0 ) { ; }
            pr.waitFor();
        
        }*/
        catch(IOException|InterruptedException ioException) {
            throw new TicException("Erreur " + pCommande + ": " + ioException.getMessage());
            //System.out.println(ioException.getMessage() );
        }
        
    } // end commande_bat_lancer

    public static boolean is_host_reachable(String pHost) {
        InetAddress address;
        
        try {
            address = InetAddress.getByName(pHost);
            if (address.isReachable(5000)) {
                return true;
            }
        } catch (IOException ex2) {
        }
        
        return false;
            
    } // end is_host_reachable
    
    
} // end Base
