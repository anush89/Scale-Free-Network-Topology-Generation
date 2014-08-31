
/*
 * JoinUDPListener waits for a connection from a remote node. It then provides the 
 * required data structures to the joining node so that it can implement Barabasi-Albert
 * model to elect m links.
 * 
 * Variables used:
 * socket myUDPSocket - listening socket for incoming datagram packets
 * boolean flag - used to stop listening for datagram packets
 * int MAX_LENGTH - maximum size of udp data accepted
 * int m - no of links to be created for joining a new node. This information is needed by
 * 		   the remote node (to decide on no of nodes it connects to)
 * int n - max size of the network. If a node asks to join after n is reached, JoinUDPListener 
 * 		   sends a message rejecting the node.
 * 
 * Methods:
 * void run() - the first m0 nodes will be waiting for new UDP connections and each of the m0
 * 				nodes will use this method to execute separate threads.
 * 
 */


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;

public class JoinUDPListener implements Runnable{

	private DatagramSocket myUDPSocket;
	private boolean flag;
	private final int MAX_LENGTH=1024;
	private static int m;
	private static int n;
	
	public JoinUDPListener(int port, int max, int noOfLinksToBeCreated)
	{
		try {
			myUDPSocket=new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		flag=true;
		n=max;
		m=noOfLinksToBeCreated;
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
			if(myUDPData.equalsIgnoreCase("join_req"))
			{
				//new node trying to connect to the network
				//transfer routing and degree info of other nodes and let it decide to make connections
				HashMap<String,Integer> tmpHostsDetails=ClientWorker.getHostConnDetails();
				HashMap<String,Integer> tmpDegreeInfo=ClientWorker.getDegreeInfo();
				
				if(tmpDegreeInfo.size()>=n)
				{
					byte[] myMsg;
					DatagramPacket sendPacket;
					
					myMsg="reject".getBytes();
					sendPacket=new DatagramPacket(myMsg, myMsg.length,myUDPSocket.getInetAddress(),myUDPSocket.getPort());
					try {
						myUDPSocket.send(sendPacket);
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
				
				byte[] myMsg;
				DatagramPacket sendPacket;
				
				myMsg=String.valueOf(tmpDegreeInfo.size()).getBytes();
				sendPacket=new DatagramPacket(myMsg, myMsg.length,myUDPSocket.getInetAddress(),myUDPSocket.getPort());
				try {
					myUDPSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				myMsg=String.valueOf(m).getBytes();
				sendPacket=new DatagramPacket(myMsg, myMsg.length,myUDPSocket.getInetAddress(),myUDPSocket.getPort());
				try {
					myUDPSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				for(String hosts:tmpDegreeInfo.keySet())
				{
					myMsg=(hosts+" "+tmpHostsDetails.get(hosts)+" "+tmpDegreeInfo.get(hosts)).getBytes();
					sendPacket=new DatagramPacket(myMsg, myMsg.length,myUDPSocket.getInetAddress(),myUDPSocket.getPort());
					try {
						myUDPSocket.send(sendPacket);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}//end if
			else
			{
				flag=false;
				System.out.println("udp listener quitting.");
				break;
			}
		}
		
	}

}
