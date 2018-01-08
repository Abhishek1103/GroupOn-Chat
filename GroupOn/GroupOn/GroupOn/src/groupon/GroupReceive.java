/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this  template file, choose Tools | Templates
 * and open the template in the editor.
 */
package groupon;

/**
 *
 * @author aks
 */
import static groupon.Messenger.isGroupMemRec;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.text.StyledDocument;

public class GroupReceive extends Thread
{
    Socket sock =null;
    String username="";
    public static boolean bool=false;
    
    public GroupReceive(Socket sock, String username)       
    { 
        this.sock=sock;
        this.username=username;
    }
    
    public void run()
    {
        DataInputStream dis=GroupStreams.dis;
        System.out.println("Group Recieve is running");
        StyledDocument sdoc;
        
        
        while(true)
        {
            try
            {
                System.out.println("Waiting for message in Group...");
                String msg=dis.readUTF();
                System.out.println("Message Recieved(in grup Thread): "+msg);
                
                if(!(msg.equals("\\#%imp%#\\") || msg.equals("\\#Grp[@]Grp\\#") || msg.equals("\\#impR[]\\#imp") || msg.equals("\\#UsR[]\\#UsR") || msg.equals("\\Pro[$]Pro\\") || msg.equals("\\#Add[@]#\\") || msg.equals("$\\hula[]N[]hula\\$")))
                {
                    // code to handle normal group messages
                    String grpName=msg.substring(0 , msg.indexOf(":")).trim();
                    String msgDisplay=msg.substring(msg.indexOf(":")+2, msg.length()).trim();
                    String grpSelected=Messenger.groupList.getSelectedValue();
                    
                    if(grpName.equals(grpSelected))
                    {
                        // Code to display msg when grpNname == grpSelected
                        sdoc=Messenger.displayTxtPane.getStyledDocument();
                        sdoc.insertString(sdoc.getLength(), msgDisplay+"\n", null);
                        System.out.println("TextPane display "+msgDisplay);
                    }
                    else
                    {
                        // Code to send notification when grpName != grpSelected
                        addNotifWhenAdded(grpName);
                    }
                }
                else if(msg.equals("\\#%imp%#\\"))
                {
                      // code for logout
                    System.out.println("Waiting for group Logout validation..");
                    Messenger.isGrpLogOutSuccessful=dis.readBoolean();
                    break;
                }
                else if(msg.equals("\\#Grp[@]Grp\\#"))
                {
                    // Creation of new Group
                    System.out.println("Waiting for Validation..");
                    NewGrupWind.isGrpValid=dis.readBoolean();
                    System.out.println("Validation recieved ");
                    NewGrupWind.isGrpValidRec=true;
                }
                else if(msg.equals("\\#impR[]\\#imp"))  // Handling refresh
                {
                    try
                    {
                        System.out.println("Waiting to recieve file in GroupReceive..");
                        SaveFile sf=new SaveFile();
                        sf.saveFile(sock, System.getProperty("user.home")+"/GroupOn/", GroupStreams.ois);
                        System.out.println("allGroupLog.txt file received..Now waiting for inGroups file");
                        int size=dis.readInt();
                        System.out.println("Size of inGroupsLog = "+ size);
                        if(size > 0)
                        {
                            sf.saveFile(sock, System.getProperty("user.home")+"/GroupOn/"+username+"/", GroupStreams.ois);
                            System.out.println("inGroupsLog.txt received");
                        }
                        else
                        {
                            System.out.println("File was empty..hence creating an empty file");
                            new FileWriter(System.getProperty("user.home")+"/GroupOn/"+username+"/inGroupsLog.txt");
                        }
                        Messenger.isGrpRefreshFileTransComp=dis.readBoolean();
                        System.out.println("Validation Received..Going Out of refresh");
                    }
                    catch(Exception ex)
                    {
                        System.out.println("Error in GroupRefresh..");
                        ex.printStackTrace();
                    }
                }
                else if(msg.equals("\\#UsR[]\\#UsR"))
                {
                    // code to handle click on grupList
                    System.out.println("waiting to receive grpchatLogs");
                    String path=System.getProperty("user.home")+"/GroupOn/"+username+"/";
                    //System.out.println("path");
                    bool=dis.readBoolean();
                    if(bool)
                    {
                        SaveFile sf=new SaveFile();
                        sf.saveFile(sock, path, GroupStreams.ois);
                        System.out.println("File received ");
                        Messenger.isGrpChatLogRec=dis.readBoolean();
                    }
                    else
                    {
                        Messenger.isGrpChatLogRec=true;
                    }
                    System.out.println("validating "+Messenger.isGrpChatLogRec);
                }
                else if(msg.equals("\\Pro[$]Pro\\"))
                {
                    // Code to display members of a group...
                    System.out.println("In group receive for Displaying members");
                    System.out.println("Waiting for file (in grpRec)");
                    String path=System.getProperty("user.home")+"/GroupOn/";
                    SaveFile sf=new SaveFile();
                    sf.saveFile(sock, path, GroupStreams.ois);
                    System.out.println("File came(in grpRec)");
                    Messenger.isGroupMemRec=true;
                    System.out.println("Self Validated "+Messenger.isGroupMemRec);
                }
                else if(msg.equals("\\#Add[@]#\\"))
                {
                    // Code to add Users to existing group
                    Messenger.isAdmin=dis.readBoolean();
                    Messenger.isAddFlag=true;
                }
                else if(msg.equals("\\#Add[@]Add#\\"))
                {
                    
                }
                else if(msg.equals("$\\hula[]N[]hula\\$"))
                {       // Code to handle Notifications(Adding users) when online
                    String flag=dis.readUTF();
                    if(!(flag.equals("\\grp\\")))
                    {
                        String grp=dis.readUTF();
                        addNotifWhenAdded(grp);
                    }
                    else
                    {
                        String usrName=dis.readUTF();
                        String grp=dis.readUTF();
                        addUserWhenOnlNotif(usrName, grp);
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
   
    
    
    protected static void addNotifWhenAdded(String grp){
        JMenuItem menuItem = new JMenuItem(new AbstractAction("<html><body>Notifications from- <br><b>"+grp+"</b></body></html>")
                    {
                        @Override
                        public void actionPerformed(ActionEvent e) 
                        {
                            try 
                            {
                                Messenger.grpClicked(grp);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        Messenger.notificationMenu.setText("<html><body>Notifications</body></html>");
                        Messenger.groupList.setSelectedValue(grp, true);
                        Messenger.contactList.clearSelection();
                        //Messenger.notifications.remove(menuItem);
                        //Messenger.notificationMenu.remove();        //Try to find the solution...
                        }
                    });
                    Messenger.notificationMenu.add(menuItem);
                    Messenger.notificationMenu.addSeparator();
                    Messenger.notificationMenu.setText("<html><body><b>Notifications</b></body></html>");
    }
    
    protected static void addUserWhenOnlNotif(String usrName, String grpName){
        JMenuItem menuItem = new JMenuItem(new AbstractAction("<html><body><b>"+usrName+"</b> wants to join <b>"+grpName+"</b></body></html>")
                    {
                        @Override
                        public void actionPerformed(ActionEvent e) 
                        {

                        }
                    });
                    Messenger.notificationMenu.add(menuItem);
                    Messenger.notificationMenu.addSeparator();
                    Messenger.notificationMenu.setText("<html><body><b>Notifications</b></body></html>");
    }
}
