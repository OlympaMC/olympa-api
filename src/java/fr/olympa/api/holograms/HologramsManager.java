package fr.olympa.api.holograms;

import java.util.ArrayList;
import java.util.List;

public class HologramsManager {

	private List<Hologram> holograms = new ArrayList<>();

	public void unload() {
		holograms.forEach(Hologram::destroy);
		holograms.clear();
	}

}
