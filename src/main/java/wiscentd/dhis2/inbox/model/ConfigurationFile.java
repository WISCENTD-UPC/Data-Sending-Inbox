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
            // Get BASE_PATH, we expect $WISCENTD_HOME to be initialized
            // If it's not we use CATALINA_HOME to default root
            String WISCENTD_HOME = System.getenv("WISCENTD_HOME");
            if (WISCENTD_HOME == null) WISCENTD_HOME = System.getenv("CATALINA_HOME");

            // Try to get configuration file from $WISCENTD_HOME/conf/name.properties
            // If it fails default to resource on %CLASSPATH%/config/name.properties
            File configurationFile = new File(WISCENTD_HOME + File.separator + "conf" + File.separator +
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
