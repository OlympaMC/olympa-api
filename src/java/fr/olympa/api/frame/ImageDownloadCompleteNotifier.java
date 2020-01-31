package fr.olympa.api.frame;

import java.util.Iterator;
import java.util.List;

import org.bukkit.scheduler.BukkitRunnable;

public class ImageDownloadCompleteNotifier extends BukkitRunnable {

	private ImageMaps plugin;

	public ImageDownloadCompleteNotifier(ImageMaps plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		List<ImageDownloadTask> tasks = this.plugin.getDownloadTasks();

		Iterator<ImageDownloadTask> itr = tasks.iterator();
		while (itr.hasNext()) {
			ImageDownloadTask task = itr.next();

			if (task.isDone()) {
				itr.remove();
				task.getSender().sendMessage("Download " + task.getURL() + ": " + task.getResult());
			}
		}
	}
}
