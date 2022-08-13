/*
 * Server.java
 * Chat Room Server
 * Jan Rubio
 * jcr4698
 * Summer 2021
 */

package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Scanner;

import com.google.gson.Gson;

public class Server extends Observable{
	
	private static HashMap<String, String> accounts;
	
	public static void main(String[] args) {
		// initialize the auction with some accounts
		accounts = new HashMap<String, String>();
		accounts.put("password1", "user1");
		accounts.put("password2", "user2");
		accounts.put("password3", "user3");
		accounts.put("password4", "user4");
		accounts.put("password5", "user5");
		
		// initialize server
		new Server().runServer();
	}
	
	private void runServer() {
		try {
			setUpNetworking();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setUpNetworking() throws Exception {
		// create server socket on port
		ServerSocket serverSocket = new ServerSocket(4242);
		
		// keep listening
		while(true) {
			// wait for client socket connection
			Socket clientSocket = serverSocket.accept();
			System.out.println("Connecting to..." + clientSocket);
			
			// initialize client and start communication
			ClientHandler handler = new ClientHandler(this, clientSocket, clientSocket.getPort());
			this.addObserver(handler); //add to the observer's list
			Thread t = new Thread(handler);
			t.start();
		}
	}
	
	protected String processMessage(String gsonMessage, String speaker) {
		String output = "";
		try {
			// message to gson
			Gson gson = new Gson();
			Command cmd = gson.fromJson(gsonMessage, Command.class);
			cmd.input = speaker + ": " + cmd.input;
			
			// determine output
			output = gson.toJson(cmd);
			
			// notify all clients
			this.setChanged();
			this.notifyObservers(output);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}
	
	protected boolean loginAttempt(String username, String password) {
		if(username.equals(accounts.get(password))) {
			return true;
		}
		return false;
	}
	
	protected boolean registerAttempt(String username, String password) {
		if(!accounts.containsValue(username)) {
			accounts.put(password, username);
			return true;
		}
		return false;
	}
	
	protected void removeClient(ClientHandler handler) {
		this.deleteObserver(handler);
		this.setChanged();
		System.out.println("Observer count: " + this.countObservers());
	}
}
