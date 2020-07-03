package fr.olympa.api.hook;

public interface IProtocolSupport {

	void disable1_6();

	void disable1_7();

	void disable1_8();

	String getBigVersion(String version);

	String getRangeVersion();

	String getVersionUnSupportedInRange();

	String getVersionSupported();

}