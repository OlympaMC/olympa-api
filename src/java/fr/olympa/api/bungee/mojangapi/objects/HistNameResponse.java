package fr.olympa.api.bungee.mojangapi.objects;

import com.google.gson.Gson;

// TODO https://api.mojang.com/user/profiles/ca0a166316964d62b93f281965522a76/names
public class HistNameResponse {
	
	public static HistNameResponse get(String json) {
		return new Gson().fromJson(json, HistNameResponse.class);
	}
}
