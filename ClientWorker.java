/*
 * ClientWorker class is responsible for executing, updating and maintaining routing tables,
 * and all other distance vector protocol data structures.
 * 
 * We also do our distance vector update work in this class when a new node joins. 
 * 
 * Each m0 nodes listening for TCP connections and every node joining the network will run
 * many instance of this ClientWorker. The exact number of instance indicates the degree
 * of the node. For example if a node has 2 outgoing connections, there will be 2 instances
 * of ClientWorker running. 
 * 
 * The data structures are made static so that one node will have exactly one routing table.
 * All the instance can make changes to this table.
 * 
 * Classes calling ClientWorker: JoinNetwork and TCPManager and ProgMain
 * 
 * JoinNetwork creates an instance of this class when a new node is done selecting m no of connections
 * 
 * TCPManager creates an instance of this class when a node connects to a node listening for a TCP connection
 * 
 * ProgMain creates m0-1 instances of this class when the base m0 nodes are being initialized 
 * 
 */

import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.net.InetAddress;
public class ClientWorker implements Runnable {
	Socket client;
	public final int joinPort=9090;
	private static HashMap<String, Integer> myNeigh=new HashMap<>();
	//my routing table hashmap<hostname,next hop>
	private static HashMap<String,String> myRoutingTable=new HashMap<>();
	//my routing table distance hashmap<hostname,distance to the host>
	private static HashMap<String,Integer> myRoutingDist=new HashMap<>();
	//data structure to hold degree information calculated hashmap<hostname,degree of that host>
	private static HashMap<String,Integer> myDegreeInfo=new HashMap<>();
	//Hostname and port numbers of all connected hosts
	private static HashMap<String, Integer> HostConnDetails=new HashMap<>();
	boolean flag=true;
	private static int noOfConnections;
	private static PrintWriter out = null;

	private Socket clientSocket;
	private ServerSocket serverSocket;
	ClientWorker(Socket myClient)
	{
		this.client=myClient;
		noOfConnections++;
		myNeigh.put(myClient.getInetAddress().toString(), myClient.getPort());
	}

	@Override
	public void run() {
		
		while(flag)
		{
			try {
				doDistVectWork();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	private static Set<String> keyset = new HashSet<String>();
	private static Set<String> distanceset = new HashSet<String>();
	
	private void doDistVectWork() throws IOException
	{
		InetAddress ip;
		ip = InetAddress.getLocalHost();
		String hostname=InetAddress.getLocalHost().getHostName();
		keyset=myNeigh.keySet();	
		for(String s : keyset)
			{
			myRoutingTable.put(s,hostname);	
			myRoutingDist.put(s,1);
			}
		distanceset=myRoutingDist.keySet();
		String result=ip + ":";
			for(String s : distanceset)
				{
					
					int value;
					value=myRoutingDist.get(s);
					result=result + s + "-" + value + ":";
				}
		for(String s : keyset)
		{
			try {
				clientSocket =   new Socket(s, joinPort);
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				out.write(result);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	
		try {
			serverSocket=new ServerSocket(joinPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	clientSocket=serverSocket.accept();
	InputStream is = clientSocket.getInputStream();
	Scanner input = new Scanner(is);
	String recvdData;
	recvdData=input.nextLine();
	  System.out.println(recvdData);
	  
		
			String[] routeinfo;
			String key,ipaddr;
			String actualvalue;
			int incomingvalue,actualdistance;
			String[] routeinfo1,routeinfo2;
			routeinfo=recvdData.split(":");
			ipaddr=routeinfo[0];
			for(int i=1;i<routeinfo.length;i++)
			{
				routeinfo2=routeinfo[i].split("-");
				incomingvalue=Integer.parseInt(routeinfo2[1]);
				actualdistance=myRoutingDist.get(routeinfo2[0]);
				
				
				if((incomingvalue+1)<actualdistance)
				{
					myRoutingDist.put(routeinfo2[0],incomingvalue+1);
					myRoutingTable.put(routeinfo2[0], clientSocket.getInetAddress().getHostName());
				}
			}
		
			for(String s : keyset)
			{
				System.out.println(s);
			System.out.println(myRoutingTable.get(s));	
			System.out.println(myRoutingDist.get(s));
			}
	}
	public static HashMap<String,Integer> getDegreeInfo()
	{
		return myDegreeInfo;
	}
	
	public static HashMap<String, Integer> getHostConnDetails()
	{
		return HostConnDetails;
	}
	
	public static HashMap<String, String> getRoutingTable()
	{
		return myRoutingTable;
	}
	
	public static HashMap<String, Integer> getRoutingDistTable()
	{
		return myRoutingDist;
	}
}
