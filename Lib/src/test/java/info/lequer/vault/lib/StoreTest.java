/*
 * Copyrigth Michel Le Quer
 * michel@lequer.info
 * 
 */
package info.lequer.vault.lib;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Iterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pryon
 */
public class StoreTest {

    private Store store;
    private URL url;

    public StoreTest() {

    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        url = Thread.currentThread().getContextClassLoader().getResource("testfile.txt");
        System.out.println(url);
        store = new Store("thesecretpassword".toCharArray(), Paths.get(url.getPath()));
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of method, of class Store.
     *
     * @throws info.lequer.vault.lib.Encryptor.InvalidPasswordException
     * @throws info.lequer.vault.lib.Store.StoreNotFoundException
     */
    @Test
    public void testStore() throws Encryptor.InvalidPasswordException, Store.StoreNotFoundException, Encryptor.InvalidEncryptionException {
        /**
         * open the store and check opening variable
         */
        store.open();
        assertTrue(store.isOpen());
        /**
         * add a key
         */
        store.add("key", "pass");
        assertTrue(store.contains("key"));
        assertFalse(store.contains("wrongkey"));
        PasswordRecord get = store.get("key");
        assertEquals(get.password, "pass");
        /**
         * update the record
         */
        if (store.update("key", "newpassword")) {
            assertEquals(store.get("key").password, "newpassword");
        } else {
            fail("could not update password");
        }
        /**
         * update non existing record
         */
        if (store.update("wrongkey", "newpassword")) {
            fail("update should have return false");

        } else {
            assertFalse(store.update("wrongkey", "newpassword"));
        }

        /**
         * encrypt the store
         */
        store.save();

        /**
         * Reload from file to force decryption and test the content
         */
        Store anotherStore = new Store("thesecretpassword".toCharArray(), Paths.get(url.getPath()));
        anotherStore.open();
        PasswordRecord pr = anotherStore.get("key");
        assertEquals(pr.password, "newpassword");

        /**
         * get(index)
         */
        assertEquals(store.get(0).key, "key");
        /**
         * lets remove the key, the store size should be 0
         */
        if (!store.remove("key")) {
            fail("failed to remove the record");
        }
        assertEquals(store.getAll().size(), 0);
        assertNull(store.get("key"));

    }

}
