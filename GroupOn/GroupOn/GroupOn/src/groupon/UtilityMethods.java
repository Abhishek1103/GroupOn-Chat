/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package groupon;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aks
 */
public class UtilityMethods {
    static String LOGINLOGPATH;
    
    
    public static void loginStatusLog(String username, int state){
       PrintWriter p=null;
        try
        {
            LOGINLOGPATH= System.getProperty("user.home")+"/GroupOn/"+username+"/loginStatusLog.txt";
            
            p = new PrintWriter(new BufferedWriter(new FileWriter(LOGINLOGPATH)));
            p.println(state);
            p.close();
        }
        catch (IOException ex)
        {
            Logger.getLogger(UtilityMethods.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
}
