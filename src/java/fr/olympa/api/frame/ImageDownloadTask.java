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
import org.bukkit.plugin.java.JavaPlugin;

public class ImageDownloadTask implements Runnable {
	private JavaPlugin plugin;
	private String filename;
	private String downloadUrl;
	private CommandSender sender;
	private CompletableFuture future;

	ImageDownloadTask(JavaPlugin plugin, String url, String filename, CommandSender sender) {
		this.plugin = plugin;
		this.sender = sender;
		this.downloadUrl = url;
		this.filename = filename;

		this.future = CompletableFuture.runAsync(this);
	}

	public void close(Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (IOException ex) {
			}
		}
	}

	public String getResult() {
		try {
			return this.future.isDone() ? (String) this.future.get() : null;
		} catch (Exception ex) {
			return "Exception when getting result";
		}
	}

	public CommandSender getSender() {
		return this.sender;
	}

	public String getURL() {
		return this.downloadUrl;
	}

	public boolean isDone() {
		return this.future.isDone();
	}

	@Override
	public void run() {
		ReadableByteChannel in = null;
		FileOutputStream fos = null;
		FileChannel out = null;
		InputStream is = null;
		try {
			URL url = new URL(this.downloadUrl);
			URLConnection connection = url.openConnection();
			if (!(connection instanceof HttpURLConnection)) {
				this.future.complete("Not a http(s) URL");
				return;
			}

			int responseCode = ((HttpURLConnection) connection).getResponseCode();
			if (responseCode != 200) {
				this.future.complete("HTTP Status " + responseCode);
				return;
			}

			String mimeType = ((HttpURLConnection) connection).getHeaderField("Content-type");
			if (!mimeType.startsWith("image/")) {
				this.future.complete("That is a " + mimeType + ", not an image");
				return;
			}

			in = Channels.newChannel(is = connection.getInputStream());
			fos = new FileOutputStream(new File(this.plugin.getDataFolder() + "/images", this.filename));
			out = fos.getChannel();
			out.transferFrom(in, 0, Long.MAX_VALUE);
			this.future.complete("Download to " + this.filename + " finished");
		} catch (MalformedURLException ex) {
			this.future.complete("URL invalid");
		} catch (IOException ex) {
			this.future.complete("IO Exception");
		} finally {
			this.close(out);
			this.close(in);
			this.close(is);
			this.close(fos);
		}
	}
}
