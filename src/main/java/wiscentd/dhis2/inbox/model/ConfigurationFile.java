package wiscentd.dhis2.inbox.model;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationFile {
    private static Logger logger = Logger.getLogger(ConfigurationFile.class.getName());
    private Properties properties = new Properties();

    public ConfigurationFile(String name) {
        try {
            // Get BASE_PATH relative to CATALINA_BASE
            File configDir = new File(System.getProperty("catalina.base"), "wiscentd");
            // Try to get configuration file from $CATALINA_BASE/config/name.properties
            // If it fails default to resource on %CLASSPATH%/config/name.properties
            File configurationFile = new File(configDir + File.separator + "config" + File.separator +
                    name + ".properties");
            InputStream is = configurationFile.exists() ? new FileInputStream(configurationFile) :
                    getClass().getClassLoader().getResourceAsStream("config/" + name + ".properties");
            properties.load(is);
            is.close();
        } catch (IOException e) {
            logger.error("Error loading property file: " + name, e);
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
