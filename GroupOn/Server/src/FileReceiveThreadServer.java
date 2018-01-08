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

public class FileReceiveThreadServer extends Thread {
	public final int BUFFER_SIZE = 1024;
	ServerSocket fsock;
	String userName;

	DataOutputStream dout = null;
	DataInputStream dis = null;
	ObjectInputStream ois = null;
	ObjectOutputStream oos = null;
	String address;

	FileReceiveThreadServer(ServerSocket fsock, String userName) {
		this.fsock = fsock;
		this.userName = userName;
		address = System.getProperty("user.home");

	}

	public void run() {
		try {

			Socket sock = fsock.accept();
			System.out.println("Creating streams");
			dis = new DataInputStream(sock.getInputStream());
			dout = new DataOutputStream(sock.getOutputStream());
			oos = new ObjectOutputStream(sock.getOutputStream());
			ois = new ObjectInputStream(sock.getInputStream());
			System.out.println("Streams created");

			System.out.println("Reading in file thread");

			String friendName = dis.readUTF();
			String path = saveFile(friendName, ois);

			BufferedWriter bw = new BufferedWriter(
					new FileWriter(address + "/GroupOn Data/User Data/" + friendName + "/fileLog.txt", true));
			bw.append(userName + "\n");
			bw.append(path + "\n");
			bw.close();

			String fileName = path.substring(path.lastIndexOf("/") + 1).trim();

			BufferedWriter bwLogUser = new BufferedWriter(
					new FileWriter(address + "/GroupOn Data/User Data/" + userName + "/" + friendName + ".txt", true));
			BufferedWriter bwLogFriend = new BufferedWriter(
					new FileWriter(address + "/GroupOn Data/User Data/" + friendName + "/" + userName + ".txt", true));
			bwLogUser.append(userName + " : " + fileName + "\n");
			bwLogFriend.append(userName + " : " + fileName + "\n");
			bwLogFriend.close();
			bwLogUser.close();

			dis.close();
			dout.close();
			oos.close();
			ois.close();
			sock.close();

		} catch (Exception ex) {
			System.out.println("In last catch");
			ex.printStackTrace();
		}
	}

	private String saveFile(String username, ObjectInputStream ois) throws Exception {
		String path1 = "";

		FileOutputStream fos = null;
		byte[] buffer = new byte[BUFFER_SIZE];
		// Read file name.
		Object o = ois.readObject();
		System.out.println("Path= " + o.toString());
		if (o instanceof String) {
			fos = new FileOutputStream(address + "/GroupOn Data/User Data/" + username + "/" + o.toString());
			path1 = address + "/GroupOn Data/User Data/" + username + "/" + o.toString();
		} else {
			throwException("Something is wrong");
		}
		// Read file to the end.
		Integer bytesRead = 0;
		do {
			o = ois.readObject();
			if (!(o instanceof Integer)) {
				throwException("Something is wrong");
			}
			bytesRead = (Integer) o;
			o = ois.readObject();
			if (!(o instanceof byte[])) {
				throwException("Something is wrong");
			}
			buffer = (byte[]) o;
			// Write data to output file.
			fos.write(buffer, 0, bytesRead);
		} while (bytesRead == BUFFER_SIZE);
		System.out.println("File transfer success");
		fos.close();

		return path1;

	}

	public static void throwException(String message) throws Exception {
		throw new Exception(message);
	}

	public void sendFile(String path, ObjectOutputStream oos) {
		try {
			System.out.println("In send file");

			String file_name = path;
			System.out.println("Input path= " + path);
			File file = new File(file_name);

			System.out.println("Going to create output stream");

			System.out.println("output Stream Created");
			oos.writeObject(file.getName()); // reads the name of file
			System.out.println("Writing object by oos");
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[BUFFER_SIZE];
			Integer bytesRead = 0;
			System.out.println("Sending file");
			while ((bytesRead = fis.read(buffer)) > 0) {
				System.out.println("BytesRead = " + bytesRead);
				oos.writeObject(bytesRead);
				oos.writeObject(Arrays.copyOf(buffer, buffer.length));
			}

			oos.flush();
			System.out.println("File sent");

			// Code to upload the file to profile
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

}
