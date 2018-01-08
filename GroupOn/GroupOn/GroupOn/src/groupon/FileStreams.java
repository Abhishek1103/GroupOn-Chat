/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package groupon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author aks
 */
public class FileStreams {
    public static DataInputStream dis=null;
    public static DataOutputStream dout=null;
    public static ObjectInputStream ois=null;
    public static ObjectOutputStream oos=null;
    
    FileStreams(Socket sock){
        try
        {
            System.out.println("");
            System.out.println("Initialising Streams");
            dis=new DataInputStream(sock.getInputStream());
            System.out.println("dis initialised");
            dout=new DataOutputStream(sock.getOutputStream());
            System.out.println("dout initialised");
            oos=new ObjectOutputStream(sock.getOutputStream());
            System.out.println("oos is initialised");
            ois=new ObjectInputStream(sock.getInputStream());
            System.out.println("ois initialised");
        }
        catch(Exception e)
        {
            System.out.println("Some error in initializing Streams");
            e.printStackTrace();
        }
    }
}
