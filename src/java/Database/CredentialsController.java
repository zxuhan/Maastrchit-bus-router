package src.java.Database;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.*;

public class CredentialsController {
    private static char[] password = "DACS".toCharArray();
    private static SecretKey key;
    private static String EncryptedPath;
    private static String path;
    private static String alias = "credentials";
    private static String KeyStorePath = "src/java/Database/keystore";

    public CredentialsController() throws Exception {
        readKeyFile();
        initializePath();
        decryptCredentials();
    }

    private void initializePath(){
        if (System.getProperty("os.name").startsWith("Mac") || System.getProperty("os.name").startsWith("Linux")) {
            EncryptedPath = "src/java/Database/credentials-encrypted.txt";
            path = "src/java/Database/credentials.txt";
        }
        else {
            EncryptedPath = "src\\java\\Database\\credentials-encrypted.txt";
            path = "src\\java\\Database\\credentials.txt";

        }
    }


    private static void encryptFile(String inputFile, String outputFile, SecretKey secretKey) throws Exception {
        // Ensure that the Cipher instance is initialized here
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec); // Proper initialization

        try (FileInputStream fileInputStream = new FileInputStream(inputFile);
             FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            fileOutputStream.write(iv); // Write IV at the beginning of the output for use during decryption

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    fileOutputStream.write(output);
                }
            }

            byte[] outputBytes = cipher.doFinal(); // Completing the encryption operation
            if (outputBytes != null) {
                fileOutputStream.write(outputBytes);
            }
        }

        // Securely delete the original file
        secureDelete(inputFile);
    }

    private static void secureDelete(String filePath) throws IOException {
        File file = new File(filePath);

        if (file.exists()) {
            // Overwrite the file with dummy data
            try (FileOutputStream fos = new FileOutputStream(file);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                byte[] dummyData = new byte[1024];
                new SecureRandom().nextBytes(dummyData);
                long length = file.length();
                for (long i = 0; i < length; i += 1024) {
                    bos.write(dummyData);
                }
            }

            // // Delete the file
            // if (file.exists()) {
            //     if (!file.delete()) {
            //         throw new IOException("Failed to delete the file securely");
            //     }
            // }
        }
    }

    private static void decryptFile(String inputFile, String outputFile) throws Exception {
        KeyStore keystore = KeyStore.getInstance("JCEKS");
        FileInputStream keystoreStream = new FileInputStream(new File(KeyStorePath));
        keystore.load(keystoreStream, password);
        keystoreStream.close();

        KeyStore.ProtectionParameter param = new KeyStore.PasswordProtection(password);
        KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keystore.getEntry(alias, param);
        SecretKey secretKey = entry.getSecretKey();

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // Read the encrypted file and extract the IV and encrypted data
        FileInputStream fis = new FileInputStream(EncryptedPath);
        byte[] fileIv = new byte[16]; // The size of the IV should match the block size
        fis.read(fileIv); // Read the IV from the beginning of the file
        IvParameterSpec ivParameterSpec = new IvParameterSpec(fileIv);

        // Decrypt the remaining data
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        FileOutputStream fos = new FileOutputStream(path);
        byte[] buffer = new byte[1024];
        int numBytesRead;
        while ((numBytesRead = fis.read(buffer)) != -1) {
            byte[] output = cipher.update(buffer, 0, numBytesRead);
            if (output != null) {
                fos.write(output);
            }
        }
        byte[] finalBytes = cipher.doFinal();
        if (finalBytes != null) {
            fos.write(finalBytes);
        }
        secureDelete(inputFile);
        fis.close();
        fos.flush();
        fos.close();
    }

    private static void keystore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JCEKS");

        // Load the keystore file; if none exists, create one
        File keystoreFile = new File(KeyStorePath);
        if (!keystoreFile.exists()) {
            keystoreFile.createNewFile();
            keyStore.load(null, password); // Load an empty keystore
        } else {
            keyStore.load(new FileInputStream(keystoreFile), password);
        }
        // Generate and store a secret key
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);  // Ensure your environment supports 128-bit keys
        SecretKey secretKey = keyGenerator.generateKey();
        KeyStore.SecretKeyEntry keyEntry = new KeyStore.SecretKeyEntry(secretKey);
        KeyStore.ProtectionParameter protectionParam = new KeyStore.PasswordProtection(password);
        keyStore.setEntry(alias, keyEntry, protectionParam);

        // Save the keystore
        keyStore.store(new FileOutputStream(keystoreFile), password);
    }
    private static void readKeyFile() throws Exception{
        KeyStore keystore = KeyStore.getInstance("JCEKS"); // Ensure you use JCEKS for secret keys
        FileInputStream keystoreStream = new FileInputStream(KeyStorePath);
        keystore.load(keystoreStream, password);
        keystoreStream.close();

        // Retrieve the secret key
        KeyStore.ProtectionParameter param = new KeyStore.PasswordProtection(password);
        KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keystore.getEntry(alias, param);
        key = entry.getSecretKey();
        System.out.println(key.toString());
    }

    public static void decryptCredentials() throws Exception {
        decryptFile(EncryptedPath, path);
    }
    public static void encryptCredentials() throws Exception {
        encryptFile(path, EncryptedPath, key);
    }
}

