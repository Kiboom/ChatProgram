package message;

import java.io.*;
public class Message implements Serializable {

	/**
	 클라이언트가 전송하는 메시지의 종류를 정의함
	 MESSAGE 일반 메시지
	 LOGOUT 로그아웃 메시지
	 */
	public static final int MESSAGE = 0, LOGOUT = 1;
	private int type;
	private String message;
	protected static final long serialVersionUID = 1112122200L;
	
	
	public Message(int type, String message) {
		this.type = type;
		this.message = message;
	}
	

	public int getType() {
		return type;
	}
	public String getMessage() {
		return message;
	}
}
