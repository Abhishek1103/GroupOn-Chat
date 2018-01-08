/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package groupon;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 *
 * @author aks
 */
public class DictionaryMaker extends Thread {
    ArrayList<String> dict;

    public DictionaryMaker(ArrayList<String> dict) 
    {
        this.dict=dict;
    }
    
    public void run(){
        try
        {
            BufferedReader br=new BufferedReader(new FileReader("wordlist.txt"));
            String s="";
            while((s=br.readLine())!=null)
            {
                dict.add(s);
                System.out.println(""+s);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
}
