package fr.olympa.api.common.chat.response;

import fr.olympa.api.common.chat.sender.ISender;

public class ReponseEvent {

	public static void add(ISender sender, AwaitResponse<?> response) {
		AwaitResponse.map.put(sender, response);
	}

	public static void remove(ISender sender) {
		AwaitResponse.map.remove(sender);
	}

	public static void messageReceive(ISender sender, String message) {
		AwaitResponse<?> response = AwaitResponse.map.get(sender);
		Boolean apply = response.applyIfExist(message);
		if (apply == null)
			remove(sender);
		else if (!apply) {

		}
	}
}
