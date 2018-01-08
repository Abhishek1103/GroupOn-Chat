/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package groupon;

//import static groupon.Welcome.BUFFER_SIZE;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author aks
 */
public class SaveFile {
//    Socket sock;
//    SaveFile(Socket sock){
//        this.sock=sock;
//    }
    public static final int BUFFER_SIZE=1024;
    public void saveFile(Socket socket, String path, ObjectInputStream ois) throws Exception {   // 
       
        System.out.println("in saveFile Function...");
        FileOutputStream fos = null;
        
        byte [] buffer = new byte[BUFFER_SIZE];
        // 1. Read file name.
        System.out.println("Waiting to read name of file through  Object o =ois.readObject();");
        Object o = ois.readObject();
        System.out.println("Reading from ObjectInputstream completed(Path read)");
        System.out.println("Path= "+o.toString());
        if (o instanceof String) 
        {
            fos = new FileOutputStream(path+o.toString());
            System.out.println("Path from saveFile Function: "+path+o.toString());
            
        } else
        {
            throwException("Something is wrong");
        }
        // 2. Read file to the end.
        Integer bytesRead = 0;
        System.out.println("Going into File reading loop...");
        int c=0;
        do
        {   
            System.out.println("File recieve iteration = "+c++);
            o = ois.readObject();
            if (!(o instanceof Integer))
            {
                throwException("Something is wrong");
            }
            System.out.println("o = ois.readObject(); THIS LINE WORKS");
            bytesRead = (Integer)o;
            System.out.println("bytesRead = "+bytesRead);
            o = ois.readObject();
            
            if (!(o instanceof byte[]))
            {
                throwException("Something is wrong");
            }
            buffer = (byte[])o;
            // 3. Write data to output file.
            System.out.println("Ready to write file");
            fos.write(buffer, 0, bytesRead);
            System.out.println("Writing file");
        } while (bytesRead == BUFFER_SIZE);
        System.out.println("File transfer success");
        fos.close();
    
    }
    public static void throwException(String message) throws Exception {
        throw new Exception(message);
    }
}
