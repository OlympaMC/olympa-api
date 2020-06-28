package fr.olympa.api.holograms;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.StringJoiner;

import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.lines.AnimLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;

public class HologramsCommand extends ComplexCommand {

	private HologramsManager holograms;

	public HologramsCommand(HologramsManager holograms) {
		super(OlympaCore.getInstance(), "holograms", "Permet de gérer les hologrammes", OlympaAPIPermissions.COMMAND_HOLOGRAMS_MANAGE, "holo");
		this.holograms = holograms;
	}

	@Cmd (player = true, min = 1, syntax = "<lignes séparées par des |>")
	public void create(CommandContext cmd) {
		String[] linesStrings = cmd.getFrom(0).split("\\|");
		int id = holograms.addPersistentHologram(new Hologram(player.getLocation().add(0, 1, 0), Arrays.stream(linesStrings).map(this::createLine).toArray(AbstractLine[]::new)));
		sendSuccess("Hologramme %d créé.", id);
	}

	@Cmd
	public void list(CommandContext cmd) {
		StringJoiner joiner = new StringJoiner("\n", "Liste des hologrammes persistants sur ce serveur :\n", "");
		for (Entry<Integer, Hologram> holo : holograms.getPersistentHolograms()) {
			joiner.add("§b§l" + holo.getKey() + "§3 - §b" + holo.getValue().toString() + "§3 - §b" + SpigotUtils.convertLocationToString(holo.getValue().getBottom()));
		}
		sendInfo(joiner.toString());
	}

	@Cmd (player = true, min = 1, args = "INTEGER", syntax = "<id de l'hologramme>")
	public void remove(CommandContext cmd) {
		int id = cmd.getArgument(0);
		if (holograms.deletePersistentHologram(id)) {
			sendSuccess("L'hologramme %d vient d'être supprimé.", id);
		}else {
			sendError("Il n'y a aucun hologramme à supprimer avec l'ID %d.", id);
		}
	}

	@Cmd (player = true, min = 1, args = "INTEGER", syntax = "<id de l'hologramme>")
	public void move(CommandContext cmd) {
		Hologram hologram = getHologram(cmd.getArgument(0));
		if (hologram == null) return;
		hologram.move(player.getLocation().add(0, 1, 0));
		sendSuccess("L'hologramme a été déplacé à votre position.");
	}

	@Cmd (player = true, min = 1, args = "INTEGER", syntax = "<id de l'hologramme>")
	public void teleport(CommandContext cmd) {
		Hologram hologram = getHologram(cmd.getArgument(0));
		if (hologram == null) return;
		player.teleport(hologram.getBottom());
		sendSuccess("Vous vous êtes téléporté à l'hologramme.");
	}

	@Cmd (player = true, min = 1, args = "INTEGER", syntax = "<id de l'hologramme> <ligne>")
	public void addLine(CommandContext cmd) {
		Hologram hologram = getHologram(cmd.getArgument(0));
		if (hologram == null) return;
		hologram.addLine(cmd.getArgumentsLength() == 1 ? FixedLine.EMPTY_LINE : createLine(cmd.getFrom(1)));
		sendSuccess("La ligne a bien été ajoutée à l'hologramme.");
	}

	@Cmd (player = true, min = 2, args = { "INTEGER", "INTEGER" }, syntax = "<id de l'hologramme> <id de la ligne>")
	public void removeLine(CommandContext cmd) {
		Hologram hologram = getHologram(cmd.getArgument(0));
		if (hologram == null) return;
		try {
			hologram.removeLine(cmd.<Integer>getArgument(1));
			sendSuccess("La ligne a bien été supprimée de l'hologramme.");
		}catch (IndexOutOfBoundsException ex) {
			sendError("Il n'y a pas de ligne avec cet ID.");
		}
	}
	
	@Cmd (min = 1, args = "WORLD", syntax = "<world>")
	public void clearUnattachedHolograms(CommandContext cmd) {
		World world = cmd.getArgument(0);
		int removed = 0, kept = 0;
		for (ArmorStand armorStand : world.getEntitiesByClass(ArmorStand.class)) {
			if (armorStand.getPersistentDataContainer().has(HologramsManager.HOLOGRAM, PersistentDataType.INTEGER)) {
				int id = armorStand.getPersistentDataContainer().get(HologramsManager.HOLOGRAM, PersistentDataType.INTEGER);
				Hologram hologram = holograms.getHologram(id);
				if (!hologram.containsArmorStand(armorStand)) {
					armorStand.remove();
					removed++;
				}else kept++;
			}
		}
		sendSuccess("%d hologrammes supprimés, %d hologrammes gardés.", removed, kept);
	}

	private Hologram getHologram(int id) {
		Hologram hologram = holograms.getHologram(id);
		if (hologram == null) sendError("L'hologramme avec l'ID %d n'existe pas.", id);
		return hologram;
	}

	private AbstractLine<Hologram> createLine(String string) {
		if (string.equalsIgnoreCase("animation")) {
			return AnimLine.olympaAnimation();
		}else return new FixedLine<>(ColorUtils.color(string));
	}

}
