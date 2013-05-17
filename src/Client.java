import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.security.PrivateKey;

public class Client {

	// define this client
	private String host = "localhost";
	private int port = 7822;
	

	public int c;
	public int cv;
	public byte[] clientDataBase;
	public PrivateKey priv;
	public String o;

	
	
	// constructor
	public Client(int in_c, int in_cv, PrivateKey in_priv) {
		// will use the default host and port
		
		c = in_c;
		cv = in_cv;

		this.priv = in_priv;
		
		// init the client data base
		clientDataBase = ClientCo.initClientDataBase(in_c);

	}


	public void chat() {
		
		try {
		
			FileWriter  fw1 = new FileWriter ("clientRsltSocket.txt", true);
			FileWriter  fw2 = new FileWriter ("clientRsltWoSocket.txt", true);
			long t_TT_temp = 0;
			long t_TT = 0;
			long t_Socket_temp = 0;
			long t_Socket = 0;
			long t_WoSocket_temp = 0;
			long t_WoSocket = 0;
			
			
			t_TT_temp = System.nanoTime();
			t_Socket_temp = System.nanoTime();
			// ***** initialize the in the out sockets *****
			Socket socket = new Socket(host, port); // initialize the socket
			t_WoSocket_temp = System.nanoTime();
			DataOutputStream out = new DataOutputStream(
					socket.getOutputStream());
			out.flush(); // to ensure that the header is sent
			DataInputStream in = new DataInputStream(socket.getInputStream());

			
			// ***** submit the request *****
			this.o = ClientCo.cur_o;
			out.writeUTF(this.o); // send the operation type
			out.flush(); // force any buffered byte to be written out the the stream
			
			// get the request package
			byte[] packSub_byte = ClientCo.getRequestPackage();

			// send the package
			out.writeInt(packSub_byte.length);
			out.flush();
			out.write(packSub_byte);
			out.flush();
			t_Socket += (System.nanoTime() - t_Socket_temp);
			t_WoSocket += (System.nanoTime() - t_WoSocket_temp);
			
			
			// ***** receive the reply package and make update*****
			// receive the reply (processed) package
			byte[] packRep_byte = new byte[0];
			byte[] buff = new byte[SDK.INBUFF];
			int k = -1;
			int flag = 1;
		    while((k = in.read(buff, 0, buff.length)) > -1) {
		    	if (flag == 1) {
		    		t_Socket_temp = System.nanoTime();
		    		t_Socket_temp = System.nanoTime();
		    		flag = 0;
		    	}
		        byte[] tbuff = new byte[packRep_byte.length + k]; // temp buffer size = bytes already read + bytes last read
		        System.arraycopy(packRep_byte, 0, tbuff, 0, packRep_byte.length); // copy previous bytes
		        System.arraycopy(buff, 0, tbuff, packRep_byte.length, k);  // copy current lot
		        packRep_byte = tbuff; // call the temp buffer as the result buff
		    }
		    //System.out.println("2. Client read reply: " + packRep_byte.length + " byte");
		    
		    // SHUT DOWN SOCKET INPUT
		    socket.shutdownInput();
		     
		    
			// make update
			byte[] packUpd_byte = ClientCo.makeUpdatePackage(c, cv, this.o,
					clientDataBase, packRep_byte, this.priv);
			// update client
			clientDataBase = packUpd_byte;
			// send the updates to server
			out.write(packUpd_byte);
			out.flush();
			
			//System.out.println("3. Client sent updates: " + packUpd_byte.length + " byte");
			
			
			// SHUT DOWN SOCKET OUTPUT
			socket.shutdownOutput();
			t_WoSocket += (System.nanoTime() - t_WoSocket_temp);
			
			
			// ***** done *****
			//System.out.println();

			if (!socket.isClosed()) {
				in.close();
				out.close();
				socket.close();
			}
			
			t_TT += (System.nanoTime() - t_TT_temp);
			t_Socket += (System.nanoTime() - t_Socket_temp);
			
			
			
			// ***** receive info *****
			Long t_TT_obj = new Long(t_TT);
			Long t_Socket_obj = new Long(t_Socket);
			Long t_WoSocket_obj = new Long(t_WoSocket);
			Long comSize = new Long(packSub_byte.length + packRep_byte.length + packUpd_byte.length);
			
			
			fw1.write("Client " + c + ":" );
			fw1.write(System.getProperty("line.separator")); // new line
			fw1.write("tTT: " + t_TT_obj.toString() + ",   ");
			fw1.write("t_Socket: " + t_Socket_obj.toString() + ",   ");
			fw1.write("comSize: " + comSize.toString());
			fw1.write(System.getProperty("line.separator"));
			fw1.write(System.getProperty("line.separator"));
			
			fw2.write("Client " + c + ":" );
			fw2.write(System.getProperty("line.separator")); // new line
			fw2.write("tTT: " + t_TT_obj.toString() + ",   ");
			fw2.write("t_WoSocket: " + t_WoSocket_obj.toString() + ",   ");
			fw2.write("comSize: " + comSize.toString());
			fw2.write(System.getProperty("line.separator"));
			fw2.write(System.getProperty("line.separator"));
			
			fw1.close();
			fw2.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	} // end of chat()

} // end of current class