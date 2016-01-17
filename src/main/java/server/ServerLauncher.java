package server;

import java.net.ServerSocket;
import java.net.Socket;


public class ServerLauncher {
	private static final int DEFAULT_PORT = 8080;
	
	public static void main(String[] args) throws Exception {
		Server server = new Server();
		listenConnectionRequest(server);
	}

	private static void listenConnectionRequest(Server server) {
		try (ServerSocket listenSocket = new ServerSocket(DEFAULT_PORT)) {
			System.out.println("채팅 서버가 "+ DEFAULT_PORT + "포트에서 시작하였습니다.");
			Socket connection;
			while((connection = listenSocket.accept()) != null) {
				server.createClientThread(connection);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
