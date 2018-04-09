package wiscentd.dhis2.inbox.model;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationFile {
    private static Logger logger = Logger.getLogger(ConfigurationFile.class.getName());
    private Properties properties = new Properties();

    public ConfigurationFile(String path) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(path);
            properties.load(is);
            is.close();
        } catch (IOException e) {
            logger.error("Error loading property file: " + path, e);
        }
    }

    public String getProperty(String key) {
        try {
            return properties.getProperty(key);
        } catch (Exception e) {
            logger.error("No property defined with this key: " + key, e);
        }
        return null;
    }
}
