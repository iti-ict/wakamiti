/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.server.infra.auth;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.*;
import java.util.Base64;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.jwt.build.Jwt;
import es.iti.wakamiti.server.spi.TokenAuthentication;

@ApplicationScoped
public class JwtAuthentication implements TokenAuthentication {

    @ConfigProperty(name = "mp.jwt.verify.privatekey.location")
    String privateKeyLocation;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name= "mp.jwt.verify.tokenLife")
    Duration tokenLife;


    private PrivateKey privateKey;

    private final Clock clock;


    public JwtAuthentication(Clock clock) {
        this.clock = clock;
    }

    public JwtAuthentication() {
        this(Clock.systemUTC());
    }



    @Override
    public String newToken(String user) {
        return Jwt.claims()
            .issuer(issuer)
            .issuedAt(clock.instant())
            .subject(user)
            .expiresIn(tokenLife)
            .jws()
            .keyId(privateKeyLocation)
            .sign(privateKey());
    }




    private PrivateKey privateKey() {
        try {
            if (privateKey == null) {
                privateKey = readPrivateKey(privateKeyLocation);
            }
            return privateKey;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static PrivateKey readPrivateKey(final String pemResName)
	throws IOException, GeneralSecurityException {
        try (InputStream contentIS = contextClassLoader().getResourceAsStream(pemResName)) {
            return decodePrivateKey(new String(contentIS.readAllBytes(), StandardCharsets.UTF_8));
        }
    }


    private static PrivateKey decodePrivateKey(final String pemEncoded)
	throws GeneralSecurityException {
        byte[] encodedBytes = toEncodedBytes(pemEncoded);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }


    private static byte[] toEncodedBytes(final String pemEncoded) {
        final String normalizedPem = removeBeginEnd(pemEncoded);
        return Base64.getDecoder().decode(normalizedPem);
    }


    private static String removeBeginEnd(String pem) {
        pem = pem.replaceAll("-----BEGIN (.*)-----", "");
        pem = pem.replaceAll("-----END (.*)----", "");
        pem = pem.replace("\r\n", "");
        pem = pem.replace("\n", "");
        return pem.trim();
    }


	private static ClassLoader contextClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}


}