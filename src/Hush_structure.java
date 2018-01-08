import java.net.InetAddress;

public class Hush_structure {

	
	
		// Save the structure Class
		int oldID;
		int port;
		InetAddress addr;
		
		public Hush_structure(int oldID, int port, InetAddress addr) {
			this.oldID = oldID;
			this.port = port;
			this.addr = addr;
		}
		
		public int getOldID() {
			return oldID;
		}

		public int getPort() {
			return port;
		}

		public InetAddress getAddr() {
			return addr;
		}
	

}
