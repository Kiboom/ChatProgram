package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientLauncher {
	private static final int DEFAULT_PORT = 8080;

	public static void main(String[] args) {
		String serverAddress = "52.192.198.85";
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
			System.out.println(e.getMessage());
			return null;
		}
	}
}
