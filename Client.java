/*
 * Client.java
 * Chat Room Client
 * Jan Rubio
 * jcr4698
 * Summer 2021
 */

package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.awt.*;
import java.awt.event.KeyEvent;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import com.google.gson.Gson;

public class Client extends Application{
	
	// Socket Variables
	private static Socket socket;
	private static final String HOST = "localhost"; //10.165.200.55
	private static BufferedReader fromServer;
	private static PrintWriter toServer;
	private Scanner consoleInput;
	private static int clientPort;
	
	// GUI Fields for Login
	private static Stage window;
	private static GridPane grid1;
	private static TextField username;
	private static Label userLbl;
	private static TextField password;
	private static Label passLbl;
	private static Button loginBtn;
	private static Button registerBtn;
	private static Label invalidLog;
    private static Button quitBtn1;
    
    // GUI fields for Chat Room
    private static GridPane grid2;
    private static Label welcomeUser;
    private TextArea chatDisplay;
    private TextArea textDisplay;
    private Button logoutBtn;
    private Button quitBtn2;
    
    // GUI fields for Registration
    private static GridPane grid3;
    private TextField usernameRegister;
	private TextField passwordRegister;
	private TextField confirmPasswordRegister;
	private Label missmatchedPassword;
	private static Label userTaken;
	private Button registerMeBtn;
    
	
	// Client Information Variables  
    public static String clientUsername = "";
    public static String clientPassword = "";
    public static Command cmd = new Command(null, null);
    
	
	public static void main(String[] args) {
		new Client().runClient(); // set up the network of client
		launch(args);
	}
	
	private void runClient() {
		consoleInput = new Scanner(System.in);
		try {
			setUpNetworking();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	protected static void sendToServer(String jsonMessage) {
		toServer.println(jsonMessage);
		toServer.flush();
	}
	
	protected void loginToServer(String jsonLoginCommand) {
		toServer.println(jsonLoginCommand);
		toServer.flush();
	}
	
	private void setUpNetworking() throws Exception {
		// Connect socket to server socket
		socket = new Socket(HOST, 4242);
		System.out.println("Connecting to..." + socket);
		
		// initialize input and output
		InputStreamReader readSock = new InputStreamReader(socket.getInputStream());
		fromServer = new BufferedReader(readSock);
		toServer = new PrintWriter(socket.getOutputStream());
		
		// create a reader task
		Thread readerThread = new Thread( new Runnable() {
			@Override
			public void run() {
				String input;
				try { // wait for output from server
					while((input = fromServer.readLine()) != null) {
						// obtain message from json
						cmd = new Gson().fromJson(input, Command.class);
						
						if(cmd.command.equals("register")) { // register command
							if(cmd.input.equals("registration successful")) {
								Platform.runLater(new Runnable() {
										@Override
										public void run() {
											window.setScene(makeLoginScene());
											loginHandler(); // event handler
										}
								}); // Display primaryStage
							}
							else {
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										userTaken = new Label(cmd.input);
						 				GridPane.setConstraints(userTaken, 1, 0);
						 				grid3.getChildren().add(userTaken);
									}
								}); // Display invalid
							}
						}
						else if(cmd.command.equals("login")) { // login command
							if(cmd.input.equals("incorrect user/password")) {
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										invalidLog = new Label(cmd.input);
										GridPane.setConstraints(invalidLog, 1, 2);
										grid1.getChildren().add(invalidLog);
									}
								});
							}
							else {
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										clientUsername = cmd.username;
										makeChatRoom();
									}
								});
							}
						}
						else if(cmd.command.equals("logout")) {
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									clientUsername = "";
									clientPassword = "";
									window.setScene(makeLoginScene());
									loginHandler(); // event handler
								}
							});
						}
					}
				} catch(IOException e) {
					System.exit(0); // if server crashes, just close.
				}
			}
		});
		
		// create a writer task
		Thread writerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					String input = consoleInput.nextLine();
					Command cmd = new Command(input, "message");
					Gson gsonMessage = new Gson();
					sendToServer(gsonMessage.toJson(cmd));
				}
			}
		});
		
		// run reading and writing tasks
		readerThread.start();
		writerThread.start();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// initialize stage
		window = primaryStage;
