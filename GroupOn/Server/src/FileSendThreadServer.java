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
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileSendThreadServer extends Thread {
	public final int BUFFER_SIZE = 1024;
	Socket sock;

	ArrayList<FileClient> list;
	DataOutputStream dout = null;
	DataInputStream dis = null;
	ObjectInputStream ois = null;
	ObjectOutputStream oos = null;

	FileSendThreadServer(Socket sock, ArrayList<FileClient> list) {
		this.sock = sock;

		this.list = list;
	}

	public void run() {
		try {

			dis = new DataInputStream(sock.getInputStream());
			dout = new DataOutputStream(sock.getOutputStream());
			oos = new ObjectOutputStream(sock.getOutputStream());
			ois = new ObjectInputStream(sock.getInputStream());
			System.out.println("Streams created in filesend");
			String userName = dis.readUTF();
			String str = null;
			BufferedReader br = null;
			while (true) {
				if (new File("/home/surbhit/GroupOn Data/User Data/" + userName + "/fileLog.txt").exists()) {
					if (str == null)
						br = new BufferedReader(
								new FileReader("/home/surbhit/GroupOn Data/User Data/" + userName + "/fileLog.txt"));
					str = br.readLine();
					if (str != null) {
						Thread.sleep(1000);
						try {
							dout.writeUTF("\\File[$]File\\");
							dout.writeUTF(str);
							System.out.println("-" + str);
							str = br.readLine();
							System.out.println("File path=" + str);
							System.out.println("File name:" + str.substring(str.lastIndexOf("/") + 1).trim());
							dout.writeUTF(str.substring(str.lastIndexOf("/") + 1).trim());
							System.out.println("sending file");
							sendFile(str, oos);
							System.out.println("File sent");
                                                        new File(str).delete();
						} catch (Exception ex) {
							System.out.println("In file send INSIDE catch");
							dis.close();
							dout.close();
							oos.close();
							ois.close();
							sock.close();
							break;
							// ex.printStackTrace();
						}

					}
					if (str == null) {

						BufferedWriter bw = new BufferedWriter(
								new FileWriter("/home/surbhit/GroupOn Data/User Data/" + userName + "/fileLog.txt"));
						//
					}
				}
				Thread.sleep(2000);
			}

		} catch (Exception ex) {
			try {
				System.out.println("In last of filesent catch");
				dis.close();
				dout.close();
				oos.close();
				ois.close();
				sock.close();
				ex.printStackTrace();
				return;
			} catch (Exception ex1) {
				ex1.printStackTrace();
			}
		}
	}

	private String saveFile(String path, ObjectInputStream ois) throws Exception {
		String path1 = "";
		FileOutputStream fos = null;
		byte[] buffer = new byte[BUFFER_SIZE];
		// 1. Read file name.
		Object o = ois.readObject();
		System.out.println("Path= " + o.toString());
		if (o instanceof String) {
			fos = new FileOutputStream(path + "/" + o.toString());
			path1 = path + "/" + o.toString();
		} else {
			throwException("Something is wrong");
		}
		// 2. Read file to the end.
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
			// 3. Write data to output file.
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
				System.out.println("These many bytes are send");
				oos.writeObject(Arrays.copyOf(buffer, buffer.length));
				System.out.println("Going for next iteration");
			}

			oos.flush();
			System.out.println("File sent");

		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

}
