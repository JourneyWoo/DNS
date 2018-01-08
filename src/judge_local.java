import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;

public class judge_local {
	
	public byte[] judge(String name_domain)
	{
		String file_name = "/Users/wuzhenglin/Desktop/to students(networks)/dnsrelay.txt";
		File file = new File(file_name);
		String read_local = "";
		String[] name_address = new String[2];
		int flag = 0;
		
		try{
			FileReader filereader = new FileReader(file);
			BufferedReader bufferedreader = new BufferedReader(filereader);
			read_local = bufferedreader.readLine();
			
			while(read_local != null)
			{
				name_address = read_local.split(" ");
				if(name_address[1].equals(name_domain))
				{
					byte[] addr = InetAddress.getByName(name_address[0]).getAddress();
					flag = 1;
					return addr;
					//break;
				}
				read_local = bufferedreader.readLine();
				
			}
		
			
			bufferedreader.close();
			filereader.close();
					
		}catch(Exception e){
			e.printStackTrace();
		}

			return null;
		
	}

}
