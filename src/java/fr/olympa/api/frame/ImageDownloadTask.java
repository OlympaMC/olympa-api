package fr.olympa.api.frame;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.CompletableFuture;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ImageDownloadTask implements Runnable {
	private Plugin plugin;
	private String filename;
	private String downloadUrl;
	private CommandSender sender;
	private CompletableFuture future;
	
	ImageDownloadTask(Plugin plugin, String url, String filename, CommandSender sender) {
		this.plugin = plugin;
		this.sender = sender;
		downloadUrl = url;
		this.filename = filename;
		
		future = CompletableFuture.runAsync(this);
	}
	
	public void close(Closeable c) {
		if (c != null)
			try {
				c.close();
			} catch (IOException ex) {
			}
	}
	
	public String getResult() {
		try {
			return future.isDone() ? (String) future.get() : null;
		} catch (Exception ex) {
			return "Exception when getting result";
		}
	}
	
	public CommandSender getSender() {
		return sender;
	}
	
	public String getURL() {
		return downloadUrl;
	}
	
	public boolean isDone() {
		return future.isDone();
	}
	
	@Override
	public void run() {
		ReadableByteChannel in = null;
		FileOutputStream fos = null;
		FileChannel out = null;
		InputStream is = null;
		try {
			URL url = new URL(downloadUrl);
			URLConnection connection = url.openConnection();
			if (!(connection instanceof HttpURLConnection)) {
				future.complete("Not a http(s) URL");
				return;
			}
			
			int responseCode = ((HttpURLConnection) connection).getResponseCode();
			if (responseCode != 200) {
				future.complete("HTTP Status " + responseCode);
				return;
			}
			
			String mimeType = ((HttpURLConnection) connection).getHeaderField("Content-type");
			if (!mimeType.startsWith("image/")) {
				future.complete("That is a " + mimeType + ", not an image");
				return;
			}
			
			in = Channels.newChannel(is = connection.getInputStream());
			fos = new FileOutputStream(new File(plugin.getDataFolder() + "/images", filename));
			out = fos.getChannel();
			out.transferFrom(in, 0, Long.MAX_VALUE);
			future.complete("Download to " + filename + " finished");
		} catch (MalformedURLException ex) {
			future.complete("URL invalid");
		} catch (IOException ex) {
			future.complete("IO Exception");
		} finally {
			close(out);
			close(in);
			close(is);
			close(fos);
		}
	}
}
