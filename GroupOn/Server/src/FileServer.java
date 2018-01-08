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

public class FileServer extends Thread {
	static ArrayList<FileClient> fileClientList;

	public void run() {
		try {
			System.out.println("file @ 7003");
			ServerSocket groupSock = new ServerSocket(7003);
			System.out.println("Creating file allGroupUserLog.txt");

			fileClientList = new ArrayList<FileClient>();
			while (true) {
				System.err.println("Waiting for client in file");
				Socket fsock = groupSock.accept();
				System.err.println("Client connected in file");
				FileClient fileObj = new FileClient("", null, null, null, null);

				fileClientList.add(fileObj);
				FileSendThreadServer ft = new FileSendThreadServer(fsock, fileClientList);
				System.out.println("Going to start thread in file");
				ft.start();
				System.out.println("Going back to starting of while loop in file");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
