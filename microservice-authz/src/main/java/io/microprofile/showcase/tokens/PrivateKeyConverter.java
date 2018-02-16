package io.microprofile.showcase.tokens;

import java.security.PrivateKey;

import org.eclipse.microprofile.config.spi.Converter;

import static io.microprofile.showcase.tokens.TokenUtils.readPrivateKey;

/**
 * A custom configuration converter for {@linkplain PrivateKey} injection using
 * {@linkplain org.eclipse.microprofile.config.inject.ConfigProperty}
 */
public class PrivateKeyConverter implements Converter<PrivateKey> {
    @Override
    public PrivateKey convert(String s) throws IllegalArgumentException {

        PrivateKey pk = null;
        try {
            pk = readPrivateKey("/privateKey.pem");
        } catch (Exception e) {
            IllegalArgumentException ex = new IllegalArgumentException("Failed to parse ");
            ex.initCause(e);
            throw ex;
        }
        return pk;
    }
}
