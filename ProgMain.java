/*
 * Main program execution starts from here.
 * 
 * ProgMain is responsible for creating the initial m0 nodes.
 * 
 * It also makes the necessary connections and creates necessary listeners(UDP, TCP) for 
 * incoming connections.
 * 
 * The input file which contains all the m0 participating nodes is given initially to ProgMain
 * 
 * Then based on the input given through the file 2 udp and one tcp listeners are established.
 * 
 * Then the first of the m0 nodes creates m0-1 tcp links to all the other nodes in the initial
 * network.
 * 
 * 
 */



import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ProgMain{
	
	public static void main(String[] args) throws IOException{
		int m0;
		int n;
		int m;
		//do some text reading
		FileReader fp=new FileReader("file.txt");
		@SuppressWarnings("resource")
		BufferedReader fileReader = new BufferedReader(fp);
		String line;
		line=fileReader.readLine();
		m0=Integer.parseInt(line);
		line=fileReader.readLine();
		n=Integer.parseInt(line);
		line=fileReader.readLine();
		m=Integer.parseInt(line);
		String[] hosts=new String[m0];
		Integer[] UDPPorts=new Integer[m0];
		Integer[] TCPPorts=new Integer[m0];
		Integer[] UDPPorts2=new Integer[m0];
		
		for(int i=0;i<m0;i++)
		{
			hosts[i]=fileReader.readLine();
			String[] tokens = hosts[i].split(" ");
			if(tokens.length!=4){throw new IllegalArgumentException();}
			hosts[i] = tokens[0];
			UDPPorts[i] = Integer.parseInt(tokens[1]);
			UDPPorts2[i] = Integer.parseInt(tokens[2]);
			TCPPorts[i]= Integer.parseInt(tokens[3]);
		}
		
		fp.close();
		
		Thread UDPThread=null;
		
		for(int i=0;i<m0;i++)//create a udp listener.
		{
			if(hosts[i].equalsIgnoreCase(InetAddress.getLocalHost().getHostName()))
			{
				UDPThread=new Thread(new UDPManager(UDPPorts[i]));
				UDPThread.start();
				break;
			}
		}
		
		Thread UDPThread2=null;
		
		for(int i=0;i<m0;i++)//create a udp listener.
		{
			if(hosts[i].equalsIgnoreCase(InetAddress.getLocalHost().getHostName()))
			{
				UDPThread2=new Thread(new JoinUDPListener(UDPPorts2[i],n,m));
				UDPThread2.start();
				break;
			}
		}
		
		
		Thread TCPThread=null;
		
		for(int i=0;i<m0;i++)
		{
			if(hosts[i].equalsIgnoreCase(InetAddress.getLocalHost().getHostName()))
			{
				TCPThread=new Thread(new TCPManager(TCPPorts[i]));
				TCPThread.start();
				break;
			}
		}
		
		int noOfConnections=m0-1;
		Socket myConnections;
		//int noOfThreads=m0-1;
		Thread[] myThread=new Thread[noOfConnections];
		int j=0;
		
		if(hosts[noOfConnections].equalsIgnoreCase(InetAddress.getLocalHost().getHostName()))
		{
			//I am the m0 node. Need to make connections to all other nodes
			for(int i=1;i<m0;i++)
			{
				myConnections=new Socket(hosts[i], TCPPorts[i]);
				myThread[j]=new Thread(new ClientWorker(myConnections));
				myThread[j].start();
				j++;
			}
		}
		
		
		
		//need to join threads
		
		try {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally
		{
			fileReader.close();
			try
			{
			if(UDPThread!=null)
				UDPThread.join();
			if(TCPThread!=null)
				TCPThread.join();
			if(UDPThread2!=null)
				UDPThread2.join();
			for(int i=0;i<(m0-1);i++)
			{
				if(myThread[i]!=null)
					myThread[i].join();
			}	
			}catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

}
