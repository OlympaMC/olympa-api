package fr.olympa.api.report;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Utils;

public class OlympaReport {

	long id = 0;
	long targetId;
	public String targetName;
	long authorId;
	public String authorName;
	ReportReason reason;
	String note;

	List<ReportStatusInfo> statusInfo;
	long time;

	String serverName;

	public OlympaReport(long targetId, long authorId, ReportReason reason, String serverName, String note) {
		this.targetId = targetId;
		this.authorId = authorId;
		this.reason = reason;
		this.serverName = serverName;
		time = Utils.getCurrentTimeInSeconds();
		if (note != null && !note.isEmpty())
			this.note = note;
		addStatusInfo(new ReportStatusInfo(ReportStatus.OPEN, null));
	}

	public OlympaReport(ResultSet resultSet) throws SQLException {
		id = resultSet.getLong("id");
		targetId = resultSet.getLong("target_id");
		authorId = resultSet.getLong("author_id");
		reason = ReportReason.get(resultSet.getInt("reason"));
		serverName = resultSet.getString("server");
		time = resultSet.getTimestamp("time").getTime() / 1000L;
		if (resultSet.getString("status_info") != null) {
			Type founderListType = new TypeToken<ArrayList<ReportStatusInfo>>() {}.getType();
			statusInfo = new Gson().fromJson(resultSet.getString("status_info"), founderListType);
		}
		if (resultSet.getString("note") != null && !resultSet.getString("note").isEmpty())
			note = resultSet.getString("note");
	}

	public String getNote() {
		return note;
	}

	@Deprecated
	public UUID getAuthor() {
		return null;
	}

	public long getAuthorId() {
		return authorId;
	}

	public long getId() {
		return id;
	}

	public ReportReason getReason() {
		return reason;
	}

	public String getServerName() {
		return serverName;
	}

	public ReportStatus getStatus() {
		return getLastStatusInfo().getStatus();
	}

	public ReportStatusInfo getLastStatusInfo() {
		if (statusInfo != null && !statusInfo.isEmpty())
			return statusInfo.get(statusInfo.size() - 1);
		return null;
	}

	public List<ReportStatusInfo> getStatusInfo() {
		return statusInfo;
	}

	public void addStatusInfo(ReportStatusInfo statusInfo) {
		if (this.statusInfo == null)
			this.statusInfo = new ArrayList<>();
		this.statusInfo.add(statusInfo);
	}

	public String getStatusString() {
		return new Gson().toJson(this);
	}

	@Deprecated
	public UUID getTarget() {
		return null;
	}

	public long getTargetId() {
		return targetId;
	}

	public long getTime() {
		return time;
	}

	public long getLastUpdate() {
		ReportStatusInfo last = getLastStatusInfo();
		if (last != null && last.getTime() != null && last.getTime() != 0)
			return last.getTime();
		else
			return time;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getStatusInfoToJson() {
		return new Gson().toJson(statusInfo);
	}

	public List<String> getLore() {
		List<String> lore = new ArrayList<>();
		lore.add(String.format("&aN°&2%s", String.valueOf(id)));
		OlympaPlayerInformations opTarget = AccountProvider.getPlayerInformations(targetId);
		OlympaPlayerInformations opAuthor = AccountProvider.getPlayerInformations(authorId);
		lore.add(String.format("&2%s &a->&2 %s", opAuthor.getName(), opTarget.getName()));
		lore.add(String.format("&aServeur %s", serverName));
		lore.add(String.format("&aStatus %s", getStatus().getNameColored()));
		lore.add(String.format("&aRaison &2%s", reason.getReason()));
		if (note != null && !note.isBlank())
			lore.add(String.format("&aNote &2%s", note));
		lore.add(String.format("&aDate &2%s &a(%s)", Utils.timestampToDateAndHour(time), Utils.timestampToDuration(time)));
		if (statusInfo.size() > 1)
			lore.add(String.format("&aDerniers statuts &2%s", statusInfo.stream().skip(1).map(rsi -> rsi.getStatus() + " &a(" + Utils.timestampToDuration(rsi.getTime()) + ")").collect(Collectors.joining("&a, &2"))));
		return lore;
	}
}
