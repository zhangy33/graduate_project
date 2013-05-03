import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Server {
	
	// communication field
	private int port = 8821;
    private ServerSocket serverSocket;
    private ExecutorService executorService; // thread pool
	private Lock lock = new ReentrantLock(); // to prevent deadlock
	
	// data base field
	private byte[] serverDataBase;

	// constructor
	public Server() {}

	
	// start the service
	public void startService() throws IOException {
		int i = 0;

		this.serverSocket = new ServerSocket(port);
		executorService = Executors.newSingleThreadExecutor();
		System.out.println("Server started up.");
		
		// 1st: let the ServerCo update the protocol name
		ServerCo.getProtocolName();

		// 2nd: initialize the server
		serverDataBase = ServerCo.initServerDataBase();

		
		while (true) {

			try {

				Socket socket = null;
				// the .accept() will be activated when a client trying to
				// connect this server
				socket = this.serverSocket.accept();
				
				
				i++; // connection counter
				System.out.println(i + " of connections.");
				
				// 1. connect the current client to a thread(input socket and i)
				// 2. input the server's data base
				// 3. output the new server's data base for updating
				Future<byte[]> futureServerDataBase = executorService.submit(new Handler(socket, serverDataBase));
				byte[] newServerDataBase = (byte[]) futureServerDataBase.get();
				
				serverDataBase = newServerDataBase;

				if (!socket.isClosed()) {
					socket.close();
				}

				System.out.println();
				

			} catch (IOException | InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}

		}

	}

	
	
	// substitute the server data base
	
	
	// implement the Callable interface
	public class Handler implements Callable<byte[]> {
		
		// define the thread fields
		// client info
		private Socket socket; // socket
		// local server data base copy
		private byte[] serverDataBase_ThreadCopy;
		
		
		// constructor of the current thread
		public Handler(Socket socket, byte[] serverDataBase) {
			
			// copy client info to local
			this.socket = socket;
			
			// copy server data base to local
			serverDataBase_ThreadCopy = serverDataBase;
			
		}

		// override the call() method
		@Override
		public byte[] call() {
			lock.lock();
			try {
				
				FileWriter  fw = new FileWriter ("rslt.txt", true);
				long t_TT1 = System.nanoTime();
				
				// ***** initialize the in the out sockets *****
				// get input and output streams
				DataOutputStream out = new DataOutputStream(
						socket.getOutputStream());
				out.flush(); // to ensure that the header is sent
				DataInputStream in = new DataInputStream(
						socket.getInputStream());
				
				

				// ***** reply the request *****
				// receive the package
				int sizePackReq = in.readInt(); // receive request package size
				byte[] packReceived_byte = new byte[sizePackReq];
				in.read(packReceived_byte); // get the request package
				
				// process and reply the request
				long t_act_repReq1 = System.nanoTime();
				byte[] packReply_byte = ServerCo.replyRequest(packReceived_byte, serverDataBase_ThreadCopy);
				long t_act_repReq2 = System.nanoTime();
				out.write(packReply_byte);
				out.flush(); // force any buffered byte to be written out the the stream
				System.out.println("1. Server sent reply: " + packReply_byte.length + " byte");
				
				// SHUT DOWN SOCKET OUTPUT !!!KEY WORD TO AVOID DEAD LOCK!!!
				// otherwise, the client will keep waiting for the reply message
				socket.shutdownOutput();
				
				
				
				// ***** receive the updating package and update server *****
				// receive the update package
				byte[] packUpd_byte = new byte[0];
				byte[] buff = new byte[SDK.INBUFF];
				int k = -1;
			    while((k = in.read(buff, 0, buff.length)) > -1) {
			        byte[] tbuff = new byte[packUpd_byte.length + k]; // temp buffer size = bytes already read + bytes last read
			        System.arraycopy(packUpd_byte, 0, tbuff, 0, packUpd_byte.length); // copy previous bytes
			        System.arraycopy(buff, 0, tbuff, packUpd_byte.length, k);  // copy current lot
			        packUpd_byte = tbuff; // call the temp buffer as the result buff
			    }
			    System.out.println("4. Server read updates: " + packUpd_byte.length + " byte");
			    
			    // // SHUT DOWN SOCKET inPUT
			    socket.shutdownInput();
			    
				// update the server data base
			    long t_act_updSer1 = System.nanoTime();
				byte[] newServerDataBase = ServerCo.updateServerDataBase(packUpd_byte, serverDataBase_ThreadCopy);
				long t_act_updSer2 = System.nanoTime();
				 
				
				
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
				Long t_act = new Long( (t_act_repReq2 - t_act_repReq1) + (t_act_updSer2 - t_act_updSer1) );
				
				fw.write("Server:     " );
				fw.write(t_TT.toString());
				fw.write("     ");
				fw.write(t_act.toString());
				fw.write(System.getProperty("line.separator"));
				
				fw.close();
				
				
				return newServerDataBase;

			} catch (IOException e) {
				e.printStackTrace();
			}
			lock.unlock();
			
			return null;
		}
		
	}
	 
} // end of this class file