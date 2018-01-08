/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.net.*;

/**
 *
 * @author aks
 */
public class Client {
	Socket sock;
	String userName;
	DataInputStream dis;
	DataOutputStream dout;
	ObjectOutputStream oos;
	ObjectInputStream ois;

	String msg = "";

	Client(Socket sock, String userName, DataInputStream dis, DataOutputStream dout, ObjectInputStream ois,
			ObjectOutputStream oos) {
		this.sock = sock;
		this.userName = userName;
		this.dis = dis;
		this.dout = dout;
		this.oos = oos;
		this.ois = ois;

	}
}
