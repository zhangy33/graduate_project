import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientCo {
	
	// the current protocol name
	public static String protocolName = "null";
	
	// client number
	public static int CLIENT_NUM = SDK.CLIENT_NUM;
	
	// current operation
	public static String cur_o = "w";
	
	// define this client
	private static String hostCo = "localhost";
	private static int portCo = 4700;

	
	public static void main(String[] args) throws IOException {

		// init multiple clients
		List<Client> clients;
		clients = new ArrayList<Client>(CLIENT_NUM);
		for (int i=0; i<CLIENT_NUM; ++i) {
			Client client = new Client(i, 1);
			clients.add(client);
		}
		
		// communicate with ServerCo to send the current protocol name for reflection
		protocolName = "SUNDR"; // should read from the config.txt
		Socket socketCo = null;
		try {
			socketCo = new Socket(hostCo, portCo);
			DataOutputStream outCo = new DataOutputStream(socketCo.getOutputStream());
			outCo.flush();
			DataInputStream inCo = new DataInputStream(socketCo.getInputStream());
			outCo.writeUTF(protocolName); // send out the protocol name
			String protocolName_trans_rslt = inCo.readUTF(); // receive the feedback of the serverCo
			System.out.println("The ServerCo got the protocolName: " + protocolName_trans_rslt);
			socketCo.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// for each client
		for (int i=0;i<CLIENT_NUM;++i) {
			// start the communication
			clients.get(i).chat();
		}
		for (int i=0;i<CLIENT_NUM;++i) {
			// start the communication
			clients.get(i).chat();
		}
		
	}

	/* ***** The followings are the static functions get from the protocol. They will be called by Client. ***** */
	
	public static byte[] getRequestPackage() {
		
		try {
			// load the protocol at runtime
			Class<?> cls_makeRequest = Class.forName(protocolName);
			Object obj_makeRequest = cls_makeRequest.newInstance();
			
			// define the input parameters: int c, String o in the protocol
			@SuppressWarnings("rawtypes")
			Class[] param_makeRequest = {int.class,String.class,int.class};
			
			// get the makeRequest method at runtime
			Method method = cls_makeRequest.getDeclaredMethod("makeRequest", param_makeRequest );
			
			// call the makeRequest method
			byte[] RequestPackage = (byte[]) method.invoke(obj_makeRequest, 1, "w", 6);
			
			return RequestPackage;
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}
	
	public static byte[] makeUpdatePackage(int c, int cv, byte[] clientDataBase, byte[] serverDataBase) {
		
		try {
			// load the protocol at runtime
			Class<?> cls_makeUpdate = Class.forName(protocolName);
			Object obj_makeUpdate = cls_makeUpdate.newInstance();
			
			// define the input parameters: int c, int cv, byte[] serverDataBase
			@SuppressWarnings("rawtypes")
			Class[] param_makeUpdate = {int.class,int.class,String.class,byte[].class,byte[].class};
			
			// get the makeUpdate method at runtime
			Method method = cls_makeUpdate.getDeclaredMethod("makeUpdate", param_makeUpdate );
			
			// call the makeUpdate method
			byte[] UpdatePackage = (byte[]) method.invoke(obj_makeUpdate, c, cv, cur_o, clientDataBase, serverDataBase);
			
			return UpdatePackage;
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}
	

}
