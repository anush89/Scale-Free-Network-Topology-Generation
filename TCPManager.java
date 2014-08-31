
/*
 * TCPManager manages all nodes listening for TCP connections (when a new node joins in)
 * 
 * It creates a serverSocket object with a designated port number and listens for connections
 * on it. Once it receives a connection it creates a new ClientWorker object to perform routing
 * and all other works.
 * 
 * Variables used:
 * ServerSocket serverSocket - This ServerSocket object is used to listen for incoming tcp connections
 * boolean flag - used to stop listening for more incoming connections.
 * 
 * 
 */


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPManager implements Runnable{
	
	private ServerSocket serverSocket;
	boolean flag;
	
	public TCPManager(int port_no) throws IOException
	{
		serverSocket=new ServerSocket(port_no);
		flag=true;	
	}

	@Override
	public void run(){
		
		System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
		while(flag)
		{
			try
			{
				Socket server=serverSocket.accept();
				@SuppressWarnings("unused")
				ClientWorker myWorker=new ClientWorker(server);
			}
			catch(IOException e)
			{
				System.out.println("Error accepting socket connection\n");
				System.exit(1);
			}
		}
		
		
		try
		{
			serverSocket.close();
		}
		catch(IOException e)
		{
			System.out.println("Error Closing serverWorker\n");
			System.exit(1);
		}
	}
}
