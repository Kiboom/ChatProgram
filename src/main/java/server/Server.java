package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import message.Message;

public class Server {
	private static int uniqueId = 0;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private ArrayList<ClientThread> clientThreadList = new ArrayList<ClientThread>();
//	private static final Logger logger = LoggerFactory.getLogger(Server.class);
	
	
	// 클라이언트로부터 연결 요청이 오면 클라이언트와 소통할 수 있는 쓰레드 생성 
	public void createClientThread(Socket connection) {
		ClientThread clientThread = new ClientThread(connection);
		clientThreadList.add(clientThread);
		clientThread.start();
	}

	
	// 채팅창에 특정 메시지를 출력. 시간 정보와 함께 출력함.
	private void display(String message) {
		message = dateFormat.format(new Date()) + " " + message;
		System.out.println(message);
	}


	// 연결을 맺고 있는 모든 클라이언트에게 특정 메시지 전달
	private synchronized void broadcast(String message) {
		String time = dateFormat.format(new Date());
		message = time + " " + message + "\n";
		System.out.print(message);

		for (ClientThread client : clientThreadList){
			if(!client.sendMessage(message)){
				clientThreadList.remove(client);
				display(client.username + "와의 연결이 끊어졌습니다.");
			}
		}
	}

	
	// 특정 클라이언트와의 연결을 종료 
	private synchronized void remove(int id) {
		for (ClientThread c : clientThreadList) {
			if (c.id == id) {
				clientThreadList.remove(c);
				c.closeStreams();
			}
		}
	}

	
	
	
	class ClientThread extends Thread {
		int id;
		String username;
		String createdTime;
		Socket connection;
		ObjectInputStream inputStream;
		ObjectOutputStream outputStream;
		
		
		public ClientThread(Socket connection) {
			this.id = ++uniqueId;
			this.connection = connection;
			createStreams(connection);
			this.createdTime = new Date().toString() + "\n";
		}

		
		
		private void createStreams (Socket connection) {
			try {
				display(id+"번 클라이언트와 Input/Output 스트림 생성 중");
				outputStream = new ObjectOutputStream(connection.getOutputStream());
				inputStream = new ObjectInputStream(connection.getInputStream());
				username = (String) inputStream.readObject();
				display("\'" + username + "\'" + " 사용자와 연결되었습니다");
			} catch (IOException e) {
				display(id+"번 클라이언트와 Input/Output 스트림 생성에 실패하였습니다 : " + e);
			} catch (ClassNotFoundException e) {
				display(id+"번 사용자의 이름 정보를 받아오지 못하였습니다 : " + e);
			}
		}

		
		
		private Message getMessageFromClient() {
			try {
				return (Message) inputStream.readObject();
			} catch (IOException e) {
				display("\'" + username + "\'" + " 사용자와의 연결 상태에 문제가 발생했습니다 : " + e);
			} catch (ClassNotFoundException e) {
				display("\'" + username + "\'" + " 사용자가 보낸 메시지에 오류가 발생했습니다 : " + e);
			}
			return null;
		}
		
		
		
		public void run() {
			while (true) {
				Message message = getMessageFromClient();
				String messageContent = message.getMessage();

				if(message.getType() == Message.MESSAGE){
					broadcast(username + " : " + messageContent);
				}
				else if(message.getType() == Message.LOGOUT){
					display('\'' + username + '\'' + "의 연결이 사용자 요청에 의해 종료되었습니다");
					break;
				}
//				else {
//					sendMessage("List of the users connected at " + dateFormat.format(new Date()) + "\n");
//					for (int i = 0; i < clientThreadList.size(); ++i) {
//						ClientThread ct = clientThreadList.get(i);
//						sendMessage((i + 1) + ") " + ct.username + " since " + ct.createdTime);
//					}
//					break;
//				}
			}
			remove(id);
			closeStreams();
		}


		private void closeStreams() {
			try {
				if (outputStream != null)
					outputStream.close();
			} catch (Exception e) {
//				logger.error("\'" + username + "\'" + "의 OutputStream을 종료하지 못했습니다.");
			}
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (Exception e) {
//				logger.error("\'" + username + "\'" + "의 InputStream을 종료하지 못했습니다.");
			}
			try {
				if (connection != null)
					connection.close();
			} catch (Exception e) {
//				logger.error("\'" + username + "\'" + "의 Socket을 종료하지 못했습니다.");
			}
		}


		// 해당 클라이언트에 메시지 전달
		private boolean sendMessage(String message) {
			if (!connection.isConnected()) {
				closeStreams();
				return false;
			}
			try {
				outputStream.writeObject(message);
			} catch (IOException e) {
				display("\'" + username + "\'" + "에게 메시지를 전송하는데 실패하였습니다");
//				logger.error(e.getMessage());
			}
			return true;
		}
	}
}
