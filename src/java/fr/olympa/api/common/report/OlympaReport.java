package fr.olympa.api.common.report;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;

import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.utils.Utils;

public class OlympaReport {

	@Expose
	private long id;
	@Expose
	private long targetId;
	@Expose
	private String targetName;
	@Expose
	private long authorId;
	@Expose
	private String authorName;
	private final ReportReason reason;
	@Expose
	private String reasonName;
	@Expose
	private String note;
	@Expose
	private List<ReportStatusInfo> statusInfo;
	@Expose
	private long time;
	@Expose
	private String serverName;

	public OlympaReport(long targetId, long authorId, ReportReason reason, String serverName, String note) {
		this.targetId = targetId;
		this.authorId = authorId;
		this.reason = reason;
		if (reason != null)
			reasonName = reason.getReason();
		this.serverName = serverName;
		time = Utils.getCurrentTimeInSeconds();
		if (note != null && !note.isEmpty())
			this.note = note;
		if (authorId == 0l)
			addStatusInfo(new ReportStatusInfo(ReportStatus.AUTO, null));
		else
			addStatusInfo(new ReportStatusInfo(ReportStatus.OPEN, null));
	}

	public OlympaReport(ResultSet resultSet) throws SQLException {
		id = resultSet.getLong("id");
		targetId = resultSet.getLong("target_id");
		authorId = resultSet.getLong("author_id");
		reasonName = resultSet.getString("reason");
		reason = ReportReason.get(reasonName);
		serverName = resultSet.getString("server");
		time = resultSet.getTimestamp("time").getTime() / 1000L;
		if (resultSet.getString("status_info") != null) {
			Type founderListType = new TypeToken<ArrayList<ReportStatusInfo>>() {}.getType();
			statusInfo = new Gson().fromJson(resultSet.getString("status_info"), founderListType);
		}
		if (resultSet.getString("note") != null && !resultSet.getString("note").isEmpty())
			note = resultSet.getString("note");
	}

	public boolean isFromConsole() {
		return authorId == 0;
	}

	public void resolveAll() {
		resolveAuthorName();
		resolveTargetName();
	}

	public void resolveAuthorName() {
		if (authorName == null || authorName.isBlank())
			authorName = AccountProviderAPI.getter().getPlayerInformations(authorId).getName();
	}

	public void resolveTargetName() {
		if (targetName == null || targetName.isBlank())
			targetName = AccountProviderAPI.getter().getPlayerInformations(targetId).getName();
	}

	/**
	 * Nullable if the name of target was not resolve. If the name was not retrieved from the id. (for example if the object is out of the database)
	 */
	@Nullable
	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	/**
	 * Nullable if the name of target was not resolve. If the name was not retrieved from the id. (for example if the object is out of the database)
	 */
	@Nullable
	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getNote() {
		return note;
	}

	public long getAuthorId() {
		return authorId;
	}

	public long getId() {
		return id;
	}

	/**
	 * Null if the ReportReason is specific to a server, and we are not in the specific server.
	 * Or if object is from JSON, like in redis channels
	 */
	@Nullable
	public ReportReason getReason() {
		return reason;
	}

	public String getReasonName() {
		return reasonName;
	}

	public String getReasonNameUpper() {
		return reasonName.toUpperCase();
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
		OlympaPlayerInformations opTarget = AccountProviderAPI.getter().getPlayerInformations(targetId);
		OlympaPlayerInformations opAuthor = AccountProviderAPI.getter().getPlayerInformations(authorId);
		lore.add(String.format("&2%s &a->&2 %s", opAuthor.getName(), opTarget.getName()));
		lore.add(String.format("&aServeur %s", serverName));
		lore.add(String.format("&aStatut %s", getStatus().getNameColored()));
		lore.add(String.format("&aRaison &2%s", reasonName));
		if (note != null && !note.isBlank())
			lore.add(String.format("&aNote &2%s", note));
		lore.add(String.format("&aDate &2%s &a(%s)", Utils.timestampToDateAndHour(time), Utils.timestampToDuration(time)));
		if (statusInfo.size() > 1)
			lore.add(String.format("&aStatuts précédent &2%s", statusInfo.stream().limit(statusInfo.size() - 1l)
					.map(rsi -> rsi.getStatus().getNameColored() + (!Objects.isNull(rsi.getTime()) ? " &a(" + Utils.timestampToDuration(rsi.getTime()) + ")" : "")).collect(Collectors.joining("&a, &2"))));
		return lore;
	}
}
