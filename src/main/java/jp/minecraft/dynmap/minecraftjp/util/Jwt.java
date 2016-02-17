package jp.minecraft.dynmap.minecraftjp.util;

import com.google.common.io.BaseEncoding;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * Created by ayu on 2015/04/25.
 */
public class Jwt {
    public static JSONObject decode(String jwt, String key) {
        String parts[] = jwt.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT");
        }

        JSONObject header, payload;
        try {
            header = (JSONObject) JSONValue.parse(new String(BaseEncoding.base64Url().decode(parts[0]), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Invalid Characters in header: " + e.getMessage());
        }

        try {
            payload = (JSONObject) JSONValue.parse(new String(BaseEncoding.base64Url().decode(parts[1]), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Invalid Characters in payload: " + e.getMessage());
        }

        byte[] signature = BaseEncoding.base64Url().decode(parts[2]);

        String alg = (String) header.get("alg");
        if (alg == null) {
            throw new IllegalArgumentException("Missing alg");
        }

        String signData = parts[0] + "." + parts[1];
        if (alg.startsWith("HS256")) {
            if (!Arrays.equals(signature, generateHmac("HmacSHA256", signData, key))) {
                throw new IllegalArgumentException("Invalid signature");
            }
        } else if (alg.startsWith("HS384")) {
            if (!Arrays.equals(signature, generateHmac("HmacSHA384", signData, key))) {
                throw new IllegalArgumentException("Invalid signature");
            }
        } else if (alg.startsWith("HS512")) {
            if (!Arrays.equals(signature, generateHmac("HmacSHA512", signData, key))) {
                throw new IllegalArgumentException("Invalid signature");
            }
        } else if (alg.startsWith("RS")) {
            PublicKey publicKey;

            if (key.startsWith("-----BEGIN CERTIFICATE-----")) {
                InputStream is = new ByteArrayInputStream(key.getBytes());
                Certificate certificate;
                try {
                    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                    certificate = certificateFactory.generateCertificate(is);
                    publicKey = certificate.getPublicKey();
                } catch (CertificateException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            } else {
                try {
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(key.getBytes()));
                } catch (NoSuchAlgorithmException e) {
                    throw new IllegalArgumentException(e.getMessage());
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                    throw new IllegalArgumentException(e.getMessage());
                }
            }

            if (alg.equals("RS256")) {
                if (!verifySignature("SHA256withRSA", signData, publicKey, signature)) {
                    throw new IllegalArgumentException("Invalid signature");
                }
            } else if (alg.equals("RS384")) {
                if (!verifySignature("SHA384withRSA", signData, publicKey, signature)) {
                    throw new IllegalArgumentException("Invalid signature");
                }
            } else if (alg.equals("RS512")) {
                if (!verifySignature("SHA512withRSA", signData, publicKey, signature)) {
                    throw new IllegalArgumentException("Invalid signature");
                }
            } else {
                throw new IllegalArgumentException("Unsupported algorithm: " + alg);
            }
        } else {
            throw new IllegalArgumentException("Unsupported algorithm: " + alg);
        }

        return payload;
    }

    private static byte[] generateHmac(String alg, String data, String key) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), alg);

            Mac mac = Mac.getInstance(alg);
            mac.init(signingKey);

            return mac.doFinal(data.getBytes());
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private static boolean verifySignature(String alg, String data, PublicKey key, byte[] signature) {
        try {
            Signature sig = Signature.getInstance(alg);
            sig.initVerify(key);
            sig.update(data.getBytes());
            return sig.verify(signature);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
