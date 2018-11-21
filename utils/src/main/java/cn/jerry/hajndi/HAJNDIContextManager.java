package cn.jerry.hajndi;

import java.util.HashMap;

public class HAJNDIContextManager {
	private static HashMap<String, HAJNDIContext> cachedContexts = new HashMap<String, HAJNDIContext>();

	public static HAJNDIContext loadContext(final String confFilePath) {
		HAJNDIContext context = cachedContexts.get(confFilePath);
		if (context == null) {
		    synchronized (confFilePath) {
                context = cachedContexts.computeIfAbsent(confFilePath, k -> new HAJNDIContext(confFilePath));
            }
		}
		return context;
	}

}
