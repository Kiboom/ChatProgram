import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
	private static int uniqueId = 0;
	private ArrayList<ClientThread> clientThreadList = new ArrayList<ClientThread>();
	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private static final Logger logger = LoggerFactory.getLogger(Server.class);

	public void handleRequest(Socket connection) {
		ClientThread clientThread = new ClientThread(connection);
		clientThreadList.add(clientThread);
//		clientThread.start();
	}

	private void display(String msg) {
		String time = dateFormat.format(new Date()) + " " + msg;
		System.out.println(time);
	}

	private synchronized void broadcast(String message) {
		String time = dateFormat.format(new Date());
		message = time + " " + message + "\n";
		System.out.print(message);

		// we loop in reverse order in case we would have to remove a Client
		// because it has disconnected
		for (int i = clientThreadList.size(); --i >= 0;) {
			ClientThread ct = clientThreadList.get(i);
			// try to write to the Client if it fails remove it from the list
			if (!ct.writeMsg(message)) {
				clientThreadList.remove(i);
				display("Disconnected Client " + ct.username + " removed from list.");
			}
		}
	}

	synchronized void remove(int id) {
		for (int i = 0; i < clientThreadList.size(); ++i) {
			ClientThread clientThread = clientThreadList.get(i);
			if (clientThread.id == id) {
				clientThreadList.remove(i);
				clientThread.close();
			}
		}
	}

	/** One instance of this thread will run for each client */
	class ClientThread extends Thread {
		// the socket where to listen/talk
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		DataOutputStream dos;
		DataInputStream dis;
		int id;
		String username;
		MessageType messageType;
		String date;
		String httpMethod;
		String requestUrl;

		// 생성자
		public ClientThread(Socket connection) {
			id = ++uniqueId;
			this.socket = connection;
			/* Creating both Data Stream */
			System.out.println("Thread trying to create Object Input/Output Streams");
			try {
				// // create output first
				// sOutput = new
				// ObjectOutputStream(connection.getOutputStream());
				// sInput = new ObjectInputStream(connection.getInputStream());
				// // read the username
				// username = (String) sInput.readObject();
				// display(username + " just connected.");
				analyzeRequest(connection);
				dos = new DataOutputStream(connection.getOutputStream());
				try {
					logger.debug(httpMethod.toString());
					logger.debug(requestUrl.toString());
					if (httpMethod.equals("GET")) {
						byte[] body;
						body = Files.readAllBytes(new File("./WebContent/WEB-INF" + requestUrl).toPath());
						response200Header(dos, body.length);
						responseBody(dos, body);
					} else if (httpMethod.equals("POST")) {

					}
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			} catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
			}

			date = new Date().toString() + "\n";
		}

		private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
			try {
				dos.writeBytes("HTTP/1.1 200 OK \r\n");
				dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
				dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
				dos.writeBytes("\r\n");
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}

		private void response302Header(DataOutputStream dos, int lengthOfBodyContent) {
			try {
				dos.writeBytes("HTTP/1.1 200 OK \r\n");
				dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
				dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
				dos.writeBytes("\r\n");
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}

		private void responseBody(DataOutputStream dos, byte[] body) {
			try {
				dos.write(body, 0, body.length);
				dos.writeBytes("\r\n");
				dos.flush();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}

		private void analyzeRequest(Socket connection) throws IOException {
			InputStream inputStream = connection.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			String firstLine = bufferedReader.readLine();

			this.httpMethod = firstLine.split(" ")[0];

			String requestUrl = firstLine.split(" ")[1];
			if (requestUrl.equals("/")) {
				requestUrl = "/index.html";
			}
			this.requestUrl = requestUrl;
		}

		// 각 클라이언트마다 개별적인 스레드를 열어서 request listen.
		public void run() {
			// to loop until LOGOUT
			boolean keepGoing = true;
			while (keepGoing) {
				// read a String (which is an object)
				try {
					messageType = (MessageType) sInput.readObject();
				} catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;
				} catch (ClassNotFoundException e2) {
					break;
				}
				// the messaage part of the ChatMessage
				String message = messageType.getMessage();

				// Switch on the type of message receive
				switch (messageType.getType()) {

				case MessageType.MESSAGE:
					broadcast(username + ": " + message);
					break;
				case MessageType.LOGOUT:
					display(username + " disconnected with a LOGOUT message.");
					keepGoing = false;
					break;
				case MessageType.WHOISIN:
					writeMsg("List of the users connected at " + dateFormat.format(new Date()) + "\n");
					// scan al the users connected
					for (int i = 0; i < clientThreadList.size(); ++i) {
						ClientThread ct = clientThreadList.get(i);
						writeMsg((i + 1) + ") " + ct.username + " since " + ct.date);
					}
					break;
				}
			}
			// remove myself from the arrayList containing the list of the
			// connected Clients
			remove(id);
			close();
		}

		// try to close everything
		private void close() {
			try {
				if (sOutput != null)
					sOutput.close();
			} catch (Exception e) {
				logger.error("Thread의 OutputStream을 close하지 못했습니다.");
			}
			try {
				if (sInput != null)
					sInput.close();
			} catch (Exception e) {
				logger.error("Thread의 InputStream을 close하지 못했습니다.");
			}
			try {
				if (socket != null)
					socket.close();
			} catch (Exception e) {
				logger.error("Thread의 socket을 close하지 못했습니다.");
			}
		}

		/*
		 * Write a String to the Client output stream
		 */
		private boolean writeMsg(String msg) {
			// if Client is still connected send the message to it
			if (!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(msg);
			}
			// if an error occurs, do not abort just inform the user
			catch (IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}
}
