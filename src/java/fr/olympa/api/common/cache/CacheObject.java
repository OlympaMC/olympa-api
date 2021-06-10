package fr.olympa.api.common.cache;

import fr.olympa.api.utils.Utils;

/**
 * Ajouter un maximum d'information afin de gérer au mieux le cache d'un objet
 */
public class CacheObject<T> {

	private static int timeBeforeEject = 1 * 60;

	private static long getTimeInSeconds() {
		return Utils.getCurrentTimeInSeconds();
	}

	T object;

	// timestamp of first getting object (by redis or db)
	private final long timeOfGetting = getTimeInSeconds();

	// if the version can be modified in other instance
	// if newerVersionOfObjectAreInOtherInstance is false is viableSource is true
	private boolean newerVersionOfObjectAreInOtherInstance = false;
	private boolean isObselete = false;

	// if source is from instance where the object is, or up to date object by redis
	private boolean viableSource = true;

	// last timestamp of update/set data in object
	private long timeLastOperation;

	// last timestamp of save object in db (or maybe redis)
	private long timeLastSaveOperation;

	// last timestamp of get object (starting with same as timeOfGetting)
	private long timeLastGetting;

	public CacheObject(T object) {
		this.object = object;
	}

	public boolean canDoOperation() {
		return viableSource;
	}

	public boolean isUpToDate() {
		return !isObselete && !newerVersionOfObjectAreInOtherInstance;
	}

	public boolean isObselete() {
		return isObselete || (isObselete = timeOfGetting + timeBeforeEject > getTimeInSeconds());
	}
}
