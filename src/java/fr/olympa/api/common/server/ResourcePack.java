package fr.olympa.api.common.server;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

import com.google.common.hash.Hashing;

public class ResourcePack {
	private String url;
	private String hash;
	
	public ResourcePack() {}
	
	public ResourcePack(String url, String hash) {
		this.url = url;
		if (hash != null)
			this.hash = hash.toLowerCase(Locale.ROOT);
		else
			this.hash = Hashing.sha1().hashString(url, StandardCharsets.UTF_8).toString().toLowerCase(Locale.ROOT);
		
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getHash() {
		return hash;
	}
	
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	@Override
	public String toString() {
		return "ResourcePack(url=" + url + ", hash=" + hash + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		else if (obj instanceof ResourcePack other) {
			if (!Objects.equals(url, other.url)) return false;
			if (!Objects.equals(hash, other.hash)) return false;
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int result = 1;
		result = result * 59 + (url == null ? 0 : url.hashCode());
		result = result * 59 + (hash == null ? 0 : hash.hashCode());
		return result;
	}
}