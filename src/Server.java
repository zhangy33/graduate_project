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
	private int port = 7822;
    private ServerSocket serverSocket;
    private ExecutorService executorService; // thread pool
	private Lock lock = new ReentrantLock(); // to prevent deadlock
	
	// data base field
	private byte[] serverDataBase;

	// constructor
	public Server() {}

	
	// start the service
	public void startService() throws IOException {
		
		int connectCount = 0;

		this.serverSocket = new ServerSocket(port);
		executorService = Executors.newSingleThreadExecutor();
		System.out.println("Server started up.");

		
		// Let ServerCo update the client number, protocol name,and pubKey
		ServerCo.getClientNumProtocolNameAndPubKey();

		for (int l = 0; l < ServerCo.ProtocolsNum; ++l) 
		{
			ServerCo.curProtocolName = ServerCo.protocols.get(l);
			for (int k = ServerCo.minClients; k <= ServerCo.maxClients; k = k + ServerCo.incClients) // client # loop
			{
				ServerCo.curClientNum = k;
				for (int j = 0; j < ServerCo.runsNum; ++j) // run # loop
				{
					// initialize the server
					serverDataBase = ServerCo.initServerDataBase();

					// operation # = client # * ( write # + read # )
					int ops = ServerCo.curClientNum
							* (ServerCo.writesNum + ServerCo.readsNum);

					while (ops-- > 0) {

						try {

							// .accept() will be activated when a client trying
							// to
							// connect this server
							Socket socket = this.serverSocket.accept();

							connectCount++; // connection counter
							System.out.println(connectCount
									+ " of connections.");

							// 1. connect the current client to a thread(input
							// socket
							// and i)
							// 2. input the server's data base
							// 3. output the new server's data base for updating
							Future<byte[]> futureServerDataBase = executorService
									.submit(new Handler(socket, serverDataBase,
											connectCount));
							byte[] newServerDataBase = (byte[]) futureServerDataBase
									.get();

							// update the server data base
							serverDataBase = newServerDataBase;

							if (!socket.isClosed()) {
								socket.close();
							}

							System.out.println();

						} catch (IOException | InterruptedException
								| ExecutionException e) {
							e.printStackTrace();
						}

					} // end of while
				} // end of run # loop
			} // end of client # loop
		} // end of protocol # loop


		
		// close the server
		executorService.shutdownNow();
		this.serverSocket.close();

	}

	
	
	// substitute the server data base
	
	
	// implement the Callable interface
	public class Handler implements Callable<byte[]> {
		
		// define the thread fields
		// client info
		private Socket socket; // socket
		// local server data base copy
		private byte[] serverDataBase_ThreadCopy;
		// connection count
		private int connectCount = 0;
		
		public String o;
		
		
		// constructor of the current thread
		public Handler(Socket socket, byte[] serverDataBase, int in_connectCount) {
			
			// copy client info to local
			this.socket = socket;
			
			// copy server data base to local
			serverDataBase_ThreadCopy = serverDataBase;
			
			this.connectCount = in_connectCount;
			
		}

		// override the call() method
		@Override
		public byte[] call() {
			lock.lock();
			try {
				
				FileWriter  fw1 = new FileWriter ("serverRsltSocket.txt", true);
				FileWriter  fw2 = new FileWriter ("serverRsltWoSocket.txt", true);
				long t_TT_temp = 0;
				long t_TT = 0;
				long t_Socket_temp = 0;
				long t_Socket = 0;
				long t_WoSocket_temp = 0;
				long t_WoSocket = 0;
				
				
				t_TT_temp = System.nanoTime();
				t_Socket_temp = System.nanoTime();
				t_WoSocket_temp = System.nanoTime();
				// ***** initialize the in the out sockets *****
				// get input and output streams
				DataOutputStream out = new DataOutputStream(
						socket.getOutputStream());
				out.flush(); // to ensure that the header is sent
				DataInputStream in = new DataInputStream(
						socket.getInputStream());
				
				

				// ***** reply the request *****
				this.o = in.readUTF(); // read the current operation
				
				// receive the package
				int sizePackReq = in.readInt(); // receive request package size
				byte[] packReceived_byte = new byte[sizePackReq];
				in.read(packReceived_byte); // get the request package
				
				// process and reply the request
				byte[] packReply_byte = ServerCo.replyRequest(packReceived_byte, serverDataBase_ThreadCopy);
				out.write(packReply_byte);
				out.flush(); // force any buffered byte to be written out the the stream
				//System.out.println("1. Server sent reply: " + packReply_byte.length + " byte");
				
				// SHUT DOWN SOCKET OUTPUT !!!KEY WORD TO AVOID DEAD LOCK!!!
				// otherwise, the client will keep waiting for the reply message
				socket.shutdownOutput();
				t_Socket += (System.nanoTime() - t_Socket_temp);
				t_WoSocket += (System.nanoTime() - t_WoSocket_temp);
				
				
				
				// ***** receive the updating package and update server *****
				// receive the update package
				byte[] packUpd_byte = new byte[0];
				byte[] buff = new byte[SDK.INBUFF];
				int k = -1;
				int flag = 1;
			    while((k = in.read(buff, 0, buff.length)) > -1) {
			    	if (flag == 1) {
			    		t_Socket_temp = System.nanoTime();
			    		t_WoSocket_temp = System.nanoTime();
			    		flag = 0;
			    	}
			        byte[] tbuff = new byte[packUpd_byte.length + k]; // temp buffer size = bytes already read + bytes last read
			        System.arraycopy(packUpd_byte, 0, tbuff, 0, packUpd_byte.length); // copy previous bytes
			        System.arraycopy(buff, 0, tbuff, packUpd_byte.length, k);  // copy current lot
			        packUpd_byte = tbuff; // call the temp buffer as the result buff
			    }
			    //System.out.println("4. Server read updates: " + packUpd_byte.length + " byte");
			    
			    // // SHUT DOWN SOCKET inPUT
			    socket.shutdownInput();
			    
				// update the server data base
				byte[] newServerDataBase = ServerCo.updateServerDataBase(this.o, packUpd_byte, serverDataBase_ThreadCopy);
				 
				
				
				// ***** done *****
				System.out.println();
				
				t_WoSocket += (System.nanoTime() - t_WoSocket_temp);
				
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
				Long comSize = new Long(packReceived_byte.length + packReply_byte.length + packUpd_byte.length);
				
				
				fw1.write("Server connection " + connectCount + ":" );
				fw1.write(System.getProperty("line.separator")); // new line
				fw1.write("tTT: " + t_TT_obj.toString() + ",   ");
				fw1.write("t_Socket: " + t_Socket_obj.toString() + ",   ");
				fw1.write("comSize: " + comSize.toString());
				fw1.write(System.getProperty("line.separator"));
				fw1.write(System.getProperty("line.separator"));
				
				fw2.write("Server connection " + connectCount + ":" );
				fw2.write(System.getProperty("line.separator")); // new line
				fw2.write("tTT: " + t_TT_obj.toString() + ",   ");
				fw2.write("t_WoSocket: " + t_WoSocket_obj.toString() + ",   ");
				fw2.write("comSize: " + comSize.toString());
				fw2.write(System.getProperty("line.separator"));
				fw2.write(System.getProperty("line.separator"));
				
				fw1.close();
				fw2.close();
				
				
				return newServerDataBase;

			} catch (IOException e) {
				e.printStackTrace();
			}
			lock.unlock();
			
			return null;
		}
		
	}
	 
} // end of this class file