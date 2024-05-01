package es.uvigo.ei.sing.singulator.utils;

import java.util.HashMap;

public class CustomMap extends HashMap {
	private static final long serialVersionUID = 1L;

	public CustomMap() {
		super();
	}

	@Override
	public void clear() {
		// Reset parameters to 0 but maintain the keys
		for (Object key : keySet()) {
			put(key, 0);
		}
	}
}
