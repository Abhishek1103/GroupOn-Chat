
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author surbhit
 */

public class GroupClient {
	String userName;
	DataInputStream dis;
	DataOutputStream dout;
	ObjectOutputStream oos;
	ObjectInputStream ois;

	GroupClient(String userName, DataInputStream dis, DataOutputStream dout, ObjectInputStream ois,
			ObjectOutputStream oos) {
		this.userName = userName;
		this.dis = dis;
		this.dout = dout;
		this.oos = oos;
		this.ois = ois;

	}
}
