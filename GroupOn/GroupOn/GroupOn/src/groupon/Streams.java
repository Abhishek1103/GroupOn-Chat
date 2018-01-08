/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package groupon;
import java.io.*;
import java.net.Socket;
/**
 *
 * @author aks
 */
public class Streams {
    public static DataInputStream dis=null;
    public static DataOutputStream dout=null;
    public static ObjectInputStream ois=null;
    public static ObjectOutputStream oos=null;
    
    public Streams(Socket sock)
    {
        try
        {
            System.out.println("Initialising streams");
            dis=new DataInputStream(sock.getInputStream());
            System.out.println("dis Initialised");
            dout=new DataOutputStream(sock.getOutputStream());
            System.out.println("dout is Initialised");
            ois=new ObjectInputStream(sock.getInputStream());
            System.out.println("ois is initialised");
            oos=new ObjectOutputStream(sock.getOutputStream());
            System.out.println("oos is initialised");
        }
        catch(Exception e)
        {
            System.out.println("Some error in defining Streams");
            e.printStackTrace();
        }
    }
}
