package io.microprofile.showcase.tokens;

import java.util.AbstractMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


import javax.enterprise.context.ApplicationScoped;

/**
 * A shared token storage class
 */
@ApplicationScoped
public class TokenStorage extends AbstractMap<String, String> {
    private ConcurrentHashMap<String, String> lastTokens = new ConcurrentHashMap<>();

    @Override
    public String get(Object key) {
        return lastTokens.get(key);
    }

    @Override
    public String put(String key, String value) {
        return lastTokens.put(key, value);
    }

    @Override
    public int size() {
        return lastTokens.size();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return lastTokens.entrySet();
    }
}
