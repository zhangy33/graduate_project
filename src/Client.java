import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;

public class Client {

	// define this client
	private String host = "localhost";
	private int port = 8821;
	

	private int c;
	private int cv;
	private byte[] clientDataBase;

	
	
	// constructor
	public Client(int in_c, int in_cv) {
		// will use the default host and port
		
		c = in_c;
		cv = in_cv;
		
	}

	// constructor
	public Client(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void chat() {
		
		try {
			
			FileWriter  fw = new FileWriter ("rslt.txt", true);
			long t_TT1 = System.nanoTime();
			
			// ***** initialize the in the out sockets *****
			Socket socket = new Socket(host, port); // initialize the socket
			DataOutputStream out = new DataOutputStream(
					socket.getOutputStream());
			out.flush(); // to ensure that the header is sent
			DataInputStream in = new DataInputStream(socket.getInputStream());

			
			
			// ***** submit the request *****
			// get the request package
			long t_act_subReq1 = System.nanoTime();
			byte[] packSub_byte = ClientCo.getRequestPackage();
			long t_act_subReq2 = System.nanoTime();

			// send the package
			out.writeInt(packSub_byte.length);
			out.flush(); // force any buffered byte to be written out the the stream
			out.write(packSub_byte);
			out.flush();
			
			
			
			// ***** receive the reply package and make update*****
			// receive the reply (processed) package
			byte[] packRep_byte = new byte[0];
			byte[] buff = new byte[SDK.INBUFF];
			int k = -1;
		    while((k = in.read(buff, 0, buff.length)) > -1) {
		        byte[] tbuff = new byte[packRep_byte.length + k]; // temp buffer size = bytes already read + bytes last read
		        System.arraycopy(packRep_byte, 0, tbuff, 0, packRep_byte.length); // copy previous bytes
		        System.arraycopy(buff, 0, tbuff, packRep_byte.length, k);  // copy current lot
		        packRep_byte = tbuff; // call the temp buffer as the result buff
		    }
		    System.out.println("2. Client read reply: " + packRep_byte.length + " byte");
		    
		    // SHUT DOWN SOCKET INPUT
		    socket.shutdownInput();
		     
			// make update
		    long t_act_makUpd1 = System.nanoTime();
			byte[] packUpd_byte = ClientCo.makeUpdatePackage(c, cv,
					clientDataBase, packRep_byte);
			long t_act_makUpd2 = System.nanoTime();
			// update client
			clientDataBase = packUpd_byte;
			// send the updates to server
			out.write(packUpd_byte);
			out.flush();
			System.out.println("3. Client sent updates: " + packUpd_byte.length + " byte");
			
			// SHUT DOWN SOCKET OUTPUT
			socket.shutdownOutput();
			
			
			
			// ***** done *****
			System.out.println();

			if (!socket.isClosed()) {
				in.close();
				out.close();
				socket.close();
			}
			
			long t_TT2 = System.nanoTime();
			
			
			
			// ***** receive info *****
			Long t_TT = new Long(t_TT2 - t_TT1);
			Long t_act = new Long( (t_act_subReq2 - t_act_subReq1) + (t_act_makUpd2 - t_act_makUpd1) );
			
			fw.write("Client " + c + ":     " );
			fw.write(t_TT.toString());
			fw.write("     ");
			fw.write(t_act.toString());
			fw.write(System.getProperty("line.separator"));
			
			fw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	} // end of chat()

} // end of current class