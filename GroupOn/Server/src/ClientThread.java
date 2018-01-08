/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author aks and surbhit
 */
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientThread extends Thread {
	public final int BUFFER_SIZE = 1024;
	ArrayList<Client> list;
	Socket sock;
	ServerSocket fsock;
	Client clObj;
	PrintWriter pw;
	DataOutputStream dout = null;
	DataInputStream dis = null;
	ObjectInputStream ois = null;
	ObjectOutputStream oos = null;
	String friendUserName;
	String address;

	public ClientThread(Socket s, Client clObj, ArrayList<Client> List, PrintWriter pw, ServerSocket fsock) {
		this.sock = s;
		this.list = List;
		this.clObj = clObj;
		this.pw = pw;
		this.fsock = fsock;
		address = System.getProperty("user.home");

	}

	public void run() {
		try {
			int f = 0;
			try {
				oos = new ObjectOutputStream(sock.getOutputStream());
				ois = new ObjectInputStream(sock.getInputStream());
				dis = new DataInputStream(sock.getInputStream());
				dout = new DataOutputStream(sock.getOutputStream());
				System.out.println(sock.getLocalPort());
				clObj.dout = dout;
				clObj.dis = dis;
				clObj.oos = oos;
				clObj.ois = ois;
				String username = "";
				for (int i = 1; i <= 2; i++) {
					int flag = clObj.dis.readInt();
					System.out.println("flag= " + flag);
					switch (flag) {
					case 0:// for login
					{
						System.out.println("In login");
						boolean validated = validateLogin(sock);
						if (validated) {

							clObj.dout.writeBoolean(validated);

						} else {
							clObj.dout.writeBoolean(validated);
							synchronized (this) {
								list.remove(clObj);
								clObj.dis.close();
								clObj.dout.close();
								clObj.oos.close();
								clObj.ois.close();
								dis.close();
								dout.close();
								oos.close();
								ois.close();
								sock.close();
								return;
							}
						}
						f = 1;
					}
						break;
					case 1:// for signup button
					{

						System.out.println("In sign up");
						String name, password;
						name = clObj.dis.readUTF();
						username = clObj.dis.readUTF();
						password = clObj.dis.readUTF();
						clObj.userName = username;

						System.out.println("Came to sql starting");
						Statement stm = connect();
						System.out.println("initialized stm");
						String query = "select username from login where username='" + username + "';";
						String query1 = "select groupname from grp where groupname='" + username + "';";
						ResultSet rs = stm.executeQuery(query);
						System.out.println("Got result");
						Boolean sqlHandler = false;
						if (rs.next()) {
							sqlHandler = true;
						}
						ResultSet rsg = stm.executeQuery(query1);
						if (sqlHandler || rsg.next()) {
							System.out.println("username unavailable so sending false");
							clObj.dout.writeBoolean(false);
							synchronized (this) {
								list.remove(clObj);
								clObj.dis.close();
								clObj.dout.close();
								clObj.oos.close();
								clObj.ois.close();
								dis.close();
								dout.close();
								oos.close();
								ois.close();
								sock.close();
								return;
							}
						} else {
							System.out.println("Everthing okay sending true");
							clObj.dout.writeBoolean(true);
							String query2 = "insert into login values('" + name + "','" + username + "','" + password
									+ "','','');";
							System.out.println("Updating db");
							stm.executeUpdate(query2);
							System.out.println("Updated db");
							System.out.println("Creating notificationLog.txt");
							new File(address + "/GroupOn Data/User Data/" + username).mkdir();
							pw.println(username);
							pw.flush();
							FileWriter fw = new FileWriter(
									address + "/GroupOn Data/User Data/" + username + "/notificationLog.txt");
							fw.close();
							System.out.println("File Created");

						}

					}
						break;
					case 2:// for image upload button
					{
						String status;
						status = clObj.dis.readUTF();
						System.out.println("In upload");
						System.out.println("status: " + status);

						String path = saveFile(sock, username, clObj.oos, clObj.ois);
						Statement stm = connect();
						String query = "update login set imagePath='" + path + "' , status='" + status
								+ "' where username='" + username + "';";

						stm.executeUpdate(query);

						System.out.println("is socket closed of client in upload: " + sock.isClosed());
					}
						break;
					case 3:// for skip button
					{
						String status;
						System.out.println("In skip");
						status = clObj.dis.readUTF();

						Statement stm = connect();
						String query = "update login set imagePath='" + address
								+ "/GroupOn Data/Binary Warriors/defaultPP.png' where username='" + username + "';";
						String query1 = "update login set status='" + status + "' where username='" + username + "';";
						stm.executeUpdate(query);
						stm.executeUpdate(query1);
						System.out.println("in Skip db updated");
						System.out.println("is socket closed in skip: " + sock.isClosed());
					}
						break;
					}
					if (f == 1) {
						break;
					}
				}
			}

			catch (Exception ex) {
				ex.printStackTrace();
			}

			System.out.println("outside loop");

			while (true) {
				System.out.println("Inside msg loop");
				try {
					System.out.println("Trying to get msg");
					String msg = clObj.userName + " : " + clObj.dis.readUTF(); // Throws IOex when Client closes socket
					System.out.println("Wait is over got the msg: " + msg);
					System.out.println("socket port= " + sock.getPort());
					synchronized (this) {
						// Handle Refresh
						if (msg.equals(clObj.userName + " : " + "\\#impR[]\\#imp")) {
							System.out.println("socket port in refresh area= " + sock.getPort());
							clObj.dout.writeUTF("\\#impR[]\\#imp");

							sendFile(sock, address + "/GroupOn Data/User Data/allUserLog.txt", clObj.oos, clObj.ois);
							System.out.println("allUserLog.txt has been sent");
							PrintWriter writerForTemp = new PrintWriter(new BufferedWriter(
									new FileWriter(address + "/GroupOn Data/User Data/tempAllUserLog.txt")));
							System.out.println("temporary file of allUserLog.txt created but not initialized");
							for (Client i : list) {
								writerForTemp.println(i.userName);
							}
							System.out.println("Closing stream to make data enter from buffer");
							writerForTemp.flush();
							writerForTemp.close();
							System.out.println("tempAllUserLog.txt created");
							if (new File(address + "/GroupOn Data/User Data/tempAllUserLog.txt").length() == 0) {
								System.out.println("File size is 0 that is no new user");
								clObj.dout.writeInt(0);
							} else {
								System.out.println("File size is not 0 sending new tempAllUserLog.txt file");
								clObj.dout
										.writeInt((int) new File(address + "/GroupOn Data/User Data/tempAllUserLog.txt")
												.length());
								sendFile(sock, address + "/GroupOn Data/User Data/tempAllUserLog.txt", clObj.oos,
										clObj.ois);
							}
							System.out.println("File Sent: going out of refresh");

						}
						// handle logout
						else if (msg.equals(clObj.userName + " : " + "\\#%imp%#\\")) {
							System.out.println("socket port in logout area= " + sock.getPort());
							if (list.remove(clObj)) {
								clObj.dout.writeUTF("\\#%imp%#\\");
								clObj.dout.writeBoolean(true);
								clObj.dis.close();
								clObj.dout.close();
								clObj.ois.close();
								clObj.oos.close();
								dout.close();
								dis.close();
								ois.close();
								oos.close();
								sock.close();
								System.out.println("Is socket Closed in logout: " + sock.isClosed());
								break;
							} else {
								clObj.dout.writeUTF("\\#%imp%#\\");
								clObj.dout.writeBoolean(false);
							}
							System.out.println("going out of logout");
						}
						// Handle friend name clicked
						else if (msg.equals(clObj.userName + " : " + "\\#UsR[]\\#UsR")) {
							int flag = 0;
							System.out.println("socket port in notificaton area= " + sock.getPort());
							friendUserName = clObj.dis.readUTF();
							System.out.println("Got username = " + friendUserName);
							for (Client i : list) {
								if (i.userName.equals(friendUserName)) {
									flag = 1;
									System.out.println(friendUserName + " is Online");

									File chatLog = new File(address + "/GroupOn Data/User Data/" + clObj.userName + "/"
											+ friendUserName + ".txt");
									System.out.println("" + chatLog.exists());
									if (chatLog.exists()) {
										clObj.dout.writeUTF("\\#UsR[]\\#UsR");
										System.out.println("Chat log for" + friendUserName + " found");
										sendFile(sock, address + "/GroupOn Data/User Data/" + clObj.userName + "/"
												+ friendUserName + ".txt", clObj.oos, clObj.ois);
										System.out.println("Chat log for" + friendUserName + " has been sent");
									} else {
										clObj.dout.writeUTF("\\#UsR[]\\#UsR");
										System.out.println("Making file");
										BufferedWriter bw = new BufferedWriter(
												new FileWriter(address + "/GroupOn Data/User Data/" + clObj.userName
														+ "/" + friendUserName + ".txt", true));
										bw.append("Chat between " + clObj.userName + " and " + friendUserName + "\n");
										bw.close();
										System.out.println("File Making done");
										sendFile(sock, address + "/GroupOn Data/User Data/" + clObj.userName + "/"
												+ friendUserName + ".txt", clObj.oos, clObj.ois);
										System.out.println("File Sent");
									}
									break;
								}
							}
							if (flag == 0) {
								// code for friend offline
								System.out.println(friendUserName + " is offline");
								File chatLog = new File(address + "/GroupOn Data/User Data/" + clObj.userName + "/"
										+ friendUserName + ".txt");
								System.out.println("" + chatLog.exists());
								if (chatLog.exists()) {
									clObj.dout.writeUTF("\\#UsR[]\\#UsR");
									System.out.println("Chat log for" + friendUserName + " found");
									sendFile(sock, address + "/GroupOn Data/User Data/" + clObj.userName + "/"
											+ friendUserName + ".txt", clObj.oos, clObj.ois);
									System.out.println("Chat log for" + friendUserName + " has been sent");
								} else {
									clObj.dout.writeUTF("\\#UsR[]\\#UsR");
									System.out.println("Making file");
									BufferedWriter bw = new BufferedWriter(
											new FileWriter(address + "/GroupOn Data/User Data/" + clObj.userName + "/"
													+ friendUserName + ".txt", true));
									bw.append("Chat between " + clObj.userName + " and " + friendUserName + "\n");
									bw.close();
									System.out.println("File Making done");
									sendFile(sock, address + "/GroupOn Data/User Data/" + clObj.userName + "/"
											+ friendUserName + ".txt", clObj.oos, clObj.ois);
									System.out.println("File Sent");
								}

							}

						}
						// code for login notification
						else if (msg.equals(clObj.userName + " : " + "$\\hula[]N[]hula\\$")) {
							clObj.dout.writeUTF("$\\hula[]N[]hula\\$");
							BufferedReader br = new BufferedReader(new FileReader(
									address + "/GroupOn Data/User Data/" + clObj.userName + "/notificationLog.txt"));
							int c = 0;
							String usr;
							System.out.println("Counting number of people");
							while ((usr = br.readLine()) != null) {
								c++;
							}

							clObj.dout.writeInt(c);
							br = new BufferedReader(new FileReader(
									address + "/GroupOn Data/User Data/" + clObj.userName + "/notificationLog.txt"));
							while ((usr = br.readLine()) != null) {
								clObj.dout.writeUTF(usr);
							}
							br.close();
							FileWriter fw = new FileWriter(
									address + "/GroupOn Data/User Data/" + clObj.userName + "/notificationLog.txt");
							fw.close();
						}
					}
					System.out.println("Came out of synchronized area");
					if (msg.equals(clObj.userName + " : " + "\\File[$]File\\")) {
						System.out.println("Received file flag");
						FileReceiveThreadServer frts = new FileReceiveThreadServer(fsock, clObj.userName);
						frts.start();
						System.out.println("FileReceiveThread started");
					}
					// Handle user profile
					if (msg.equals(clObj.userName + " : " + "\\Pro[$]Pro\\")) {
						System.out.println("Inside userPtofileAction");
						String user = clObj.dis.readUTF();
						System.out.println("data will be sent of; " + user);
						clObj.dout.writeUTF("\\Pro[$]Pro\\");
						String query1 = "select name, username, imagePath, status from login where username='" + user
								+ "';";
						Statement stm = connect();
						ResultSet rs = stm.executeQuery(query1);

						String name = "", path = "", status = "";
						while (rs.next()) {
							name = rs.getString(1);

							path = rs.getString(3);
							status = rs.getString(4);
						}

						clObj.dout.writeUTF(name);
						System.out.println("Name sent");
						clObj.dout.writeUTF(user);
						System.out.println("username sent");
						clObj.dout.writeUTF(status);
						System.out.println("status sent");
						sendFile(sock, path, clObj.oos, clObj.ois);
						System.out.println("Sending File send confirmation");
						clObj.dout.writeBoolean(true);
						System.out.println("File sent ie. File sent confirmation sent");
					}

					if (!(msg.equals(clObj.userName + " : " + "\\#%imp%#\\")
							|| msg.equals(clObj.userName + " : " + "\\#impR[]\\#imp")
							|| msg.equals(clObj.userName + " : " + "$\\hula[]N[]hula\\$")
							|| msg.equals(clObj.userName + " : " + "\\Pro[$]Pro\\")
							|| msg.equals(clObj.userName + " : " + "\\File[$]File\\")
							|| msg.equals(clObj.userName + " : " + "\\#UsR[]\\#UsR"))) {
						System.out.println("Inside msg printing loop");
						int flagIsOnline = 0;
						for (Client i : list) {
							// username is online
							if (i.userName.equals(friendUserName)) {
								BufferedWriter bw = new BufferedWriter(
										new FileWriter(address + "/GroupOn Data/User Data/" + clObj.userName + "/"
												+ friendUserName + ".txt", true));
								bw.append(msg + "\n");
								bw.flush();
								bw.close();

								System.out.println("Updated both log now sending msg");
								try {
									i.dout.writeUTF(msg);
									flagIsOnline = 1;
								} catch (Exception ex) {

									System.out.println(friendUserName + " socket is " + i.sock.isClosed());
									flagIsOnline = 0;
									BufferedReader br = new BufferedReader(new FileReader(address
											+ "/GroupOn Data/User Data/" + friendUserName + "/notificationLog.txt"));
									String str;
									int flag = 0;
									while ((str = br.readLine()) != null) {
										if (str.equals(clObj.userName)) {
											flag = 1;
											break;
										}
									}
									if (flag == 0) {
										bw = new BufferedWriter(new FileWriter(address + "/GroupOn Data/User Data/"
												+ friendUserName + "/notificationLog.txt", true));
										bw.append(clObj.userName + "\n");
										bw.flush();
										bw.close();
									}

								}
								bw = new BufferedWriter(new FileWriter(address + "/GroupOn Data/User Data/"
										+ friendUserName + "/" + clObj.userName + ".txt", true));
								bw.append(msg + "\n");
								bw.flush();
								bw.close();
								System.out.println("msg send" + msg);

							}

						}
						if (flagIsOnline == 0) {
							BufferedWriter bw = new BufferedWriter(new FileWriter(address + "/GroupOn Data/User Data/"
									+ clObj.userName + "/" + friendUserName + ".txt", true));
							bw.append(msg + "\n");
							bw.flush();
							bw.close();
							bw = new BufferedWriter(new FileWriter(address + "/GroupOn Data/User Data/" + friendUserName
									+ "/" + clObj.userName + ".txt", true));
							bw.append(msg + "\n");
							bw.flush();
							bw.close();
							System.out.println("Updated both log now updating friend login file");
							BufferedReader br = new BufferedReader(new FileReader(
									address + "/GroupOn Data/User Data/" + friendUserName + "/notificationLog.txt"));
							String str;
							int flag = 0;
							while ((str = br.readLine()) != null) {
								if (str.equals(clObj.userName)) {
									flag = 1;
									break;
								}
							}
							if (flag == 0) {
								bw = new BufferedWriter(new FileWriter(
										address + "/GroupOn Data/User Data/" + friendUserName + "/notificationLog.txt",
										true));
								bw.append(clObj.userName + "\n");
								bw.flush();
								bw.close();
							}
						}
					}
				} catch (Exception e) {
					System.out.println("Is socket Closed: " + sock.isClosed());
					try {
						System.out.println("Inside biggest catch");
						list.remove(clObj);
						clObj.dis.close();
						clObj.dout.close();
						clObj.ois.close();
						clObj.oos.close();
						dout.close();
						dis.close();
						ois.close();
						oos.close();
						sock.close();
						return;
					} catch (IOException ex1) {
						System.out.println("Inside biggest catch");
						ex1.printStackTrace();
					}
					e.printStackTrace();
				}
			}

		} catch (Exception ex) {
			try {
				System.out.println("Inside biggest catch");
				list.remove(clObj);
				clObj.dis.close();
				clObj.dout.close();
				clObj.ois.close();
				clObj.oos.close();
				dout.close();
				dis.close();
				ois.close();
				oos.close();
				sock.close();
				return;
			} catch (IOException ex1) {
				System.out.println("Inside biggest catch");
				ex.printStackTrace();
			}
		}
	}

	public boolean validateLogin(Socket sock) throws SQLException, IOException {
		boolean val = false;
		DataInputStream din = new DataInputStream(sock.getInputStream());
		String username = din.readUTF();
		String pass = din.readUTF();
		clObj.userName = username;
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/softa", "surbhit", "awasthi@7");
		Statement stm = conn.createStatement();
		String query = "select password from login where username='" + username + "';";
		ResultSet rs = stm.executeQuery(query);

		if (rs.next()) {
			rs.first();
			String p = rs.getString(1);
			if (p.equals(pass))
				val = true;
		}

		return val;
	}

	Statement connect() {
		Statement stm = null;
		try {
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/softa", "surbhit", "awasthi@7");
			stm = conn.createStatement();

		} catch (SQLException ex) {
			Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
		}
		return stm;
	}

	private String saveFile(Socket socket, String username, ObjectOutputStream oos, ObjectInputStream ois)
			throws Exception {
		String path = "";

		FileOutputStream fos = null;
		byte[] buffer = new byte[BUFFER_SIZE];
		// Read file name.
		Object o = ois.readObject();
		System.out.println("Path= " + o.toString());
		if (o instanceof String) {
			fos = new FileOutputStream(address + "/GroupOn Data/User Data/" + username + "/" + o.toString());
			path = address + "/GroupOn Data/User Data/" + username + "/" + o.toString();
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

		return path;

	}

	public static void throwException(String message) throws Exception {
		throw new Exception(message);
	}

	public void sendFile(Socket sock, String path, ObjectOutputStream oos, ObjectInputStream ois) {
		// Code to upload the file to profile
		try {
			System.out.println("In send file");

			String file_name = path;
			System.out.println("Inpyt path= " + path);
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

		} 
		catch (IOException ex) {
			ex.printStackTrace();
		}

	}
}