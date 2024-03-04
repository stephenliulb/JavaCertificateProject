/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * This class implements a network connection protocol base on the serialized
 * objects over a simple socket connect.
 * <p>
 * The open() method must be called before executing database remote commands;
 * if any exception occurs, the close() method must be called to release
 * occupied network resource.
 * <p>
 * 
 * The example code:<br>
 * <code>
 * RPC rpc = null;<br>
 * try{<br>
 * rpc = new RPC();<br>
 * rpc.open("localhost", 8899);<br>
 * DBResult result = (DBResult) rpc.execute(command);<br>
 * ... ...<br>
 * }finally{<br>
 * if(rpc!=null){<br>
 * 	rpc.close();<br>
 * }<br>
 * }<br>
 * 
 * </code>
 * 
 * @author Stephen Liu
 * 
 */
public class RPC implements AutoCloseable{
	private String server;
	private int port;

	private Socket sock;
	private OutputStream os;
	private InputStream is;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;

	private boolean isConnected = false;

	/**
	 * Create a RPC object for a specific server.
	 * 
	 * @param server remote database server address.
	 * @param port   remote database server port.
	 */
	public RPC(String server, int port) {
		this.server = server;
		this.port = port;
	}

	/**
	 * Initialize the connection with the remote server.
	 * 
	 * @throws IOException Any of the usual Input/Output related exceptions.
	 */
	public void open() throws IOException {
		sock = new Socket(server, port);
		os = sock.getOutputStream();
		oos = new ObjectOutputStream(os);
		is = sock.getInputStream();
		ois = new ObjectInputStream(is);

		isConnected = true;
	}

	/**
	 * Determine if there is a connection to remote database server.
	 * 
	 * @return true if connection exists,otherwise false.
	 */
	public boolean isConnected() {
		return isConnected;
	}

	/**
	 * Release the connection with the remote server.
	 * 
	 * @throws IOException Any of the usual Input/Output related exceptions.
	 */
	@Override
	public void close() throws IOException {
		isConnected = false;

		if (oos != null) {
			oos.close();
			oos = null;
		}

		if (ois != null) {
			ois.close();
			ois = null;
		}

		if (os != null) {
			os.close();
			os = null;
		}

		if (is != null) {
			is.close();
			is = null;
		}

		if (sock != null) {
			sock.close();
			sock = null;
		}
	}

	/**
	 * Execute the command on the remote database server. Before the method got
	 * executed, network connection must be successfully set up by invoked the
	 * method <code>open()</code>
	 * 
	 * @param command Include command type and command data.
	 * @return Data result if successful or an exception ojbect if failed.
	 * @throws IOException            Any of the usual Input/Output related
	 *                                exceptions; or the connection to server has
	 *                                not been set up.
	 * @throws ClassNotFoundException Class of a serialized object cannot be found.
	 */
	public synchronized Object execute(Object command) throws IOException, ClassNotFoundException {
		if (command == null) {
			return null;
		}

		if (!isConnected) {
			throw new IOException("Connection to server has not been setup.");
		}

		oos.writeObject(command);
		oos.flush();
		Object result = ois.readObject();
		return result;
	}

	/**
	 * Get server address connected by current RPC object.
	 * 
	 * @return the server
	 */
	public String getServer() {
		return server;
	}

	/**
	 * Get server address connected by current RPC object.
	 * 
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

}
