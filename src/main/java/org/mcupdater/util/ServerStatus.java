package org.mcupdater.util;

import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.logging.Level;

public class ServerStatus {
	
	private final String motd;
	private final int players;
	private final int maxPlayers;

	public ServerStatus(String motd, int players, int maxPlayers) {
		this.motd = motd;
		this.players = players;
		this.maxPlayers = maxPlayers;
	}
	
	public String getMOTD() {
		return motd;
	}
	
	public int getPlayers() {
		return players;
	}
	
	public int getMaxPlayers() {
		return maxPlayers;
	}
	
	public static ServerStatus getStatus(String address) throws Exception {
		
		ServerStatus result = null;
		
		Socket socket = null;
		DataInputStream dis = null;
		DataOutputStream dos = null;
		URI server;
		String host = null;
		int port = -1;
		
		try {
			server = new URI("my://" + address);
			host = server.getHost();
			if (server.getPort() != -1) {
				port = server.getPort();
			} else {
				try
				{
					Class.forName("com.sun.jndi.dns.DnsContextFactory");
					Hashtable<String, String> hashtable = new Hashtable<>();
					hashtable.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
					hashtable.put("java.naming.provider.url", "dns:");
					hashtable.put("com.sun.jndi.dns.timeout.retries", "1");
					DirContext dircontext = new InitialDirContext(hashtable);
					Attributes attributes = dircontext.getAttributes("_minecraft._tcp." + host, new String[] {"SRV"});
					String[] astring = attributes.get("srv").get().toString().split(" ", 4);
					host = astring[3];
					port = Integer.parseInt(astring[2]);
				}
				catch (Exception e)
				{
					port = 25565;
				}
			}
		} catch (URISyntaxException e1) {
			MCUpdater.apiLogger.log(Level.SEVERE, "URI Syntax error", e1);
		}
		
		try {
			socket = new Socket();
			
			socket.setSoTimeout(3000);
			socket.setTcpNoDelay(true);
			socket.setTrafficClass(18);
			
			socket.connect(new InetSocketAddress(host, port), 3000);
			
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			
			dos.write(254);
			dos.write(1);
			
			if (dis.read() != 255) throw new IOException("Bad message");
			
			short strLength = dis.readShort();
			if (strLength < 0 || strLength > 64) throw new IOException("invalid string length");

			StringBuilder sb = new StringBuilder();
			for(int i=0; i<strLength; ++i) sb.append(dis.readChar());

			char[] test = sb.toString().toCharArray();
			String foo = " !#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_'abcdefghijklmnopqrstuvwxyz{|}~⌂ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»";
			for (int i = 0; i<test.length; ++i) {
				if (test[i] != 167 && foo.indexOf(test[i]) < 0){
					test[i] = 63;
				}
			}
			String data = sb.toString();
			int players = -1;
			int maxPlayers = -1;
			String motd = ""; 
			String[] dataParts; 
			if (data.startsWith("\u00a7") && data.length() > 1) {
				dataParts = data.substring(1).split("\u0000");
				
				if (parseIntWithDefault(dataParts[0],0) == 1) {
					motd = dataParts[3];
					players = Integer.parseInt(dataParts[4]);
					maxPlayers = Integer.parseInt(dataParts[5]);
				}
			} else {
				dataParts = data.split("\u00a7");

				motd = dataParts[0];

				try {
					players = Integer.parseInt(dataParts[1]);
					maxPlayers = Integer.parseInt(dataParts[2]);
				} catch(Exception e) {}
			}
			result = new ServerStatus(motd, players, maxPlayers);
			
		} catch (SocketException | SocketTimeoutException | UnknownHostException e1) {
		} finally {
			if (dis != null) dis.close();
			if (dos != null) dos.close();
			if (socket != null) socket.close();
		}
		
		return result;
	}
	
	private static int parseIntWithDefault(String par0Str, int par1)
	{
		int j = par1;

		try
		{
			j = Integer.parseInt(par0Str);
		}
		catch (Throwable throwable)
		{
		}

		return j;
	}

}
