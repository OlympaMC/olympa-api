package fr.olympa.api.report;

import java.util.ArrayList;
import java.util.List;

import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Utils;

public class ReportStatusInfo {

	String note;
	Long time;
	ReportStatus status;
	Long idAuthor;

	public ReportStatusInfo(ReportStatus status, String note) {
		this.status = status;
		this.note = note;
	}

	public ReportStatusInfo(String note, long idAuthor, ReportStatus status) {
		this.note = note;
		this.idAuthor = idAuthor;
		this.status = status;
		time = Utils.getCurrentTimeInSeconds();
	}

	public String getNote() {
		return note;
	}

	public ReportStatus getStatus() {
		return status;
	}

	public Long getTime() {
		return time;
	}

	public List<String> getLore() {
		List<String> lore = new ArrayList<>();
		OlympaPlayerInformations opAuthor = AccountProvider.getPlayerInformations(idAuthor);
		lore.add(String.format("&aChangement de &2%s", opAuthor.getName()));
		lore.add(String.format("&aStatus %s", status.getNameColored()));
		if (note != null && !note.isBlank())
			lore.add(String.format("&aNote &2%s", note));
		lore.add(String.format("&aDate &2%s &a(%s)", Utils.timestampToDateAndHour(time), Utils.timestampToDuration(time)));
		return lore;
	}
}
