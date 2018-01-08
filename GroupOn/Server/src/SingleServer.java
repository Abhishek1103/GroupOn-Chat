/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aks
 */
public class SingleServer extends Thread {
	public void run()

	{
		try {

			Statement stm = connect();
			String query = "insert into login values('','Binary Warriors','','','');";
			String query1 = "insert into grp values('');";
			stm.executeUpdate(query);
			stm.executeUpdate(query1);
			PrintWriter pwSingleUser = null;

			ServerSocket ssock = new ServerSocket(5003);
			ServerSocket fsock = new ServerSocket(8003);

			try {
				pwSingleUser = new PrintWriter(
						new BufferedWriter(new FileWriter("/home/surbhit/GroupOn Data/User Data/allUserLog.txt")));

			} catch (IOException ex) {
				System.err.println("Something went wrong");
			}

			ArrayList<Client> list = new ArrayList<Client>();

			while (true) {
				System.out.println("Started Listening");
				Socket socket = ssock.accept();

				Client clObj = new Client(socket, null, null, null, null, null);
				list.add(clObj);

				ClientThread clThrd = new ClientThread(socket, clObj, list, pwSingleUser, fsock);

				clThrd.start();

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	static Statement connect() {
		Statement stm = null;
		try {
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/softa", "surbhit", "awasthi@7");
			stm = conn.createStatement();

		} catch (SQLException ex) {
			Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
		}
		return stm;
	}
}
