import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

// Serialize the object to byte array or vice versa
import java.io.IOException;
import java.io.Serializable;


public class SUNDR implements Serializable, NewProtocol  {

	private static final long serialVersionUID = -4078257594186693768L;


	// define the class for ClientDataBase
	public class ClientDataBase implements Serializable {

		private static final long serialVersionUID = 8720439442926157434L;
		public int c; // client #
		public int cv; // value
		public byte[] VS; // VS
		public byte[] Sig; // Sig

		public ClientDataBase() {
		}
		
		public ClientDataBase(int in_c, int in_cv, byte[] in_VS, byte[] in_Sig) {
			this.c = in_c;
			this.cv = in_cv;
			this.VS = in_VS;
			this.Sig = in_Sig;
		}
	}

	// define the class for ServerDataBase
	public class ServerDataBase implements Serializable {

		private static final long serialVersionUID = -3574385112881198245L;
		public int cur_server_value; // current server value
		public List<byte[]> VSL; // VS list
		public List<byte[]> signedVSL; // sig list

		public ServerDataBase() {
		}
		
		public ServerDataBase(int in_cur_server_value, List<byte[]> in_VSL, List<byte[]> in_signedVSL) {
			this.cur_server_value = in_cur_server_value;
			this.VSL = in_VSL;
			this.signedVSL = in_signedVSL;
		}
	}
	/*
	// define the class for VS
	public class VSstruct implements Serializable{


		
		private static final long serialVersionUID = 1208091541999970479L;
		String type = "VRS"; // type is usually VRS
		byte[] cvArr; // hash value of cv
		int c; // client number
		int[] pairs; // version pairs
		
		 in_c: incoming client #
		 * in_cv: incoming value
		 * in_CLIENT_TTNUM: incoming total client number		
		public VSstruct (int in_c, int in_cv, int in_CLIENT_TTNUM) {
			try {
				// cvByte
				byte[] cvByte = new byte[4]; 
				for (int i = 0; i < 4; ++i) {
					cvByte[i] = (byte) (in_cv >>> (i * 8));
				}
				MessageDigest md = MessageDigest.getInstance("MD5");
				this.cvArr = md.digest(cvByte);
				// c
				this.c = in_c;
				// pairs
				pairs = new int[2*in_CLIENT_TTNUM];
				for (int i = 0; i < in_CLIENT_TTNUM; ++i) {
					pairs[2*i] = i; // client #
					pairs[2*i + 1] = 0; // corresponding version #
				}
			} catch (NoSuchAlgorithmException e) {
				System.out.println("VSstruct constructor error!");
				e.printStackTrace();
			}
		}
		
	}
	*/

	// define the class for packSub
	public class PackSubClass implements Serializable {


		/**
		 * 
		 */
		private static final long serialVersionUID = 3776778181626364672L;
		private int c; // client #
		private String o; // operation w or r
		int cv; // current value

		// constructor and functions to return the fields
		public PackSubClass(int in_c, String in_o, int in_cv) {
			this.c = in_c;
			this.o = in_o;
			this.cv = in_cv;
		}
	}
	
	
	
	@Override
	public byte[] initializeClientDataBase(int client_num, int CLIENT_TTNUM) {

		ClientDataBase clientDataBase = new ClientDataBase();

		// init data base:
		// client #
		clientDataBase.c = client_num;
		// value
		clientDataBase.cv = 0;
		// VS
		/*
		VSstruct localVSstruc = new VSstruct(client_num, 0, CLIENT_TTNUM);
		byte[] localVSstrucByte = SDK.serialize(localVSstruc);
		*/
		String test = "testing";
		byte[] localVSstrucByte = test.getBytes();
		clientDataBase.VS = localVSstrucByte;
		// sig
		clientDataBase.Sig = localVSstrucByte;// !!!!!!! to be continue
												 
		return SDK.serialize(clientDataBase);
		
	}

