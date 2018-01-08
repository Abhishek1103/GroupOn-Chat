
import java.io.IOException;

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
import java.nio.file.Files;

public class SuperServer {
	public static void main(String args[]) throws IOException {

		if (!new File(System.getProperty("user.home") + "/GroupOn Data").exists()) {
			new File(System.getProperty("user.home") + "/GroupOn Data").mkdir();
			new File(System.getProperty("user.home") + "/GroupOn Data/Binary Warriors").mkdir();
			new File(System.getProperty("user.home") + "/GroupOn Data/User Data").mkdir();
			new File(System.getProperty("user.home") + "/GroupOn Data/Group Data").mkdir();
			Files.copy(new File("defaultPP.png").toPath(),
					new File(System.getProperty("user.home") + "/GroupOn Data/Binary Warriors/defaultPP.png").toPath());
		} else {
			if (!new File(System.getProperty("user.home") + "/GroupOn Data/Binary Warriors").exists()) {
				new File(System.getProperty("user.home") + "/GroupOn Data/Binary Warriors").mkdir();
				if (!new File(System.getProperty("user.home") + "/GroupOn Data/Binary Warriors/defaultPP.png").exists())
					Files.copy(new File("defaultPP.png").toPath(),
							new File(System.getProperty("user.home") + "/GroupOn Data/Binary Warriors/defaultPP.png")
									.toPath());
			}

			if (!new File(System.getProperty("user.home") + "/GroupOn Data/User Data").exists())
				new File(System.getProperty("user.home") + "/GroupOn Data/User Data").mkdir();

			if (!new File(System.getProperty("user.home") + "/GroupOn Data/Group Data").exists())
				new File(System.getProperty("user.home") + "/GroupOn Data/Group Data").mkdir();
		}
		SingleServer ss = new SingleServer();
		GroupServer gs = new GroupServer();
		FileServer fs = new FileServer();
		ss.start();
		gs.start();
		fs.start();

	}
}
