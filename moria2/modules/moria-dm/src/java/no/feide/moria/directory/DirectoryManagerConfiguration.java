package no.feide.moria.directory;

import no.feide.moria.log.MessageLogger;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Configuration handler for the directory manager.
 */
public class DirectoryManagerConfiguration {

    private static String s;


    private static void error(String message, Throwable cause)
    throws DirectoryManagerConfigurationException {

        // Set up logging.
        MessageLogger messageLog = new MessageLogger(DirectoryManagerConfiguration.class);

        // TODO: Differ between critical and warning depending on existing
        // configuration.
        messageLog.logCritical(message);
        throw new DirectoryManagerConfigurationException(message, cause);

    }


    protected static void read(Properties config)
    throws DirectoryManagerConfigurationException {

        // Sanity check.
        if (config == null)
            error("Parameter cannot be NULL", null);

        // Preparing to read base configuration from file.
        String configFile = (String) config.get("directoryConfiguration");
        if (configFile == null || configFile.equals(""))
            error("Missing basic directory configuration (directoryConfiguration not set)", null);

        // Read base configuration.
        SAXBuilder builder = new SAXBuilder();
        try {
            Document document = builder.build(new File(configFile));
            HashMap baseConfig = parseBaseConfig(document.getRootElement());
        } catch (IOException e) {
            error("Unable to read from base configuration file", e);
        } catch (JDOMException e) {
            error("Unable to parse base configuration file", e);
        }

    }


    private static HashMap parseBaseConfig(Element element)
    throws DirectoryManagerConfigurationException {

        // Sanity check.
        if (element == null)
            error("Missing root element in base configuration file", null);

        HashMap baseConfig = new HashMap();
        s = element.getAttributeValue("IndexClass");
        System.err.println(element.getAttributes().toString());
        return baseConfig;

    }


    public static String getIndexClass() {

        return s;

    }
}