	@Override
	public byte[] initializeServerDataBase(int CLIENT_TTNUM){
		
		ServerDataBase serverDataBase = new ServerDataBase();
		
		// allocate memory for the data base
		serverDataBase.cur_server_value = 0;
		serverDataBase.VSL = new ArrayList<byte[]>(CLIENT_TTNUM);
		serverDataBase.signedVSL = new ArrayList<byte[]>(CLIENT_TTNUM);
		
		
		// the initial values are all zeros
		int i;
		for (i = 0; i < CLIENT_TTNUM; ++i) {
			
			/*
			VSstruct localVSstruc = new VSstruct(i, 0, CLIENT_TTNUM);
			byte[] localVSstrucByte = SDK.serialize(localVSstruc);
			*/
			String test = "testing";
			byte[] localVSstrucByte = test.getBytes();

			serverDataBase.VSL.add(i, localVSstrucByte);
			serverDataBase.signedVSL.add(i, localVSstrucByte); // !!!!!!! to be continue
		}
	
		return SDK.serialize(serverDataBase);
		
	}
	
	
	@Override
	public byte[] makeRequest(int c, String o, int cv) {

		PackSubClass packSubClass = new PackSubClass(c, o, cv);
		
		System.out.println("Protocal log: Request was made.");
		return SDK.serialize(packSubClass);

	}

	@Override
	public byte[] processRequest(byte[] Req, byte[] serverDataBase){
		
		try {
			// de-serialize the request
			PackSubClass packSubClass = (PackSubClass) SDK.deserialize( Req );
			
			/*
			System.out.println(packSubClass.c);
			System.out.println(packSubClass.o);
			System.out.println(packSubClass.cv);
			
			
			
			// testing!!!
			try {
				ServerDataBase test;
				test = ( ServerDataBase ) SDK.deserialize(serverDataBase);
				byte[] test_VSLbyte = test.VSL.get(1);
				VSstruct test_VSL;
				test_VSL = ( VSstruct ) SDK.deserialize(test_VSLbyte);
				for (int i = 0;i<20;++i)
				{
				System.out.print( test_VSL.pairs[i] + " ");
				}
				System.out.println();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			
			System.out.println(serverDataBase.length);
			*/
			System.out.println("Protocal log: Request was processed. Server data base size: " + serverDataBase.length);
			return serverDataBase;
			
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
			
	}

	@Override
	public byte[] makeUpdate(int c, int cv, String o, byte[] clientDataBase, byte[] serverDataBase) {
			
		try {
			// de-serialize the server data base
			ServerDataBase serverDataBase_local;
			serverDataBase_local = (ServerDataBase) SDK.deserialize(serverDataBase);
			
			// verify its own sig
			byte[] mySig = serverDataBase_local.signedVSL.get(c);
			
			// verify others' sig
			/*
			// create new VS
			String type = "VRS";
			byte[] typeArr = type.getBytes();
			
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] cvArr = md.digest( BigInteger.valueOf(cv).toByteArray() );
			
			byte[] uArr = BigInteger.valueOf(c).toByteArray();
			
			
			
			
			// verfy VS are ordered
			
			// generate new sig
			
			
			// 
			*/
			
			byte[] myVS = serverDataBase_local.VSL.get(c);
			
			ClientDataBase packUpdateServer = new ClientDataBase(c, cv, myVS, mySig );
			
			System.out.println("Protocal log: Update was made.");
			return SDK.serialize(packUpdateServer);
			
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		} /*catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}*/
		
		return null;
		
	}

	
	@Override
	public byte[] updateServer(byte[] packUpdateServer, byte[] serverDataBase) {
		try {
			// de-serialize the server data base
			ServerDataBase serverDataBase_local= (ServerDataBase) SDK.deserialize(serverDataBase);

			// de-serialize the request
			ClientDataBase packUpdateServer_local = (ClientDataBase) SDK.deserialize(packUpdateServer);
			
			// update local server data base value
			serverDataBase_local.cur_server_value = packUpdateServer_local.cv;

			/*
			// update local server data base's VSL
			byte[] test_VSLbyte = serverDataBase_local.VSL.get(1);
			VSstruct test_VSL = ( VSstruct ) SDK.deserialize(test_VSLbyte);
			test_VSL.pairs[1] ++;
			test_VSLbyte = SDK.serialize(test_VSL);
			serverDataBase_local.VSL.set(1, test_VSLbyte);
			*/
			serverDataBase_local.VSL.set(packUpdateServer_local.c,
					packUpdateServer_local.VS);

			// update local server data base's sig
			serverDataBase_local.signedVSL.set(packUpdateServer_local.c,
					packUpdateServer_local.Sig);

			// return the local server data base
			System.out.println("Protocal log: Server was updated.");
			return SDK.serialize(serverDataBase_local);
				
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
