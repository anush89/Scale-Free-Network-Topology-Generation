
/*
 * QueryApp is used query about the status of a node in the network.
 * 
 * This program needs to be run separately. It has its own main method.
 * 
 * The various options are
		1 Display the given hosts routing table
		2 Display the node degree information for the given host
		3 Display the farthest node from the given host
		4 Search different host
		5 Quit
 * 
 * Variable used:
 * int UDPPortNo - a fixed udp port no for listening and answering queries.
 * 
 */


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class QueryApp {

	private static final int UDPPortNo=9090;
	
	public static void main(String[] args) {
		
		DatagramSocket mySocket=null;
		try {
			mySocket=new DatagramSocket(UDPPortNo);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		String host=""; 
		if(args.length>0)
			host=args[1];
		else
		{
			System.out.println("Please enter a hostname to query");
			Scanner in=new Scanner(System.in);
			host=in.nextLine();
			in.close();
		}
			
		
		InetAddress IPAddress=null;
		try {
			IPAddress = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		System.out.println("Choose a menu option");
		System.out.println("1 Display the given hosts routing table");
		System.out.println("2 Display the node degree information for the given host");
		System.out.println("3 Display the farthest node from the given host");
		System.out.println("4 Search different host");
		System.out.println("5 Quit");
		
		boolean flag=true;
		
		while(flag)
		{
			Scanner in=new Scanner(System.in);
			String choice=in.nextLine();
			in.close();
			if(choice.equalsIgnoreCase("1"))
			{
				byte[] sendData="query 1".getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, UDPPortNo);
				
				try {
					mySocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				byte[] recvArray=new byte[1024];
				
				DatagramPacket recvPacket=new DatagramPacket(recvArray, recvArray.length);
				String myRevdData="";
				int size;
				try {
					mySocket.receive(recvPacket);
					myRevdData=new String(recvPacket.getData());
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				size=Integer.parseInt(myRevdData);
				System.out.println("\nHost Name"+"\t"+"Next Hop");
				for(int i=0;i<size;i++)
				{
					try {
						mySocket.receive(recvPacket);
					} catch (IOException e) {
						e.printStackTrace();
					}
					myRevdData=new String(recvPacket.getData());
					StringTokenizer myTokens=new StringTokenizer(myRevdData," ");
					String hostRecvd=myTokens.nextToken();
					String port=myTokens.nextToken();
					System.out.println(hostRecvd+"\t"+port);
				}
			}
			else if(choice.equalsIgnoreCase("2"))
			{
				byte[] sendData="query 2".getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, UDPPortNo);
				
				try {
					mySocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				byte[] recvArray=new byte[1024];
				
				DatagramPacket recvPacket=new DatagramPacket(recvArray, recvArray.length);
				String myRevdData="";
				int size;
				try {
					mySocket.receive(recvPacket);
					myRevdData=new String(recvPacket.getData());
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				size=Integer.parseInt(myRevdData);
				System.out.println("\nHost Name"+"\t"+"Degree Info");
				for(int i=0;i<size;i++)
				{
					try {
						mySocket.receive(recvPacket);
					} catch (IOException e) {
						e.printStackTrace();
					}
					myRevdData=new String(recvPacket.getData());
					StringTokenizer myTokens=new StringTokenizer(myRevdData," ");
					String hostRecvd=myTokens.nextToken();
					String degInfo=myTokens.nextToken();
					System.out.println(hostRecvd+"\t"+degInfo);
				}
			}
			else if(choice.equalsIgnoreCase("3"))
			{
				byte[] sendData="query 3".getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, UDPPortNo);
				
				try {
					mySocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				byte[] recvArray=new byte[1024];
				
				DatagramPacket recvPacket=new DatagramPacket(recvArray, recvArray.length);
				String myRevdData="";
				try {
					mySocket.receive(recvPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
				myRevdData=new String(recvPacket.getData());
				StringTokenizer myTokens=new StringTokenizer(myRevdData," ");
				String hostRecvd=myTokens.nextToken();
				String dist=myTokens.nextToken();
				System.out.println("\nFarthest host name"+"\t"+"Dist value");
				System.out.println(hostRecvd+"\t"+dist);
			}
			else if(choice.equalsIgnoreCase("4"))
			{
				Scanner input=new Scanner(System.in);
				host=input.nextLine();
				try {
					IPAddress = InetAddress.getByName(host);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				System.out.println("Now querying host "+host);
				input.close();
				
			}
			else if(choice.equalsIgnoreCase("5"))
			{
				flag=false;
			}
			else
			{
				System.out.println("Invalid Choice... Please enter again");
			}
		}
	}

}
