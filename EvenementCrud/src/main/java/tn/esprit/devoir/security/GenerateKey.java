package tn.esprit.devoir.security;

import io.jsonwebtoken.io.Encoders;
import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;

public class GenerateKey {
    public static void main(String[] args) {
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String base64Key = Encoders.BASE64.encode(key.getEncoded());
        System.out.println(base64Key);
    }
}
