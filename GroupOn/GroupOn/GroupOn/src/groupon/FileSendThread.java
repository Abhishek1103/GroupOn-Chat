/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package groupon;

import static groupon.GroupStreams.dis;
import static groupon.GroupStreams.dout;
import static groupon.GroupStreams.ois;
import static groupon.GroupStreams.oos;
import static groupon.Messenger.displayTxtPane;
import java.io.*;
import java.net.*;
import javax.swing.text.StyledDocument;
/**
 *
 * @author aks
 */
// this thread will be spawned each time when attach file button is clicked for a particular user....
public class FileSendThread extends Thread {
    String friendUsername;
    String filePath;
    static DataInputStream dis=null;
    static DataOutputStream dout=null;
    static ObjectInputStream ois=null;
    static ObjectOutputStream oos=null;
    
    FileSendThread(String friendUsername, String filePath)
    {
        this.friendUsername=friendUsername;
        this.filePath=filePath;
    }
    
    public void run()
    {
        Socket fileSock=null;
        StyledDocument sdoc = Messenger.displayTxtPane.getStyledDocument();
        try
        {
            fileSock=new Socket("172.31.78.171", 8003);
            dis=new DataInputStream(fileSock.getInputStream());
            System.out.println("dis initialised");
            dout=new DataOutputStream(fileSock.getOutputStream());
            System.out.println("dout initialised");
            oos=new ObjectOutputStream(fileSock.getOutputStream());
            System.out.println("oos is initialised");
            ois=new ObjectInputStream(fileSock.getInputStream());
            System.out.println("ois initialised");
            
            dout.writeUTF(friendUsername);
            
            SendFile sndF=new SendFile();
            sndF.sendFile(fileSock, filePath, oos);
            System.out.println("File Transfer Complete");
            sdoc.insertString(sdoc.getLength(), Messenger.username+" : File-> "+filePath.substring(filePath.lastIndexOf("/")+1)+"\n", null);
            
            dout.close();
            dis.close();
            oos.close();
            ois.close();
            fileSock.close();
        }
        catch(Exception e)
        {
            System.out.println("Error in connecting to file Server");
        }
        
        
        
    }
}
