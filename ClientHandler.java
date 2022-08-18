/*
 * ClientHandler.java
 * Chat Room Server
 * Jan Rubio
 * jcr4698
 * Summer 2021
 */

package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

import com.google.gson.Gson;

public class ClientHandler implements Runnable, Observer {
	
	private Server server;
	private Socket clientSocket;
	private BufferedReader fromClient;
	private PrintWriter toClient;
	private int port;
	private String user;
	
	Gson gsonMessage;
	Command cmd;
	
	protected ClientHandler(Server server, Socket clientSocket, int port) {
		this.server = server;
		this.clientSocket = clientSocket;
		try {
			fromClient = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			toClient = new PrintWriter(this.clientSocket.getOutputStream());
		} catch(IOException e) {
			e.printStackTrace();
		}
		this.port = port;
		this.user = "";
	}
	
	protected void sendToClient(String message) {
		Gson gsonMessage = new Gson();
		Command cmd = gsonMessage.fromJson(message, Command.class);
		if(user.equals(""))
			System.out.println("Sending to client " + port + ": \"" + cmd.input + "\"");
		else
			System.out.println("Sending to " + user + ": \"" + cmd.input + "\"");
		toClient.println(message);
		toClient.flush();
	}
	
	@Override
	public void run() {
		String input;
		try {
			while((input = fromClient.readLine()) != null) {
				// obtain message from json
				gsonMessage = new Gson();
				cmd = gsonMessage.fromJson(input, Command.class);
				
				System.out.println("Command: " + cmd);
				
				if(cmd.command.equals("login")) { // login attempt command
					if(server.loginAttempt(cmd.username, cmd.password)) { // attempt passed
						user = cmd.username;
						cmd = new Command("login successful", "login");
						cmd.username = user;
						gsonMessage = new Gson();
						sendToClient(gsonMessage.toJson(cmd));
					}
					else { // attempt failed
						cmd = new Command("incorrect user/password", "login");
						gsonMessage = new Gson();
						sendToClient(gsonMessage.toJson(cmd));
					}
				}
				else if(cmd.command.equals("register")) { // register command
					String regStatus = "this username is already taken";
					if(server.registerAttempt(cmd.username, cmd.password)) {
						regStatus = "registration successful";
					}
					cmd = new Command(regStatus, "register");
					sendToClient(gsonMessage.toJson(cmd));
				}
				else if(cmd.command.equals("quit")) { // quit/exit command
					server.removeClient(this);
					this.clientSocket.close();
				}
				else if(cmd.command.equals("message")) { // message command
					Command cmdSend = new Command(cmd.input, cmd.command);
					Gson gsonMessageSend = new Gson();
					if(!user.equals("") && (cmd.username != null) && !cmd.username.equals("")) {
						server.processMessage(gsonMessageSend.toJson(cmdSend), cmd.username);
					}
				}
				else if(cmd.command.equals("logout")) { // logout command
					user = ""; // user field is empty
					cmd = new Command("logout", "logout");
					gsonMessage = new Gson();
					sendToClient(gsonMessage.toJson(cmd));
				}
			}
			clientSocket.close();
		} catch(IOException e) {
			if(!user.equals(""))
				System.out.println(user + " left. (port " + port + ")");
			else
				System.out.println("Port " + port + " left.");
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		Gson gsonSend = new Gson();
		Command cmd = gsonSend.fromJson((String)arg, Command.class);		
		// if command is message
		if(cmd.command.equals("message")) {
			sendToClient(gsonSend.toJson(cmd));
		}
		// if command is update
		if(cmd.command.equals("bid")) {
			cmd.command = "update";
			sendToClient(gsonSend.toJson(cmd));
		}
	}
}
