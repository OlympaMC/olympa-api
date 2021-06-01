package fr.olympa.api.common.machine;

import java.io.File;
import java.util.stream.Collectors;

import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.plugin.PluginDescription;

public class TpsMessageBungee extends TpsMessage {

	public TpsMessageBungee(boolean isConsole) {
		super(isConsole);
	}

	@Override
	public TxtComponentBuilder getInfoMessage() {
		if (main.isSpigot())
			throw new UnsupportedOperationException("Unable to get Bungee Info on not Bungee Environment");
		TxtComponentBuilder textBuilder = super.getInfoMessage();
		try {
			textBuilder.extra(new TxtComponentBuilder("\n&3Plugins Olympa: &b"));
			for (TxtComponentBuilder txt : ((OlympaBungee) main).getProxy().getPluginManager().getPlugins().stream().filter(f -> f.getDescription().getName().startsWith("Olympa"))
					.map(ff -> {
						PluginDescription desc = ff.getDescription();
						String fileInfo = Utils.tsToShortDur(new File(ff.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).lastModified() / 1000L);
						return new TxtComponentBuilder("&6%s ", desc.getName().substring(6)).onHoverText("&eDernière MAJ %s (%s)", fileInfo, desc.getVersion()).console(isConsole);
					})
					.collect(Collectors.toList()))
				textBuilder.extra(txt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return textBuilder;
	}
}
