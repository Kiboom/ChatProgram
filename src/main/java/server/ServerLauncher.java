package server;

import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerLauncher {
	private static final Logger logger = LoggerFactory.getLogger(ServerLauncher.class);
	private static final int DEFAULT_PORT = 8080;
	
	public static void main(String[] args) throws Exception {
		Server server = new Server();
		listenConnectionRequest(server);
	}

	private static void listenConnectionRequest(Server server) {
		try (ServerSocket listenSocket = new ServerSocket(DEFAULT_PORT)) {
			logger.info("채팅 서버가 {}포트에서 시작하였습니다.", DEFAULT_PORT);
			Socket connection;
			while((connection = listenSocket.accept()) != null) {
				server.createClientThread(connection);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
