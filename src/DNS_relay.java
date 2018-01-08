
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class DNS_relay {
	
	InetAddress server_addr;	
	
	InetAddress client_addr;
	int client_port;
	
	DatagramSocket DNSsocket; //build the socket instance which can receive and send UDP
	byte receive_buff[] =  new byte[1024];
	DatagramPacket receive_package = new DatagramPacket(receive_buff, receive_buff.length); //deal with message   break into byte array
	
	String domain_name = "";
	int data_pos;
	//store the corresponding client’s address and port.
	Map<Short, Hush_structure> PackageMap = new HashMap<Short, Hush_structure>();

	public void ini() throws Exception
	{
		try {
			DNSsocket = new DatagramSocket(53);//Generate a new DNS socket and bind it to the port 53
		} catch (SocketException e)
		{
			System.out.println("Cannot open port!!");
		}				
		server_addr = InetAddress.getByName("8.8.8.8");//Set the server address is 8.8.8.8
		System.out.println("************************ BEGIN ************************");
	
		while(true)
		{
			DNSsocket.receive(receive_package);
			//return IP address of the machine which receive or send message
			client_addr = receive_package.getAddress();
			//return IP port number of the machine which receive or send message
			client_port = receive_package.getPort();
			//return buffer of the data
			receive_buff = receive_package.getData();  
			
			if (((receive_buff[2] & 0x80) == 0x00)) //judge QUERY
			{
				System.out.println("\n The Receive Time is ： " + new java.util.Date());//print receive time
				//Analysis Begin
				data_pos = 12;
				int data_length;
				data_length= byte2Int(receive_buff, data_pos);
				data_pos++;

				while (data_length != 0) 
				{
					domain_name = domain_name + byte2String(receive_buff, data_pos, data_length) + ".";
					data_pos += data_length;	
					data_length = byte2Int(receive_buff, data_pos);
					data_pos++;
				}
				//Analysis Over
				
				System.out.println("The Domain Name is : " + domain_name);//Print the domain name
				
				judge_local judge_loc = new judge_local();
				String judge_string = domain_name.substring(0, domain_name.length() - 1);
				byte[] judge_flag = judge_loc.judge(judge_string);
				
				if(judge_flag != null)//Judge the local database
				{
					System.out.println("@In the Local Database.");
					int judge0000 = 0;
					
					for (int i = 0; i < 4; i++) 
						if (judge_loc.judge(domain_name.substring(0, domain_name.length() - 1))[i] == 0)
							judge0000++;
					
					if (judge0000 == 4)//if 0.0.0.0 -->shield
					{// flag=0x8183
						System.out.println("@Shield.");
						System.out.println("@Shield. Send the package to the client!");
						receive_buff[2] = (byte) (receive_buff[2] | 0x81);//O+       10000001 response . not authoritative name server.  request recursive service by the name server.
						receive_buff[3] = (byte) (receive_buff[3] | 0x83);//		   10000011  Recursion Available.  Name in query does not exist.
						DatagramPacket response_package = new DatagramPacket(receive_buff, receive_buff.length, client_addr,
								client_port);
						DNSsocket.send(response_package);
						System.out.println("@Shield. Send the package to the client------Successfully!");
					}
					
					
					else if(receive_buff[data_pos] == 0x00 && receive_buff[data_pos + 1] == 0x01)
					{//IPV4
				
						System.out.println("@IPV4.");
					    System.out.println("@IPV4. Return the IP Address!");
						byte[] response = new byte[16];
						byte[] answer = { (byte) 0xc0, (byte) 0x0c, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00,
								(byte) 0x00, (byte) 0x01, (byte) 0x76, (byte) 0x00, (byte) 0x04 };
						byte[] send_buff = new byte[1024];
						// Answer
						System.arraycopy(answer, 0, response, 0, answer.length);
						System.arraycopy(judge_flag, 0, response, answer.length,
								judge_flag.length);
						// add response into message
						System.arraycopy(receive_buff, 0, send_buff, 0, data_pos + 4);
						System.arraycopy(response, 0, send_buff, data_pos + 4, response.length);
						// modify the message head
						send_buff[2] = (byte) (send_buff[2] | 0x81);
						send_buff[3] = (byte) (send_buff[3] | 0x80);//10000000 Recursion Available  no error
						send_buff[6] = (byte) (send_buff[6] | 0x00);//ANCOUNT(Answer count): 
						send_buff[7] = (byte) (send_buff[7] | 0x01);//16-bit field that defines the number of resource records in the answer section.
						// send UDP
						DatagramPacket response_package = new DatagramPacket(send_buff, send_buff.length, client_addr, client_port);
						DNSsocket.send(response_package);
						System.out.println("@IPV4. Return the IP Address------Successfully!");
					}
					
					else if (receive_buff[data_pos] == 0x00 && receive_buff[data_pos + 1] == 0x1c)//28
					{//IPV6
						System.out.println("@IPV6.");
					    System.out.println("@IPV6. Send the Package to the Client!");
						byte[] response = new byte[16];
						// modify the message head
						receive_buff[2] = (byte) (receive_buff[2] | 0x81);
						receive_buff[3] = (byte) (receive_buff[3] | 0x80);
						receive_buff[8] = (byte) (receive_buff[8] | 0x00);
						receive_buff[9] = (byte) (receive_buff[9] | 0x01);
						// add response into message
						System.arraycopy(response, 0, receive_buff, data_pos + 4, response.length);
						//send UDP
						DatagramPacket send_package = new DatagramPacket(receive_buff, receive_buff.length, client_addr, client_port);
						DNSsocket.send(send_package);
						System.out.println("@IPV6. Send the Package to the Client------Successfully!");
					}
					else
					{
						System.out.println("@ipv4/6 judge WRONG!!!");
					}
				}
				
				else//Judge NOT in the local database
				{
					    System.out.println("@Not in the Local Database.");
					    System.out.println("@Not in the Local Database. Send the package to the remote DNS server!");
						//send UDP to far server
						DatagramPacket send_package = new DatagramPacket(receive_buff, receive_buff.length, server_addr, 53);
						DNSsocket.send(send_package);
						// add the address into HASH
						Hush_structure idRecord = new Hush_structure(byte2Short(receive_buff, 0), receive_package.getPort(),
								receive_package.getAddress());
						PackageMap.put((short) byte2Short(receive_buff, 0), idRecord);
						System.out.println("@Not in the Local Database. Send the package to the remote DNS serve------Successfully!");
						
				}
				
				
			}
			else//Judge response
			{
				// send the received message to client
				System.out.println("@Response.");
				System.out.println("@Response. Send the Received Message to Client!");
				Hush_structure idTransition = PackageMap.get(byte2Short(receive_buff, 0));
				DatagramPacket response_package = new DatagramPacket(receive_buff, receive_buff.length,
						idTransition.getAddr(), idTransition.getPort());
				DNSsocket.send(response_package);
				System.out.println("@Response. Send the Received Message to Client------Successfully!");
			}
		
		}
	
	}
	
	
	
	public int byte2Int(byte[] array, int start)
	{
		final int length = 1;
		int result = 0;
		
		byte loop;
		for (int i = start; i < start + length; i++) {
			loop = array[i];
			int offSet = length - (i - start) -1;
			//map the signed bytes to unsigned integer
			result += (loop & 0xFF) << (8 * offSet);
		}
		
		return result;
	}
	
	public static String byte2String(byte [] b, int start, int length) {
		
		char [] c = new char[length];
		for(int i = 0; i < length; i++) {
			c[i] = (char) b[start + i];
		}
		return String.valueOf(c);
	}
	
	public static short byte2Short(byte[] array, int start)
	{
		final int length = 2;
		short result = 0;
		
		byte loop;
		for (int i = start; i < start + length; i++) {
			loop = array[i];
			int offSet = length - (i - start) -1; //(i - start);
			result += (loop & 0xFF) << (8 * offSet);
		}
		
		return result;
	}

}
