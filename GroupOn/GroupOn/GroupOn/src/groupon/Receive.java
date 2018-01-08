 package groupon;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import static groupon.Messenger.contactList;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.text.StyledDocument;
/**
 *
 * @author aks
 */
public class Receive extends Thread{
    Socket sock;
     
    String username;
    
     public  Receive(Socket sock, String username){
       this.sock=sock;
       this.username=username;
   }
    
    public void run(){
    try{
        DataInputStream dis=Streams.dis;
        System.out.println("Recieve is running");
      StyledDocument sdoc;
      
      String selectedUsername="";
    while(true)
    {
         
        System.out.println("Waiting for message...");
        String msg=dis.readUTF();
        System.out.println("Message Recieved: "+msg);
        
        if(!(msg.equals("\\#%imp%#\\") || msg.equals("$\\hula[]N[]hula\\$") || msg.equals("\\#impR[]\\#imp") || msg.equals("\\#UsR[]\\#UsR") || msg.equals("\\Pro[$]Pro\\")))
        {  
            System.out.println("in if...");
            sdoc = Messenger.displayTxtPane.getStyledDocument();
            String friendUsername=msg.substring(0,msg.indexOf(":")).trim();
            System.out.println(""+friendUsername);
            try
            {
                selectedUsername=Messenger.contactList.getSelectedValue();
            }
            catch(Exception e)
            {
                System.out.println("Friend list is selected");
                selectedUsername="Friend List-";
            }
             selectedUsername=selectedUsername.substring(0, selectedUsername.lastIndexOf("-")).trim();
            
            if(friendUsername.equals(selectedUsername))
            {
                sdoc.insertString(sdoc.getLength(), msg+"\n", null);
                System.out.println("TextPane"+msg);
                
            }
            else
            {
                int f=0;
                for(String i : Messenger.notifications){
                    if(i.equals(friendUsername))
                    {
                       f=1;
                       break;
                    }
                }
                if(f==0)
                {
                    JMenuItem menuItem = new JMenuItem(new AbstractAction("<html><body><b>"+friendUsername+"</b><br> -has sent you a message</body></html>")
                    {
                        @Override
                        public void actionPerformed(ActionEvent e) 
                        {
                        Messenger.userClicked(friendUsername+"-");
                        Messenger.notificationMenu.setText("<html><body>Notifications</body></html>");
                        contactList.setSelectedValue(friendUsername+"\t- Offline", true);
                        contactList.setSelectedValue(friendUsername+"\t- Online", true);
                        Messenger.notifications.remove(friendUsername);
                        //Messenger.notificationMenu.remove();        //Try to find the solution...
                        }
                    });
                    Messenger.notificationMenu.add(menuItem);
                    Messenger.notificationMenu.addSeparator();
                    Messenger.notificationMenu.setText("<html><body><b>Notifications</b></body></html>");
                }
                 Messenger.notificationMenu.setText("<html><body><b>Notifications</b></body></html>");
            }
           
            BufferedWriter bw=new BufferedWriter(new FileWriter(System.getProperty("user.home")+"/GroupOn/"+username+"/"+friendUsername+".txt", true));
            bw.append(msg+"\n");
            bw.close();
            
        }
        else if(msg.equals("\\#%imp%#\\"))      // handling Logout
        {
            Messenger.isLogOutSuccessful=dis.readBoolean();
            
                break;
        }
        else if(msg.equals("\\#impR[]\\#imp"))      // handling refresh
        {
            SaveFile sf=new SaveFile();
            try {
                System.out.println("Waiting to recieve the two files");
                sf.saveFile(sock, System.getProperty("user.home")+"/GroupOn/", Streams.ois);
                System.out.println("Waiting to read size of second file");
                int sizeOfOnlineUsFile=Streams.dis.readInt();
                if(sizeOfOnlineUsFile > 0)
                {
                System.out.println("First file recieved, 2nd left");
                sf.saveFile(sock, System.getProperty("user.home")+"/GroupOn/", Streams.ois);
                System.out.println("Recieved both the files");
                }
                else
                {
                    new FileWriter(System.getProperty("user.home")+"/GroupOn/tempAllUserLog.txt");
                    System.out.println("Only One File Recieved");
                }
                Messenger.isRefreshFileTransComp=true;
            } catch (Exception ex) {
                System.out.println("Error in recieving both the files..");
                ex.printStackTrace();
            }
        }
        else if(msg.equals("\\#UsR[]\\#UsR"))       // handling chat logs
        {
            System.out.println("In the code of receiving chatLogs..Initiating file transfer");
            try
            {
            String chatLogPath = System.getProperty("user.home")+"/GroupOn/"+username+"/";
            System.out.println("Chat Log Path: "+chatLogPath);
            SaveFile sf = new SaveFile();
            sf.saveFile(sock, chatLogPath, Streams.ois);
            System.out.println("Recieved Chat logs");
            Messenger.isChatLogReceived=true;
                System.out.println("Self Validated "+ Messenger.isChatLogReceived);
            }
            catch(Exception e)
            {
                System.out.println("Error in Recieving Chat Files");
                e.printStackTrace();
            }
        }
        else if(msg.equals("$\\hula[]N[]hula\\$"))      // handling login Notifications
        {
            try
            {
            ArrayList<String> listClients=new ArrayList<>();
            BufferedReader br =new BufferedReader(new FileReader(System.getProperty("user.home")+"/GroupOn/allUserLog.txt"));
            String s="";
            while((s=br.readLine())!=null){
                listClients.add(s);
            }
            
            System.out.println("Waiting to read number of users..");
            int count=dis.readInt();
            System.out.println("Number of users recieved"+count);
            while(count!=0){
                String not=dis.readUTF().trim();
                
                if(listClients.contains(not)){
                    clientNotif(not);
                }
                else if(not.equals("\\#Add[@]#\\"))
                {
                    String usrNm=dis.readUTF();
                    count--;
                    String grpNm=dis.readUTF();
                    count--;
                    GroupReceive.addUserWhenOnlNotif(usrNm, grpNm);
                }
                else{
                    GroupReceive.addNotifWhenAdded(not);
                }
                
                count--;
            }
            System.out.println("Added Users to Notifiction Menu");
            Messenger.isLoginNotificationRec=true;
        }catch(Exception e)
        {
            System.out.println("Error in updating Notifications");
            e.printStackTrace();
        }
        }
        else if(msg.equals("\\Pro[$]Pro\\"))
        {
            System.out.println("In friends Profile, waitin to recieve");
            try
            {
                String name=Streams.dis.readUTF();
                System.out.println("Friend Name recieved: "+name);
                String frndUsnm=Streams.dis.readUTF();
                System.out.println("Friend UserName recieved: "+frndUsnm);
                String status=Streams.dis.readUTF();
                System.out.println("Recieved status: "+status);
                
                try{
                    System.out.println("Waiting to recieve Image");
                    String path=System.getProperty("user.home")+"/GroupOn/"+username+"/";
                    SaveFile sf=new SaveFile();
                    sf.saveFile(sock, path, Streams.ois);
                    System.out.println("Image File Recieved..");
                    System.out.println("Making Credentials of "+frndUsnm);
                    BufferedWriter br=new BufferedWriter(new FileWriter(System.getProperty("user.home")+"/GroupOn/"+username+"/"+frndUsnm+"CredLog.txt"));
                    br.append(frndUsnm+"\n");
                    br.append(name+"\n");
                    br.append(status+"\n");
                    br.close();
                    System.out.println(frndUsnm+" credentials created");
                }catch(Exception ex){
                    System.out.println("Error In Recieving Image");
                    ex.printStackTrace();
                }
                Messenger.isFriendProfileRec=Streams.dis.readBoolean();
                ProfilePage pg=new ProfilePage(username, name, status,frndUsnm );
                pg.setVisible(true);
                System.out.println("Profile Page opened...Proceeding Further");
     
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            Messenger.isFriendProfileRec=true;
        }
        
    }
    
    }catch(Exception e){
        e.printStackTrace();
    }
    }
    
  
    public void clientNotif(String not){
        JMenuItem menuItem = new JMenuItem(new AbstractAction("<html><body>Notifications from - <br><b>"+not+"</b></body></html>")
                {
                    @Override
                    public void actionPerformed(ActionEvent e) 
                    {
                       Messenger.userClicked(not+"-");
                       Messenger.notificationMenu.setText("<html><body>Notifications</body></html>");
                       Messenger.groupList.clearSelection();
                    }
                });
                Messenger.notificationMenu.add(menuItem);
                Messenger.notificationMenu.addSeparator();
                Messenger.notificationMenu.setText("<html><body><b>Notifications</b></body></html>");
    }
}
