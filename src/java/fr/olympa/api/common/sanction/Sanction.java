package fr.olympa.api.common.sanction;

import java.sql.SQLException;
import java.util.Set;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public interface Sanction {

	String getTarget();

	long getAuthor();

	String getAuthorName();

	long getBanTime();

	long getCreated();

	long getExpires();

	long getId();

	String getReason();

	boolean isTarget(OlympaPlayer olympaPlayer);

	String getTargetIp();

	Long getTargetId();

	Set<ProxiedPlayer> getOnlinePlayers();

	Set<OlympaPlayerInformations> getPlayersInfos() throws SQLException;

	String getPlayersNames() throws SQLException;


	boolean isPermanent();


	void setId(long id2);

	BaseComponent[] toBaseComplement();

	OlympaSanctionType getType();

}