//		window.setTitle("Chat Room - login");

		// Initialize primary condition of primaryStage
		
		Scene scene1 = makeLoginScene();
		
	    // Start first GUI
	    
		loginHandler(); // event handler
		window.setScene(scene1); // Display scene1 at beginning
		window.show();
	}		
	
	private Scene makeLoginScene() { // initialize login window
		// Login window registration
		window.setTitle("Chat Room - login");
		
		// grid1 scene format
		grid1 = new GridPane();
		grid1.setPadding(new Insets(10, 10, 10, 10));
		grid1.setVgap(10);
		grid1.setHgap(10);
		
		// user name field
		userLbl = new Label("username:");
		GridPane.setConstraints(userLbl, 0, 0);
		username = new TextField();
		username.setPromptText("username");
		GridPane.setConstraints(username, 1, 0);
		
		// password field
		passLbl = new Label("password:");
		GridPane.setConstraints(passLbl, 0, 1);
		password = new TextField();
		password.setPromptText("password");
		GridPane.setConstraints(password, 1, 1);
		
		// login button
		loginBtn = new Button("login");
	    GridPane.setConstraints(loginBtn, 0, 2);
	    
	    // login as guest button
	    registerBtn = new Button("register");
	    GridPane.setConstraints(registerBtn, 0, 3);
	    
	    // quit button
	    quitBtn1 = new Button("quit");
	    GridPane.setConstraints(quitBtn1, 0, 4);
	    
	    // put labels, buttons, and text fields in grid
	    grid1.getChildren().addAll(username, userLbl, password, passLbl, loginBtn, registerBtn, quitBtn1);
		
		// fill stage with scene
		return new Scene(grid1, 300, 200);
	}
	
	private void loginHandler() { // button set up
		
		// login
		loginBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(!username.getText().equals("") && !password.getText().equals("")) {
					Gson gsonMessage = new Gson();
					cmd.command = "login";
					cmd.username = username.getText();
					cmd.password = password.getText();
					sendToServer(gsonMessage.toJson(cmd));
				}
			}
		});
		
		// password and enter
		password.setOnKeyPressed(e -> {
			if(e.getCode() == KeyCode.ENTER && !username.getText().equals("") && !password.getText().equals("")) {
				Gson gsonMessage = new Gson();
				cmd.command = "login";
				cmd.username = username.getText();
				cmd.password = password.getText();
				sendToServer(gsonMessage.toJson(cmd)); // register button
			}
		});
		
		// register
		registerBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				makeRegisterScene();
			}
		});
		
		// quit button
		quitBtn1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					Gson gsonMessage = new Gson();
					sendToServer(gsonMessage.toJson(new Command(null, "quit")));
					fromServer.close();
					toServer.close();
					System.exit(0);
				} catch (IOException e) {
					System.exit(0); // just exit
				}
				window.close();
			}
		});
		
		// exit button
		window.setOnCloseRequest(e -> {
			quitBtn1.fire();
		});
	}
	
	private void makeRegisterScene() {
		// Registration window title
		window.setTitle("Chat Room - Registration");
		
		// grid1 scene format
		grid3 = new GridPane();
		grid3.setPadding(new Insets(10, 10, 10, 10));
		grid3.setVgap(10);
		grid3.setHgap(10);
		
		// user name field
		usernameRegister = new TextField();
		usernameRegister.setPromptText("new username");
		GridPane.setConstraints(usernameRegister, 0, 0);

		// password field
		passwordRegister = new TextField();
		passwordRegister.setPromptText("new password");
		GridPane.setConstraints(passwordRegister, 0, 1);
		
		// password field
		confirmPasswordRegister = new TextField();
		confirmPasswordRegister.setPromptText("confirm password");
		GridPane.setConstraints(confirmPasswordRegister, 0, 2);
		
		// register button
	    registerMeBtn = new Button("register me");
	    GridPane.setConstraints(registerMeBtn, 0, 3);
	 	registerMeBtn.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent event) {
 				if(!usernameRegister.getText().equals("") && !passwordRegister.getText().equals("") && !confirmPasswordRegister.getText().equals("")) {
 					if(passwordRegister.getText().equals(confirmPasswordRegister.getText())) {
 						Gson gsonMessage = new Gson();
 	 					cmd.command = "register";
 	 					cmd.username = usernameRegister.getText();
 	 					cmd.password = passwordRegister.getText();
 	 					sendToServer(gsonMessage.toJson(cmd));
 					}
 					else {
 						missmatchedPassword = new Label("passwords must match");
 						GridPane.setConstraints(missmatchedPassword, 1, 2);
 						grid3.getChildren().add(missmatchedPassword);
 					}
 				}
 			}
 		});
	    
	    // put labels, buttons, and text fields in grid
	    grid3.getChildren().addAll(usernameRegister, passwordRegister, confirmPasswordRegister, registerMeBtn, quitBtn1);
	    
	    // fill stage with scene
	    window.setScene(new Scene(grid3, 400, 200));
	}
	
	private void makeChatRoom() {
		// Chat Room window title
		window.setTitle("Chat Room - Welcome, " + username.getText() + "!");
		
		// grid2 scene format
		grid2 = new GridPane();
		grid2.setPadding(new Insets(10, 10, 10, 10));
		grid2.setVgap(10);
		grid2.setHgap(10);
		
		// logout button
		logoutBtn = new Button("logout");
		GridPane.setRowIndex(logoutBtn, 0);
		GridPane.setColumnIndex(logoutBtn, 0);
		
		// username label
		welcomeUser = new Label("~Chat~");
		GridPane.setRowIndex(welcomeUser, 1);
		GridPane.setColumnIndex(welcomeUser, 0);
		
		// chat window
		chatDisplay = new TextArea();
		GridPane.setRowIndex(chatDisplay, 2);
		GridPane.setRowSpan(chatDisplay, 4);
	    GridPane.setColumnSpan(chatDisplay, 4);
//		GridPane.setFillWidth(chatDisplay, true);
		chatDisplay.setPrefHeight(300.0);
	    chatDisplay.setEditable(false);
	    
	    // message window
	    textDisplay = new TextArea();
	    GridPane.setRowIndex(textDisplay, 6);
	    GridPane.setRowSpan(textDisplay, 4);
	    GridPane.setColumnSpan(textDisplay, 4);
//	    GridPane.setFillWidth(textDisplay, true);
	    textDisplay.setPrefHeight(100.0);
		
		// put labels, buttons, and text fields in grid
	    grid2.getChildren().addAll(welcomeUser, logoutBtn, chatDisplay, textDisplay);
	    
	    // fill stage with scene
	    window.setScene(new Scene(grid2, 500, 500));
	    
	    // start handler
	    chatHandler();
	}

	private void chatHandler() {
		
		// exit button
		window.setOnCloseRequest(e -> {
			quitBtn1.fire();
		});
		
		// logout button
		logoutBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Gson gsonMessage = new Gson();
				cmd.command = "logout";
				sendToServer(gsonMessage.toJson(cmd));
			}
		});
		
		// chat button
		textDisplay.setOnKeyPressed(e -> { // at "sendToServer" it erases clientUsername for some reason!
			if(e.getCode() == KeyCode.ENTER) {
				String message = textDisplay.getText().trim();
				if(!message.equals("")) {
					Gson gsonMessage = new Gson();
					cmd.username = clientUsername;
					cmd.command = "message";
					cmd.input = message;
					sendToServer(gsonMessage.toJson(cmd)); // register button
				}
			}
		});
	}
}
