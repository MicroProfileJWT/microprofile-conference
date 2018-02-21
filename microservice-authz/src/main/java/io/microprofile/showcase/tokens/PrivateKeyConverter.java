package io.microprofile.showcase.tokens;

import java.security.PrivateKey;

import org.eclipse.microprofile.config.spi.Converter;

import static io.microprofile.showcase.tokens.TokenUtils.readPrivateKey;

/**
 * A custom configuration converter for {@linkplain PrivateKey} injection using
 * {@linkplain org.eclipse.microprofile.config.inject.ConfigProperty}
 */
public class PrivateKeyConverter implements Converter<PrivateKey> {
    /**
     * Converts a string to a PrivateKey by loading it as a classpath resource
     * @param s - the string value to convert
     * @return the PrivateKey loaded as a resource
     * @throws IllegalArgumentException - on failure to load the key
     */
    @Override
    public PrivateKey convert(String s) throws IllegalArgumentException {

        PrivateKey pk = null;
        try {
            pk = readPrivateKey(s);
        } catch (Exception e) {
            IllegalArgumentException ex = new IllegalArgumentException("Failed to parse ");
            ex.initCause(e);
            throw ex;
        }
        return pk;
    }
}
