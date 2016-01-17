package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import message.Message;



public class Client  {

	private int port;
	private Socket connection;
	private String server, username;
	private ObjectInputStream inputStream;	
	private ObjectOutputStream outputStream;	
	private Scanner scan = new Scanner(System.in);
	
	
	
	Client(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;
	}
	
	
	

	public boolean start() {
		try {
			connection = new Socket(server, port);
		} catch(Exception e) {
			displayError("서버와의 연결에 실패하였습니다 : " + e);
			return false;
		}
		
		try	{
			inputStream  = new ObjectInputStream(connection.getInputStream());
			outputStream = new ObjectOutputStream(connection.getOutputStream());
		} catch (IOException e) {
			displayError("input/output stream 생성에 오류가 발생하였습니다 : " + e);
			return false;
		}

		new ListenFromServer().start();

		try{
			outputStream.writeObject(username);
		} catch (IOException e) {
			displayError("로그인에 오류가 발생하였습니다 : " + e);
			disconnect();
			return false;
		}

		String successMessage = "서버와의 연결에 성공하였습니다 " + connection.getInetAddress() + ":" + connection.getPort();
		displaySuccess(successMessage);
		return true;
	}


	
	
	private static void displayError(String message) {
		System.out.println(message);      
	}

	
	
	private static void displaySuccess(String message) {
		System.out.println(message);      
	}
	
	
	
	void getMessageFromUser(){
		
		while(true) {
			System.out.print("> ");
			String message = scan.nextLine();
			
			if(message.equalsIgnoreCase("LOGOUT")) {
				sendMessage(new Message(Message.LOGOUT, ""));
				break;
			}
			else {			
				sendMessage(new Message(Message.MESSAGE, message));
			}
		}
		
		scan.close();
	}
	
	
	
	
	private void sendMessage(Message message) {
		try {
			outputStream.writeObject(message);
		} catch (IOException e) {
			displayError("서버에 메시지를 전송하는 중 오류가 발생하였습니다 : " + e);
		}
	}

	
	

	public void disconnect() {
		try { 
			if(inputStream != null){ 
				inputStream.close();
			}
			if(outputStream != null){
				outputStream.close();
			}
			if(connection != null){
				connection.close();
			}
		} catch (Exception e) {
			displayError("접속 종료 중 오류가 발생하였습니다 : ");
		} 
	}

	
	
	
	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					String message = (String) inputStream.readObject();
					System.out.print(message);
					System.out.print("> ");
				}
				catch(IOException e) {
					displayError("서버가 연결을 종료하였습니다 : " + e);
					break;
				}
				catch(ClassNotFoundException e) {
					displayError(e.getMessage());
					break;
				}
			}
		}
	}
}
