/*
 * Chat Room Command.java
 * Chat Room Client
 * Jan Rubio
 * jcr4698
 * Summer 2021
 */

package client;

public class Command {
	String command;
	String input;
	String username;
	String password;
	
	protected Command(String input, String cmd) {
		this.command = cmd;
		this.input = input;
	}

	@Override
	public String toString() {
		return "{command:" + command + "; input:" + input + "; username:" + username + "; password:" + password;
	}
}
