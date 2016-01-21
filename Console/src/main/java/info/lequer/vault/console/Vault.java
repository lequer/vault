/*
 * 
 * 
 */
package info.lequer.vault.console;

import info.lequer.vault.lib.Encryptor;
import java.io.Console;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.lequer.vault.lib.Store;

/**
 *
 * @author Michel
 */
public class Vault {

    private static Store vault;

    private final HashMap<String, String> userStore = new HashMap<>();
    private static Path vaultPath;
    private final static String APP_DIR = ".vault";
    private final static String VAULT_FILE = "store.txt";
    private final static Logger LOGGER = LoggerFactory.getLogger(Vault.class);

    private static char[] password;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        vaultPath = Paths.get(System.getProperty("user.home"), APP_DIR, VAULT_FILE);
        LOGGER.debug("Setting userFile to {} ", vaultPath.toString());

        /**
         * Get the console
         */
        Console console = System.console();
        if (console == null) {
            System.out.println("Couldn't get Console instance");
            System.exit(0);
        }
        /**
         * Check if the application is installed
         */
        if (!checkInstall()) {
            installProcess(console);
        }

        password = console.readPassword("Password: ");
        vault = new Store(password, vaultPath);
        try {
            vault.open();
            waitForCommand(console);
        } catch (Encryptor.InvalidPasswordException | Store.StoreNotFoundException ex) {
            System.out.println("Couldn't open the store");
            System.exit(0);
        }

    }

    private static boolean checkInstall() {
        return Files.exists(vaultPath);
    }

    private static void installProcess(Console console) {
        try {
            if (Files.notExists(vaultPath)) {
                console.printf("Do you wish to create vault file? (y/n):");
                String response = console.readLine();
                if (!response.equalsIgnoreCase("y")) {
                    console.printf("Aborting installation. Bye\n");
                    System.exit(0);
                }
                boolean success = vaultPath.getParent().toFile().mkdir();
                if (success) {
                    System.out.println("Directory: " + vaultPath.toString() + " created");
                    Files.createFile(vaultPath);
                }
            }

        } catch (Exception ex) {
            console.printf("Unable to create the vault file\n");
            LOGGER.error("Error: {}", ex.getMessage());
            System.exit(1);
        }
    }

    public static void waitForCommand(Console console) {
        //  open up standard input
        String command = "";
        while (!command.equalsIgnoreCase("quit")) {
            command = console.readLine("Vault> ").trim();
            // default listing
            if (command.equals("")) {
                command = "list";
            }

            if (command.equalsIgnoreCase("list")) {
                console.printf("%-25s |  %-10s %n", "Key", "Value");
                console.printf("---------------------------------------------\n");
                vault.getAll().stream().forEach((pr) -> {
                    console.printf("%-25s |  %-10s %n", pr.key, pr.password);
                });
                console.printf("---------------------------------------------\n");
            }

            if (command.contains("add")) {
                String[] s = command.split(" ");
                if (s.length < 3) {
                    console.printf("add KEY PASS\n");
                } else {

                    if (!vault.contains(s[1])) {
                        vault.add(s[1], s[2]);
                    } else {
                        console.printf("Store already contains key " + s[1]+"\n");
                    }
                  //  vault.save();
                }
            }

            if (command.contains("remove")) {
                String[] s = command.split(" ");
                if (s.length < 2) {
                    console.printf("remove KEY\n");
                } else {
                    if (vault.contains(s[1])) {
                        vault.remove(s[1]);
                    } else {
                        console.printf("could not find key " + s[1]+"\n");
                    }
                   // vault.save();
                }
            }

            if (command.contains("update")) {
                String[] s = command.split(" ");
                if (s.length < 3) {
                    console.printf("update KEY VALUE\n");
                } else {
                    if (vault.contains(s[1])) {
                        vault.update(s[1], s[2]);
                    } else {
                        console.printf("could not find key " + s[1]+ "\n");
                    }
                   // vault.save();
                }
            }
        }
        console.printf("Encrypting ...");
        try {
            vault.save();
        } catch (Store.StoreNotFoundException | Encryptor.InvalidEncryptionException ex) {
            System.out.println("\nCouldn't get encrypt the store! Quitting.");
            System.exit(0);
        }
         console.printf(" Done.\n");

    }

}
