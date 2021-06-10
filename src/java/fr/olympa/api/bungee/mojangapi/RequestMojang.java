package fr.olympa.api.bungee.mojangapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

public abstract class RequestMojang {

	public static String send(InetSocketAddress inetSocketAdress, String link) throws IOException {
		Proxy proxy = new Proxy(Proxy.Type.HTTP, inetSocketAdress);
		URL url = new URL(link);
		URLConnection request = url.openConnection(proxy);
		request.setUseCaches(false);
		BufferedReader in = new BufferedReader(new InputStreamReader(request.getURL().openStream(), Charset.forName("UTF-8")));
		StringBuilder sb = new StringBuilder();
		String lineNotFount;
		while ((lineNotFount = in.readLine()) != null) {
			sb.append(lineNotFount + "\n");
		}
		in.close();
		if (!sb.toString().isEmpty()) {
			return sb.toString();
		}
		return null;
	}
}
