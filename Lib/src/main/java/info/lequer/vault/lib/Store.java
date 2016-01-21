/*
 * 
 * 
 */
package info.lequer.vault.lib;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michel
 */
public class Store {

    private final Logger LOGGER = LoggerFactory.getLogger(Store.class);
    private final ArrayList<PasswordRecord> userStore = new ArrayList<>();
    private final char[] password;
    private final Path storePath;

    private Boolean isOpen = false;

    public Store(char[] password, Path storePath) {
        this.password = password;
        this.storePath = storePath;

    }

    public void open() throws Encryptor.InvalidPasswordException, StoreNotFoundException {
        BufferedReader br;
        String line, decrypted;
        // read the file and decrypt in hash map

        try {

            br = new BufferedReader(new FileReader(storePath.toFile()));
            while ((line = br.readLine()) != null) {
                decrypted = Encryptor.decrypt(password, line);
                userStore.add(new PasswordRecord(decrypted));
            }
            isOpen = true;
        } catch (IOException ex) {
            isOpen = false;
            throw new StoreNotFoundException();
        }
    }

    public Boolean isOpen() {
        return isOpen;
    }

    public void add(String key, String password) {
        userStore.add(new PasswordRecord(key, password));
    }

    public PasswordRecord get(int index) {
        return userStore.get(index);
    }

    public PasswordRecord get(String key) {
        Optional<PasswordRecord> record = userStore.stream().filter((p) -> p.key.equalsIgnoreCase(key)).findFirst();
        if (record.isPresent()) {
            return record.get();
        }
        return null;
    }

    public ArrayList<PasswordRecord> getAll() {
        return userStore;
    }

    public Boolean contains(String key) {
        Optional<PasswordRecord> record = userStore.stream().filter((p) -> p.key.equalsIgnoreCase(key)).findFirst();
        return record.isPresent();
    }

    public Boolean update(String key, String password) {
        Optional<PasswordRecord> record = userStore.stream().filter((p) -> p.key.equalsIgnoreCase(key)).findFirst();
        if (record.isPresent()) {
            userStore.set(userStore.indexOf(record.get()), new PasswordRecord(key, password));
            return true;
        }
        return false;
    }

    public Boolean remove(String key) {
        Optional<PasswordRecord> record = userStore.stream().filter((p) -> p.key.equalsIgnoreCase(key)).findFirst();
        if (record.isPresent()) {
            userStore.remove(record.get());
            return true;
        }
        return false;
    }

    public void save() throws StoreNotFoundException,Encryptor.InvalidEncryptionException {
        FileWriter writer;
        try {

            writer = new FileWriter(storePath.toFile());
            for (PasswordRecord p : userStore) {
                String output = Encryptor.encrypt(password, p.asLine());
                writer.write(output + "\n");
            }
            writer.close();
        } catch (IOException ex) {
           throw new StoreNotFoundException();
        }
    }

    public static class StoreNotFoundException extends Exception {

        public StoreNotFoundException() {
        }
    }
    
    

}
