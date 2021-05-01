package exemple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.api.utils.CacheStats;
import fr.olympa.api.utils.TimeEvaluator;

public class OtherExemple {

	public OtherExemple() {
		TimeEvaluator time = new TimeEvaluator("SQL");
		// SQL STATERMENT
		time.print(); // print time taken

		// DEBUG Cache, Map, List
		Cache<String, Object> cache = CacheBuilder.newBuilder().recordStats().expireAfterWrite(5, TimeUnit.MINUTES).build();
		CacheStats.addCache("CACHE", cache);
		cache.put("test_cache", new Object());

		HashMap<String, Object> map = new HashMap<>();
		CacheStats.addDebugMap("MAP", map);
		map.put("test_map", new Object());

		List<String> list = new ArrayList<>();
		list.add("test_list");
		CacheStats.addDebugList("LIST", list);
	}
}
