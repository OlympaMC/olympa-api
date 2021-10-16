package fr.olympa.api.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import fr.olympa.api.common.observable.AbstractObservable;

public class Hierarchy<T> extends AbstractObservable {
	
	private final String key;
	private Hierarchy<T> parent;
	private BiMap<String, Hierarchy<T>> subHierarchies;
	private List<T> objects;
	
	public Hierarchy(String key) {
		this(key, null, HashBiMap.create(), new ArrayList<>());
	}
	
	public Hierarchy(String key, Hierarchy<T> parent, BiMap<String, Hierarchy<T>> subHierarchies, List<T> objects) {
		this.key = key;
		this.parent = parent;
		this.subHierarchies = subHierarchies;
		this.objects = objects;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getPath() {
		LinkedList<String> parents = new LinkedList<>();
		Hierarchy<T> hier = this;
		while ((hier = hier.parent) != null) {
			parents.addFirst(hier.key);
		}
		parents.add(key);
		return String.join("/", parents);
	}
	
	public Hierarchy<T> getParent() {
		return parent;
	}
	
	public boolean hasParent() {
		return parent != null;
	}
	
	public void setParent(Hierarchy<T> parent) {
		this.parent = parent;
		update();
	}
	
	public BiMap<String, Hierarchy<T>> getSubHierarchies() {
		return subHierarchies;
	}
	
	public Hierarchy<T> getSubHierarchy(String key) {
		return subHierarchies.get(key);
	}
	
	public Hierarchy<T> getOrCreateSubHierarchy(String key) {
		Hierarchy<T> hierarchy = subHierarchies.get(key);
		if (hierarchy != null) return hierarchy;
		hierarchy = new Hierarchy<>(key);
		putSubHierarchy(key, hierarchy);
		return hierarchy;
	}
	
	public List<T> getObjects() {
		return objects;
	}
	
	public void putSubHierarchy(String key, Hierarchy<T> subHierarchy) {
		if (subHierarchy.hasParent()) throw new IllegalArgumentException("Hierarchy already has a parent");
		subHierarchies.put(key, subHierarchy);
		subHierarchy.setParent(this);
		update();
	}
	
	public void removeSubHierarchy(String key) {
		Hierarchy<T> subHierarchy = subHierarchies.remove(key);
		if (subHierarchy != null) {
			subHierarchy.setParent(null);
			update();
		}
	}
	
	public void addObject(T object) {
		objects.add(object);
		update();
	}
	
}
