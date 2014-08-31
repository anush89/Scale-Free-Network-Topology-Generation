/*
 * UDPManager replies with the required details to the querying application.
 * 
 * This class initially listens on a specific port for incoming querying connections.
 * 
 * Once a remote node asks for a specific query it sends the required details through the
 * UDP connections established.
 * 
 * Variables Used:
 * socket myUDPSocket - UDP listener for incoming query connections and for communicating
 * int MAX_LENGTH - Max length of a single datagram packet incoming and outgoing.
 * boolean flag - termination flag for the listener. when the flag becomes false the listener terminates
 * int UDPport - a fixed port no for listening and answering queries. 
 * 
 * Methods:
 * doQueryWork(String cmd) - based on the parameter cmd give different answers to the query.
 * 
 */


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.StringTokenizer;

public class UDPManager implements Runnable{
	private static DatagramSocket myUDPSocket;
	private final int MAX_LENGTH=1024;
	boolean flag=true;
	private static int UDPPort=9090;

	public UDPManager(int myPort)
	{
		try {
			myUDPSocket=new DatagramSocket(myPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
		while(flag)
		{
			byte[] buf=new byte[MAX_LENGTH];
			DatagramPacket myUDPPacket=new DatagramPacket(buf, buf.length);
			try {
				myUDPSocket.receive(myUDPPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String myUDPData=new String(myUDPPacket.getData());
			
			if(myUDPData.startsWith("query"))
			{
				StringTokenizer myStr=new StringTokenizer(myUDPData," ");
				myStr.nextToken();
				String cmd=myStr.nextToken();
				doQueryWork(cmd);
			}
		}
		
		try {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			myUDPSocket.close();
		}
		
	}
	
	
	// This method provides answers to the query from remote nodes.
	private static void doQueryWork(String cmd)
	{
		if(cmd.equalsIgnoreCase("1"))
		{
			HashMap<String,String> tmpRoutingTable=ClientWorker.getRoutingTable();
			byte[] myMsg;
			DatagramPacket sendPacket;
			
			myMsg=String.valueOf(tmpRoutingTable.size()).getBytes();
			sendPacket=new DatagramPacket(myMsg, myMsg.length,myUDPSocket.getInetAddress(),UDPPort);
			try {
				myUDPSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			for(String hosts:tmpRoutingTable.keySet())
			{
				myMsg=(hosts+" "+tmpRoutingTable.get(hosts)).getBytes();
				sendPacket=new DatagramPacket(myMsg, myMsg.length,myUDPSocket.getInetAddress(),UDPPort);
				try {
					myUDPSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else if(cmd.equalsIgnoreCase("2"))
		{
			HashMap<String,Integer> tmpDegreeInfo=ClientWorker.getDegreeInfo();
			byte[] myMsg;
			DatagramPacket sendPacket;
			
			myMsg=String.valueOf(tmpDegreeInfo.size()).getBytes();
			sendPacket=new DatagramPacket(myMsg, myMsg.length,myUDPSocket.getInetAddress(),UDPPort);
			try {
				myUDPSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			for(String hosts:tmpDegreeInfo.keySet())
			{
				myMsg=(hosts+" "+tmpDegreeInfo.get(hosts)).getBytes();
				sendPacket=new DatagramPacket(myMsg, myMsg.length,myUDPSocket.getInetAddress(),UDPPort);
				try {
					myUDPSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else if(cmd.equalsIgnoreCase("3"))
		{
			HashMap<String,Integer> myRoutingDist=ClientWorker.getRoutingDistTable();
			byte[] myMsg;
			DatagramPacket sendPacket;
			int max=0;
			String maxHost="";
			
			for(String hosts:myRoutingDist.keySet())
			{
				if(myRoutingDist.get(hosts)>max)
				{
					maxHost=hosts;
					max=myRoutingDist.get(hosts);
				}
			}
			myMsg=(maxHost+" "+max).getBytes();
			sendPacket=new DatagramPacket(myMsg, myMsg.length, myUDPSocket.getInetAddress(),UDPPort);
			try {
				myUDPSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
