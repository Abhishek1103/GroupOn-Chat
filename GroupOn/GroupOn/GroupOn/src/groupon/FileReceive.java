/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package groupon;
import static groupon.Messenger.contactList;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.text.StyledDocument;
/**
 *
 * @author aks
 */
public class FileReceive extends Thread{
    Socket sock;
    String username;
    String selectedFrnd="";

   
    
   FileReceive(Socket sock, String username){
        this.sock=sock;
        this.username=username;
    }
    
    public void run(){
        System.out.println("File Receive is running");
        DataOutputStream dout=FileStreams.dout;
        DataInputStream dis=FileStreams.dis;
        ObjectOutputStream oos=FileStreams.oos;
        ObjectInputStream ois=FileStreams.ois;
        
        
        System.out.println("In file Receive thread");
        StyledDocument sdoc=Messenger.displayTxtPane.getStyledDocument();
        String msg="";
        while(true)
        {
          try
          {
              try
              {
                System.err.println("Waiting to receive file in File Receive thread");
                msg=dis.readUTF();
                System.err.println("Message Received: "+msg);
              }
              catch(Exception e)
              {
                  System.out.println("breaking out of while");
                  break;
              }
              if(msg.equals("\\File[$]File\\"))
              {
                  String friendusername=dis.readUTF();
                  System.out.println("friendUsername : "+friendusername);
                  String fileName=dis.readUTF();
                  System.out.println("fileName : "+fileName);
                  String path=System.getProperty("user.home")+"/GroupOn/"+username+"/";
                  SaveFile sf=new SaveFile();
                  sf.saveFile(sock, path, ois);
                  System.out.println("File Received");
                  
                  selectedFrnd=Messenger.contactList.getSelectedValue();
                  if(selectedFrnd!=null)
                  {
                    selectedFrnd=selectedFrnd.substring(0, selectedFrnd.lastIndexOf("-")).trim();
                    if(selectedFrnd==friendusername)
                    {
                        sdoc.insertString(sdoc.getLength(),friendusername+" : File-> "+fileName ,null);
                        System.out.println("Message appended");
                    }
                    else
                    {
                      JMenuItem menuItem = new JMenuItem(new AbstractAction("<html><body><b>"+friendusername+"</b><br> -has sent you a file</body></html>")
                    {
                        @Override
                        public void actionPerformed(ActionEvent e) 
                        {
                        Messenger.userClicked(friendusername+"-");
                        Messenger.notificationMenu.setText("<html><body>Notifications</body></html>");
                        Messenger.contactList.setSelectedValue(friendusername+"\t- Offline", true);
                        Messenger.contactList.setSelectedValue(friendusername+"\t- Online", true);
                        Messenger.notifications.remove(friendusername);
                        //Messenger.notificationMenu.remove();        //Try to find the solution...
                        }
                    });
                    Messenger.notificationMenu.add(menuItem);
                    Messenger.notificationMenu.addSeparator();
                    Messenger.notificationMenu.setText("<html><body><b>Notifications</b></body></html>");
                    }
                  }
                  
             }
          }
          catch(Exception e)
          {
              e.printStackTrace();
          }
        }
    }
}
