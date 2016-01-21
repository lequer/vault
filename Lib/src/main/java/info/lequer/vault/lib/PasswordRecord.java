/*
 * 
 * 
 */
package info.lequer.vault.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michel
 */
public class PasswordRecord {
    
    private final Logger LOGGER = LoggerFactory.getLogger(PasswordRecord.class);
    public String key, password, site;
    private final String SEPARATOR = "_____SEP____";
    
    public PasswordRecord(String key, String password) {
        this(key, password, "local");
    }
    
    public PasswordRecord(String key, String password, String site) {
        this.key = key;
        this.password = password;
        this.site = site;
    }
    
    public PasswordRecord(String line) {
        String[] tokens = line.trim().split(SEPARATOR);
        if ((tokens == null) || (tokens.length < 3)) {
            
            throw new IllegalArgumentException(line);
        }
        this.key = tokens[0].trim();
        this.password = tokens[1].trim();
        this.site = tokens[2].trim();
    }
    
    public String asLine() {
        StringBuilder sb = new StringBuilder();
        sb.append(key).append(SEPARATOR).append(password).append(SEPARATOR).append(site); //.append("\n");
        return sb.toString();
    }
}
