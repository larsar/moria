package no.feide.moria.directory;

//import no.feide.moria.log.MessageLogger;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Configuration handler for the directory manager.
 */
public class DirectoryManagerConfiguration {

    /** Internal representation of the index configuration. */
    private static HashMap indexConfig;
    
    /** Internal representation of the backend configuration. */
    private static HashMap backendConfig;


    private static void error(String message, Throwable cause)
    throws DirectoryManagerConfigurationException {

        // Set up logging.
        // TODO: Make sure it works.
        //MessageLogger messageLog = new
        // MessageLogger(DirectoryManagerConfiguration.class);

        // TODO: Differ between critical and warning depending on existing
        // configuration.
        //messageLog.logCritical(message);
        throw new DirectoryManagerConfigurationException(message, cause);

    }


    /**
     * 
     * @param config
     * @throws DirectoryManagerConfigurationException
     */
    protected static void read(Properties config)
    throws DirectoryManagerConfigurationException {

        // Sanity check.
        if (config == null)
            error("Parameter cannot be NULL", null);

        // Preparing to read configuration from file.
        String configFile = (String) config.get("directoryConfiguration");
        if (configFile == null || configFile.equals(""))
            error("Missing basic directory configuration (directoryConfiguration not set)", null);

        // Read index (not the index files themselves, mind you) and backend
        // configuration.
        SAXBuilder builder = new SAXBuilder();
        try {
            Element rootElement = builder.build(new File(configFile)).getRootElement(); 
            HashMap indexConfig = parseIndexConfig(rootElement);
            HashMap backendConfig = parseBackendConfig(rootElement);
            System.err.println(indexConfig.toString());
            System.err.println(backendConfig.toString());
        } catch (IOException e) {
            error("Unable to read from configuration file", e);
        } catch (JDOMException e) {
            error("Unable to parse configuration file", e);
        }

    }


    /**
     * Look up a given child element from a root element, and make sure the
     * child element is unique (that is, there is one and only one existence).
     * @param rootElement
     *            The root element.
     * @param name
     *            The child element's name.
     * @return The child element itself.
     * @throws DirectoryManagerConfigurationException
     *             If the given element cannot be found, or if it is found more
     *             than once.
     */
    private static Element getUniqueElement(Element rootElement, String name)
    throws DirectoryManagerConfigurationException {

        // Get the element, with sanity checks.
        List elements = rootElement.getChildren(name);
        if (elements.size() != 1)
            error(name + " element not unique in configuration file", null);
        elements = null; // Cleanup.

        return rootElement.getChild(name);

    }


    /**
     * Parse the subsection of the configuration file related to the index.
     * @param rootElement
     *            The root configuration element.
     * @return The index configuration, presented as a hash map.
     * @throws DirectoryManagerConfigurationException
     *             If the root element is missing, or if the section of the
     *             configuration file relating to the index cannot be parsed as
     *             expected.
     */
    private static HashMap parseIndexConfig(Element rootElement)
    throws DirectoryManagerConfigurationException {

        // Sanity check.
        if (rootElement == null)
            error("Missing root element in configuration file", null);

        // Get the index element, with sanity checks.
        Element indexElement = getUniqueElement(rootElement, "Index");
        HashMap indexConfig = new HashMap();

        // Get index class, with sanity checks.
        Attribute a = getUniqueElement(indexElement, "Class").getAttribute("name");
        if ((a == null) || (a.getValue() == null) || (a.getValue() == ""))
            error("Index class not set in configuration file", null);
        indexConfig.put("indexClass", a.getValue());

        return indexConfig;

    }


    /**
     * Parse the subsection of the configuration file related to the backend.
     * @param rootElement
     *            The root configuration element.
     * @return The backend configuration, presented as a hash map.
     * @throws DirectoryManagerConfigurationException
     *             If the root element is missing, or if the section of the
     *             configuration file relating to the backend cannot be parsed
     *             as expected.
     */
    private static HashMap parseBackendConfig(Element rootElement)
    throws DirectoryManagerConfigurationException {

        // Sanity check.
        if (rootElement == null)
            error("Missing root element in configuration file", null);

        // Get the backend element, with sanity checks.
        Element backendElement = getUniqueElement(rootElement, "Backend");
        HashMap backendConfig = new HashMap();

        // Get backend class, with sanity checks.
        Attribute a = getUniqueElement(backendElement, "Class").getAttribute("name");
        if ((a == null) || (a.getValue() == null) || (a.getValue() == ""))
            error("Backend class not set in configuration file", null);
        backendConfig.put("backendClass", a.getValue());

        return backendConfig;

    }

}