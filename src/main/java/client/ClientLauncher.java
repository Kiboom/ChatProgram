package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientLauncher {
	private static final int DEFAULT_PORT = 8080;
	private static final Logger logger = LoggerFactory.getLogger(ClientLauncher.class);

	public static void main(String[] args) {
		String serverAddress = "localhost";
		String clientName = getNameFromUser();
		Client client = new Client(serverAddress, DEFAULT_PORT, clientName);
		
		if(!client.start()){
			return;
		}
		
		client.getMessageFromUser();
		client.disconnect();	
	}
	
	
	private static String getNameFromUser(){
		System.out.print("유저 이름을 입력하세요 : ");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			return br.readLine();
		} catch (IOException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
}
