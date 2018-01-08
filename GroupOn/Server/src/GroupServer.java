/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author surbhit
 */
import java.io.*;
import java.util.*;
import java.net.*;

public class GroupServer extends Thread {
	public void run() {
		try {
			System.out.println("Listening for 6003");
			ServerSocket groupSock = new ServerSocket(6003);

			System.out.println("Creating file allGroupUserLog.txt");
			PrintWriter pwGroupUser = null;
			pwGroupUser = new PrintWriter(
					new BufferedWriter(new FileWriter("/home/surbhit/GroupOn Data/User Data/allGroupLog.txt", true)));
			pwGroupUser.println("No Groups Created");
			pwGroupUser.flush();
			System.out.println("File Created");

			ArrayList<GroupClient> list = new ArrayList<GroupClient>();
			while (true) {
				System.err.println("Waiting for client");
				Socket gsock = groupSock.accept();
				System.err.println("Client connected");
				GroupClient grpObj = new GroupClient("", null, null, null, null);

				list.add(grpObj);
				GroupClientThread gct = new GroupClientThread(gsock, pwGroupUser, grpObj, list);
				System.out.println("Going to start thread");
				gct.start();
				System.out.println("Going back to starting of while loop");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
