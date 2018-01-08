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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GroupClientThread extends Thread {
	DataOutputStream dout = null;
	DataInputStream dis = null;
	ObjectInputStream ois = null;
	ObjectOutputStream oos = null;
	String userName = "";
	Socket sock;
	PrintWriter pw;
	GroupClient grpObj;
	String address;
	ArrayList<GroupClient> list;
	public final int BUFFER_SIZE = 1024;

	GroupClientThread(Socket sock, PrintWriter pw, GroupClient grpObj, ArrayList<GroupClient> list) {
		this.sock = sock;
		this.pw = pw;
		this.grpObj = grpObj;
		this.list = list;
		address = System.getProperty("user.home");
		try {
			System.out.println("Stream creation started");
			dout = new DataOutputStream(sock.getOutputStream());
			System.err.println("dout created");
			dis = new DataInputStream(sock.getInputStream());
			System.err.println("dis created");
			oos = new ObjectOutputStream(sock.getOutputStream());
			System.err.println("oos created");
			ois = new ObjectInputStream(sock.getInputStream());
			System.err.println("ois created");

			System.err.println("Streams creation ended");
		} catch (Exception ex) {
			System.out.println("Unable to construct streams");
			ex.printStackTrace();
		}
	}

	public void run() {
		System.out.println("Reading username");
		try {
			userName = dis.readUTF();
			grpObj.userName = userName;
			grpObj.dis = dis;
			grpObj.dout = dout;
			grpObj.oos = oos;
			grpObj.ois = ois;
			BufferedWriter bw = new BufferedWriter(
					new FileWriter(address + "/GroupOn Data/User Data/" + userName + "/inGroupsLog.txt", true));
			while (true) {
				System.out.println("Reading message....");
				String msg = userName + " : " + dis.readUTF();
				System.out.println("msg = " + msg);
				// handle new group
				if (msg.equals(userName + " : " + "\\#Grp[@]Grp\\#")) {

					String groupName = dis.readUTF();
					System.out.println("group name is : " + groupName);
					// check for validity
					System.out.println("Sending flag" + "\\#Grp[@]Grp\\#");
					dout.writeUTF("\\#Grp[@]Grp\\#");
					Boolean isValid = validGroup(groupName);
					System.out.println(isValid);
					if (isValid) {
						pw.append(groupName + "\n");
						pw.flush();
						dout.writeBoolean(true);
						bw.append(groupName + "\n");
						bw.flush();
						System.out.println("group name written on" + userName + " ingrouplog file");
						int totalUser = dis.readInt();
						System.out.println("Creating folder");
						new File(address + "/GroupOn Data/Group Data/" + groupName).mkdir();
						System.out.println("Created folder");

						BufferedWriter bwChatLog = new BufferedWriter(new FileWriter(
								address + "/GroupOn Data/Group Data/" + groupName + "/" + groupName + "ChatLog.txt",
								true));
						bwChatLog.append("Group Chat of : " + groupName + "\n");
						bwChatLog.close();
						System.out.println("Default chatlog created");
						BufferedWriter bwMembers = new BufferedWriter(new FileWriter(
								address + "/GroupOn Data/Group Data/" + groupName + "/" + groupName + "allMembers.txt",
								true));
						bwMembers.append("Admin : " + userName + "\n");
						bwMembers.flush();
						System.out.println("member list admin added");
						for (int i = 0; i < totalUser; i++) {
							String frndUser = dis.readUTF();
							System.out.println(frndUser + " selected");
							BufferedWriter bwFriendList = new BufferedWriter(new FileWriter(
									address + "/GroupOn Data/User Data/" + frndUser + "/inGroupsLog.txt", true));
							bwFriendList.append(groupName + "\n");
							bwFriendList.close();
							System.out.println("Updated his ingrouplog.txt");
							bwMembers.append(frndUser + "\n");
							bwMembers.flush();
							System.out.println("Added him to member file");
							if (isOffline(frndUser)) {
								System.out.println(frndUser + " is offline");
								BufferedWriter bwNotification = new BufferedWriter(new FileWriter(
										address + "/GroupOn Data/User Data/" + frndUser + "/notificationLog.txt",
										true));
								bwNotification.append(groupName + "\n");
								bwNotification.close();
								System.out.println("Writing notification file of " + frndUser);
							} else {
								System.out.println(frndUser + " is online");
								System.out.println("Printing online group arraylist");
								for (GroupClient p : list) {
									System.err.println("" + p.userName);
								}

								System.out.println("List printed");
								for (Iterator<GroupClient> j = list.iterator(); j.hasNext();) {
									GroupClient gObj;
									synchronized (this) {
										gObj = j.next();
									}
									if (gObj.userName.equals(frndUser)) {
										gObj.dout.writeUTF("$\\hula[]N[]hula\\$");
										gObj.dout.writeUTF("my");
										gObj.dout.writeUTF(groupName);
										break;
									}
								}
								System.out.println("Sent groupname as notification");
							}
						}
						bwMembers.close();
					} else {
						System.out.println("sending false");
						dout.writeBoolean(false);
					}

					System.out.println("Going out of group craetion");

				}
				// handle refresh
				else if (msg.equals(userName + " : " + "\\#impR[]\\#imp")) {

					System.out.println("Writing back flag");
					dout.writeUTF("\\#impR[]\\#imp");
					System.out.println("socket port in refresh area= " + sock.getPort());

					sendFile(address + "/GroupOn Data/User Data/allGroupLog.txt");
					System.out.println("allGroupLog.txt has been sent");

					if (new File(address + "/GroupOn Data/User Data/" + userName + "/inGroupsLog.txt").length() != 0) {
						dout.writeInt(4);
						sendFile(address + "/GroupOn Data/User Data/" + userName + "/inGroupsLog.txt");
						System.out.println("inGroupsLog.txt sent");
					} else {
						dout.writeInt(0);
						System.out.println("File empty for this user");
					}
					System.out.println("sending confirmation flag");
					dout.writeBoolean(true);
					System.out.println("sent with flag = true");
					System.out.println("Going out of group refresh");
				}
				// code to handle group click
				else if (msg.equals(userName + " : " + "\\#UsR[]\\#UsR")) {

					String grpName = dis.readUTF();
					BufferedReader br = new BufferedReader(new FileReader(
							address + "/GroupOn Data/Group Data/" + grpName + "/" + grpName + "allMembers.txt"));
					String member = "";
					int flag = 0;
					while ((member = br.readLine()) != null) {
						if (member.equals(userName) || member.equals("Admin : " + userName)) {
							flag = 1;
							System.out.println("Writing back flag");
							dout.writeUTF("\\#UsR[]\\#UsR");
							System.out.println("Sending file");
							dout.writeBoolean(true);
							sendFile(address + "/GroupOn Data/Group Data/" + grpName + "/" + grpName + "ChatLog.txt");
							System.out.println("File Sent");
							dout.writeBoolean(true);
							System.out.println("Going out of file sent");
							break;
						}
					}
					if (flag == 0) {
						dout.writeUTF("\\#UsR[]\\#UsR");
						dout.writeBoolean(false);
						System.out.println("Member not in group sending admin notification");
						br = new BufferedReader(new FileReader(
								address + "/GroupOn Data/Group Data/" + grpName + "/" + grpName + "allMembers.txt"));
						member = br.readLine();
						member = member.substring(member.indexOf(":") + 1).trim();
						System.out.println("Admin = " + member);
						BufferedWriter notiWriter = new BufferedWriter(new FileWriter(
								address + "/GroupOn Data/User Data/" + member + "/notificationLog.txt", true));
						BufferedReader notiReader = new BufferedReader(
								new FileReader(address + "/GroupOn Data/User Data/" + member + "/notificationLog.txt"));
						String str = "";
						int f = 0;
						while ((str = notiReader.readLine()) != null) {
							if (str.equals("\\#Add[@]#\\")) {
								str = notiReader.readLine();
								if (str.equals(userName))
									System.out.println(userName + " already send request");
								f = 1;
								break;
							}
						}
						if (f == 0) {
							int flag1 = 0;
							for (Iterator<GroupClient> j = list.iterator(); j.hasNext();) {
								GroupClient gObj;
								synchronized (this) {
									gObj = j.next();
								}
								if (gObj.userName.equals(member)) {
									gObj.dout.writeUTF("$\\hula[]N[]hula\\$");
									gObj.dout.writeUTF("\\grp\\");
									gObj.dout.writeUTF(userName);
									gObj.dout.writeUTF(grpName);
									flag1 = 1;
									break;
								}
							}
							if (flag1 == 0) {
								System.out
										.println("New user request given to admin " + member + " of group " + grpName);
								notiWriter.append("\\#Add[@]#\\" + "\n");
								notiWriter.append(userName + "\n");
								notiWriter.append(grpName + "\n");
								notiWriter.close();
							}
						}
					}

				}
				// handle logout
				else if (msg.equals(userName + " : " + "\\#%imp%#\\")) {
					// code for logout
					System.out.println("Logging out the user");
					dout.writeUTF("\\#%imp%#\\");
					dout.writeBoolean(true);
					dout.close();
					dis.close();
					oos.close();
					ois.close();
					grpObj.dis.close();
					grpObj.dout.close();
					grpObj.ois.close();
					grpObj.oos.close();
					synchronized (this) {
						list.remove(grpObj);
					}
					sock.close();
					System.out.println("Is socket closed in logout of group " + sock.isClosed());
					System.out.println("Going out of logout");
					break;
				}
				// handle group list request
				else if (msg.equals(userName + " : " + "\\Pro[$]Pro\\")) {
					String grpName = dis.readUTF();
					dout.writeUTF("\\Pro[$]Pro\\");
					System.out.println("Sending file");
					sendFile(address + "/GroupOn Data/Group Data/" + grpName + "/" + grpName + "allMembers.txt");
					System.out.println("File sent");
					System.out.println("Going out of list request");
				}
				// handle user addition to existing group
				else if (msg.equals(userName + " : " + "\\#Add[@]#\\")) {
					// code to add user to existing group
					String grpName = dis.readUTF();
					BufferedReader brAdmin = new BufferedReader(new FileReader(
							address + "/GroupOn Data/Group Data/" + grpName + "/" + grpName + "allMembers.txt"));
					String admin = brAdmin.readLine();
					admin = admin.substring(admin.indexOf(":") + 1).trim();
					System.out.println("Admin = " + admin);
					if (admin.equals(userName)) {
						System.out.println("Admin is adding");
						dout.writeUTF("\\#Add[@]#\\");
						dout.writeBoolean(true);

					} else {
						System.out.println("Chutiya bana raha hai");
						dout.writeUTF("\\#Add[@]#\\");
						dout.writeBoolean(false);
					}

				}
				// main addtion handling code
				else if (msg.equals(userName + " : " + "\\#Add[@]Add#\\")) {
					String grpName = dis.readUTF();
					int n = dis.readInt();
					for (int i = 1; i <= n; i++) {
						String person = dis.readUTF();
						if (!userInList(person, grpName)) {
							System.out.println("selectd " + person);
							BufferedWriter bwNewMember = new BufferedWriter(new FileWriter(
									address + "/GroupOn Data/Group Data/" + grpName + "/" + grpName + "allMembers.txt",
									true));
							bwNewMember.append(person + "\n");
							bwNewMember.close();
							bwNewMember = new BufferedWriter(new FileWriter(
									address + "/GroupOn Data/User Data/" + person + "/inGroupsLog.txt", true));
							bwNewMember.append(grpName + "\n");
							bwNewMember.close();
							System.out.println("Added " + person);
						}
					}
					System.out.println("Adding done");

				}

				if (!(msg.equals(userName + " : " + "\\#Grp[@]Grp\\#")
						|| msg.equals(userName + " : " + "\\#impR[]\\#imp")
						|| msg.equals(userName + " : " + "\\#UsR[]\\#UsR")
						|| msg.equals(userName + " : " + "\\#%imp%#\\")
						|| msg.equals(userName + " : " + "\\Pro[$]Pro\\")
						|| msg.equals(userName + " : " + "\\#Add[@]Add#\\")
						|| msg.equals(userName + " : " + "\\#Add[@]#\\"))) {

					System.out.println("Inside group msg printing loop");

					msg = msg.substring(userName.length() + 3).trim();
					String grpName = msg.substring(0, msg.indexOf(":")).trim();
					msg = msg.substring(grpName.length() + 3).trim();
					BufferedReader br = new BufferedReader(new FileReader(
							address + "/GroupOn Data/Group Data/" + grpName + "/" + grpName + "allMembers.txt"));
					String member = "";
					BufferedWriter bwLog = new BufferedWriter(new FileWriter(
							address + "/GroupOn Data/Group Data/" + grpName + "/" + grpName + "ChatLog.txt", true));
					bwLog.append(userName + " : " + msg + "\n");
					bwLog.close();
					while ((member = br.readLine()) != null) {
						try {
							if (member.substring(0, member.indexOf(":")).trim().equals("Admin")) {
								System.out.println("Before: " + member);
								member = member.substring(member.indexOf(":") + 1).trim();
								System.out.println("After: " + member);
							}
						} catch (Exception e) {
							System.out.println("Member not admin");
							// e.printStackTrace();
						}
						GroupClient gObj;
						String str = "";
						int flag = 0;
						for (Iterator<GroupClient> i = list.iterator(); i.hasNext();) {

							synchronized (this) {
								gObj = i.next();
								str = gObj.userName;
							}
							if (member.equals(str) && !(member.equals(userName))) {
								flag = 1;
								System.out.println(gObj.userName + " is online sending msg");
								try {
									gObj.dout.writeUTF(grpName + " : " + userName + " : " + msg);
								} catch (Exception e) {
									System.out.println(gObj.userName + " socket closed abruptly");
									BufferedWriter notiWriter = new BufferedWriter(
											new FileWriter(address + "/GroupOn Data/User Data/" + gObj.userName
													+ "/notificationLog.txt", true));
									BufferedReader notiReader = new BufferedReader(new FileReader(address
											+ "/GroupOn Data/User Data/" + gObj.userName + "/notificationLog.txt"));
									String temp = "";
									int f = 0;
									while ((temp = notiReader.readLine()) != null) {
										if (grpName.equals("temp")) {
											f = 1;
											break;
										}
									}
									if (f == 0) {
										System.out.println(
												gObj.userName + " not in logs writing names in notification log");
										notiWriter.append(grpName + "\n");
										notiWriter.close();
										System.out.println("logs written");
									}
								}
								break;
							}

						}

						if (!(member.equals(userName)) && flag == 0) {
							System.out.println(member + " offline");
							BufferedWriter notiWriter = new BufferedWriter(new FileWriter(
									address + "/GroupOn Data/User Data/" + member + "/notificationLog.txt", true));
							BufferedReader notiReader = new BufferedReader(new FileReader(
									address + "/GroupOn Data/User Data/" + member + "/notificationLog.txt"));
							String temp = "";
							int f = 0;
							while ((temp = notiReader.readLine()) != null) {
								if (grpName.equals("temp")) {
									f = 1;
									break;
								}
							}
							if (f == 0) {
								System.out.println(member + " not in logs writing names in notification log");
								notiWriter.append(grpName + "\n");
								notiWriter.close();
								System.out.println("logs written");
							}
						}

					}

				}

			}

		} catch (Exception ex) {
			try {
				grpObj.dis.close();
				grpObj.dout.close();
				grpObj.ois.close();
				grpObj.oos.close();
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
			ex.printStackTrace();
		}
	}

	private String saveFile(String username) throws Exception {
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

	public void sendFile(String path) {

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

		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	Boolean validGroup(String groupName) {
		Boolean valid = true;
		Statement stm = connect();
		String query = "select groupname from grp where groupname='" + groupName + "';";
		String query1 = "select username from login where username='" + groupName + "';";
		try {
			ResultSet rs = stm.executeQuery(query);

			Boolean sqlHandler = false;
			if (rs.next()) {
				sqlHandler = true;
			}
			ResultSet rsg = stm.executeQuery(query1);
			if (sqlHandler || rsg.next()) {
				valid = false;
			}
		} catch (Exception ex) {
			System.out.println("Some error is sql query");
			ex.printStackTrace();
		}
		if (valid == true) {
			String query2 = "insert into grp values('" + groupName + "');";
			try {
				stm.executeUpdate(query2);
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		return valid;

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

	Boolean isOffline(String name) throws SQLException {
		boolean status = true;
		try {
			synchronized (this) {
				BufferedReader br = new BufferedReader(
						new FileReader(address + "/GroupOn Data/User Data/tempAllUserLog.txt"));
				String str = "";
				while ((str = br.readLine()) != null) {
					if (str.equals(name)) {
						status = false;
						break;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.err.println("Return from isOffline = " + status);
		return status;
	}

	Boolean userInList(String person, String grpName) {
		Boolean status = false;
		try {
			BufferedReader brUser = new BufferedReader(
					new FileReader(address + "/GroupOn Data/Group Data/" + grpName + "/" + grpName + "allMembers.txt"));
			String str = "";
			while ((str = brUser.readLine()) != null) {
				if (str.equals(person) || str.equals("Admin : " + person)) {
					status = true;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;
	}
}
