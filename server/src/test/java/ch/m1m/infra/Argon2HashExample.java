package ch.m1m.infra;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

// from: https://www.heise.de/hintergrund/Secure-Coding-Passwort-Hashing-zum-Schutz-vor-Brute-Force-und-Rainbow-Tabellen-10265244.html

public class Argon2HashExample {

    private static final SecureRandom random = new SecureRandom();

    public static void main(String[] args) {

        String password = "DeinSicheresPasswort";

        byte[] salt = generateSalt(16);
        byte[] hash = generateHashFromSaltAndPlainPassword(salt, password);

        String saltBase64 = bytesToBase64(salt);
        String hashBase64 = bytesToBase64(hash);

        System.out.printf("Salt: %s  PasswordHash: %s%n", saltBase64, hashBase64);

        // NOTE: to enforce a failing verify match, reset the password to a different value!
        //password = "toto";
        // boolean passwordMatched = verifyPasswordWithSaltAndHash(password, salt, hash);
        boolean passwordMatched = verifyPasswordWithSaltAndHashAsBase64(password, saltBase64, hashBase64);
        if (passwordMatched) {
            System.out.println("verify was successful (password matched)");
        } else {
            throw new IllegalStateException("Authentication failed (userid/password)");
        }
    }

    private static byte[] generateHashFromSaltAndPlainPassword(byte[] salt, String password) {
        Argon2Parameters.Builder builder = new Argon2Parameters
                .Builder(Argon2Parameters.ARGON2_id)
                .withSalt(salt)
                .withMemoryPowOfTwo(16)
                .withParallelism(4)
                .withIterations(3);

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(builder.build());

        byte[] hash = new byte[64];
        generator.generateBytes(password.getBytes(), hash);
        return hash;
    }

    private static boolean verifyPasswordWithSaltAndHashAsBase64(String password, String saltBase64, String hashBase64) {
        Base64.Decoder base64decoder = Base64.getDecoder();
        byte[] salt = base64decoder.decode(saltBase64);
        byte[] hash = base64decoder.decode(hashBase64);
        return verifyPasswordWithSaltAndHash(password, salt, hash);
    }

    private static boolean verifyPasswordWithSaltAndHash(String password, byte[] salt, byte[] oldHash) {
        byte[] actualHash = generateHashFromSaltAndPlainPassword(salt, password);
        return Arrays.equals(oldHash, actualHash);
    }

    private static String bytesToBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] generateSalt(int length) {
        byte[] salt = new byte[length];
        random.nextBytes(salt);
        return salt;
    }
}
