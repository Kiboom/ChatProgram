import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServerLauncher {
	private static final Logger logger = LoggerFactory.getLogger(WebServerLauncher.class);
	private static final int DEFAULT_PORT = 8080;
	
	public static void main(String[] args) throws Exception {
		
		try (ServerSocket listenSocket = new ServerSocket(DEFAULT_PORT)) {
			logger.info("채팅 서버가 {}포트에서 시작하였습니다.", DEFAULT_PORT);
			Server server = new Server();
			
			Socket connection;
			while((connection = listenSocket.accept()) != null) {
				server.handleRequest(connection);
			}
		}
	}

}
