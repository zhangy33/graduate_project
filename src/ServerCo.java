import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerCo {
	
	// the current protocol name
	public static String curProtocolName = "null";
	
	// communicate with the ClientCo
	private static int portCo = 4700;
	
	public static int CLIENT_NUM = SDK.CLIENT_NUM;
	
	public static void main(String[] args) {

		// conduct the Server
		Server server = new Server();
		try {
			server.startService();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/* ***** The followings are the static functions get from the protocol. They will be called by Server. ***** */
	
	// update the protocol name
	public static void getProtocolName() {
		
		// communicate with the Client
		ServerSocket serverSocketCo;
		try {
			serverSocketCo = new ServerSocket(portCo);
			Socket socketCo = serverSocketCo.accept();
			DataOutputStream outCo = new DataOutputStream(
					socketCo.getOutputStream());
			outCo.flush();
			DataInputStream inCo = new DataInputStream(
					socketCo.getInputStream());
			String protocolName_fromClient = inCo.readUTF(); // get the protocol name form the ClientCo
			System.out.println("The protocolName got from the ClientCo is: "
					+ protocolName_fromClient);
			if (protocolName_fromClient.equals("SUNDR")) { // give the feedback to the ClientCo about the validity
				outCo.writeUTF("true");
			} else {
				outCo.writeUTF("false");
			}
			socketCo.close();
			serverSocketCo.close();

			// copy the protocol name to the local variable
			curProtocolName = protocolName_fromClient;

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	// update the protocol name
	public static byte[] initServerDataBase() {
		// load the protocol at runtime
				Class<?> cls_initializeServerDataBas;
				try {
					// load the protocol at run time
					cls_initializeServerDataBas = Class.forName(curProtocolName);
					Object obj_initializeServerDataBas = cls_initializeServerDataBas.newInstance();
					
					
					// get the initializeServerDataBas method at runtime
					Method method = cls_initializeServerDataBas.getDeclaredMethod(
							"initializeServerDataBase", int.class);

					// call the initializeServerDataBas method
					byte[] serverDataBase = (byte[]) method.invoke(obj_initializeServerDataBas, CLIENT_NUM);
					
					return serverDataBase;
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				
			return null;

	}

	public static byte[] replyRequest(byte[] packReceived_byte, byte[] serverDataBase) {

		// load the protocol at runtime
		Class<?> cls_processRequest;
		try {
			// load the protocol at run time
			cls_processRequest = Class.forName(curProtocolName);
			Object obj_processRequest = cls_processRequest.newInstance();

			// define the input parameters: byte[], SDK.ServerDataBase in the protocol
			@SuppressWarnings("rawtypes")
			Class[] param_processRequest = { byte[].class, byte[].class };

			// get the processRequest method at runtime
			Method method = cls_processRequest.getDeclaredMethod(
					"processRequest", param_processRequest);

			// call the processRequest method
			byte[] packReply_byte = (byte[]) method.invoke(obj_processRequest, packReceived_byte, serverDataBase);
			
		
			
			
			
			return packReply_byte;
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public static byte[] updateServerDataBase(byte[] packUpdateServer, byte[] serverDataBase) {

		// load the protocol at runtime
		Class<?> cls_updateServerDataBase;
		try {
			// load the protocol at run time
			cls_updateServerDataBase = Class.forName(curProtocolName);
			Object obj_updateServerDataBase = cls_updateServerDataBase.newInstance();

			// define the input parameters: byte[], SDK.ServerDataBase in the protocol
			@SuppressWarnings("rawtypes")
			Class[] param_updateServerDataBase = { byte[].class, byte[].class };

			// get the processRequest method at runtime
			Method method = cls_updateServerDataBase.getDeclaredMethod(
					"updateServer", param_updateServerDataBase);

			// call the processRequest method
			byte[] newServerDataBase = (byte[]) method.invoke(obj_updateServerDataBase, packUpdateServer, serverDataBase);

			
			return newServerDataBase;
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return null;
	}


}
