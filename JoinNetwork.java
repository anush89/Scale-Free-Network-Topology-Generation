
/*
 * JoinNetwork is a stand alone program which is needed for joining a computer to the scale
 * free network.
 * 
 * The Barabasi-Albert model for choosing m links( to connect to nodes in the network) 
 * is implemented in this class.
 * 
 * First this program reads in a file which contains information about the m0 nodes.
 * (Even though there is also TCP information in this file our program does not read it but 
 * it gets it from the JoinUDPListener.)
 * 
 * Based on the Degree information received from one of the randomly chosen initial m0 node 
 * it performs Barabasi-Albert algorithm.
 * 
 * Variables used:
 * 	String[] hosts - list of hosts in intial m0 network. read in from the input file
	int m0 - initial network no of nodes 
	int m - no of links(or nodes) to connect to
	int joinPort - fixed value of port number to perform udp join connections
	int queryPort - fixed value of port number used to perform query operations
	int joinTCPPort - tcp port number info obtained from JoinUDPListener through udp connection.
 * 
 * Methods used:
 * void readFile() - reads in the file and extracts the necessary data from it(hostnames of m0 nodes etc.)
 * Thread listenForTCP() - creates a separate thread for listening TCP connections from new joining nodes  
 * Thread listenForUDP() - creates a separate thread for listening for query connections
 * Thread[] joinIntoNetwork() - Connect to the selected m nodes via tcp links using separate threads
 * 
 */



import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

public class JoinNetwork {
	
	private static String[] hosts;
	private static int m0;
	private static int m;
	private static final int joinPort=10000;
	private static final int queryPort=9090;
	private static int joinTCPPort;
	
	public JoinNetwork() {

	}

	private static void readFile()
	{
		//do some text reading
		BufferedReader fileReader=null;
		try {
			fileReader = new BufferedReader(new FileReader("file.txt"));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		String line="";
		try {
			line=fileReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int m0=Integer.parseInt(line);
		try {//n
			line=fileReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {//m
			line=fileReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		hosts=new String[m0];
		//UDPPorts=new Integer[m0];
		
		for(int i=0;i<m0;i++)
		{
			try {
				hosts[i]=fileReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String[] tokens = hosts[i].split(" ");
			hosts[i] = tokens[0];
		}
		
		try {
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static Thread listenForTCP(Integer args)
	{
		Thread TCPThread=null;
		try {
			TCPThread=new Thread(new TCPManager(args));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		TCPThread.start();
		return TCPThread;
	}
		
	private static Thread listenForUDP()
	{
		Thread UDPThread=null;

		UDPThread=new Thread(new UDPManager(queryPort));
		UDPThread.start();
		return UDPThread;
	}
	
	public static void main(String[] args) {

		readFile();
				
	
		
		Random random=new Random();
		int myRandomNo=random.nextInt(m0);
		
		//connecting to one of the m0 udp nodes chosen randomly
		DatagramSocket mySocket=null;
		try {
			mySocket=new DatagramSocket(joinPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		byte[] sendData="join_req".getBytes();
		InetAddress IPAddress=null;
		try {
			IPAddress = InetAddress.getByName(hosts[myRandomNo]);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, joinPort);
		
		try {
			mySocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//recv the routing table info and degree info through udp
		final int MAX=1024;
		
		byte[] recvArray=new byte[MAX];
		
		DatagramPacket recvPacket=new DatagramPacket(recvArray, recvArray.length);
		HashMap<String, Integer> recvdConnDetails=new HashMap<>();
		HashMap<String, Integer> degreeInfo=new HashMap<>();
		String myRevdData="";
		int size;
		
		try {
			mySocket.receive(recvPacket);
			myRevdData=new String(recvPacket.getData());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(!myRevdData.equalsIgnoreCase("reject"))
		{
			size=Integer.parseInt(myRevdData);
			
			Thread myUDPThread=null;
			myUDPThread=listenForUDP();	
			
			try {
				mySocket.receive(recvPacket);
				myRevdData=new String(recvPacket.getData());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			m=Integer.parseInt(myRevdData);
			
			int sumOfDegrees=0;
			for(int i=0;i<size;i++)
			{
				try {
					mySocket.receive(recvPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
				myRevdData=new String(recvPacket.getData());
				StringTokenizer myTokens=new StringTokenizer(myRevdData," ");
				String host=myTokens.nextToken();
				String port=myTokens.nextToken();
				String degree=myTokens.nextToken();
				recvdConnDetails.put(host, Integer.parseInt(port));
				degreeInfo.put(host, Integer.parseInt(degree));
				sumOfDegrees=sumOfDegrees+Integer.parseInt(degree);
			}
			
			//do preferential attachment
			int i=0;
			double zeroToOne;
			double sumTillNow;
			String selected[]=new String[m];
			HashMap<String,Double> probabilities=new HashMap<>();
			boolean breakFlag;
			while(i<m)
			{	
				for(String hosts:degreeInfo.keySet())
				{
					double myProb=(double)degreeInfo.get(hosts)/sumOfDegrees;
					probabilities.put(hosts, myProb);
				}
				
				zeroToOne=Math.random();
				sumTillNow=0;
				breakFlag=false;
				for(String hosts:degreeInfo.keySet())
				{
					sumTillNow=probabilities.get(hosts)+sumTillNow;
					
					if(zeroToOne<=(sumTillNow/sumOfDegrees) && breakFlag!=true)
					{
						selected[i]=hosts;
						breakFlag=true;
					}
				}
				sumOfDegrees=sumOfDegrees-degreeInfo.get(selected[i]);
				degreeInfo.remove(selected[i]);
				probabilities.clear();
				
				i++;
				
			}//end while loop
			
			//add tcp socket connections to selected nodes
			Thread[] myTalkingThreads=joinIntoNetwork(selected,recvdConnDetails);
			
			Thread myTCPThread=null;
			myTCPThread=listenForTCP(joinTCPPort);
			
			try {
				myUDPThread.join();
				myTCPThread.join();
				for(Thread t:myTalkingThreads)
					t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}//end if(not reject)
		else
		{
			System.out.println("Network has reached max number of nodes allowed. Connection rejected.");
		}
	}//end main
	
	private static Thread[] joinIntoNetwork(String[] selected,HashMap<String, Integer> connDetails)
	{
		String host;
		int portNo=0;
		Thread[] myThread=new Thread[selected.length];
		Socket myConnections=null;
		for(int i=0;i<selected.length;i++)
		{
			host=selected[i];
			portNo=connDetails.get(selected[i]);
			try {
				myConnections=new Socket(host, portNo);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			myThread[i]=new Thread(new ClientWorker(myConnections));
			myThread[i].start();
		}
		if(portNo!=0)
			joinTCPPort=portNo;
		return myThread;
	}

}
