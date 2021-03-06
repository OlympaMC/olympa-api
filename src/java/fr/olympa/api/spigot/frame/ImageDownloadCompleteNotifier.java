package fr.olympa.api.spigot.frame;

import java.util.Iterator;
import java.util.List;

import org.bukkit.scheduler.BukkitRunnable;

public class ImageDownloadCompleteNotifier extends BukkitRunnable {
	
	private ImageFrameManager manager;
	
	public ImageDownloadCompleteNotifier(ImageFrameManager manager) {
		this.manager = manager;
	}
	
	@Override
	public void run() {
		List<ImageDownloadTask> tasks = manager.getDownloadTasks();
		
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
