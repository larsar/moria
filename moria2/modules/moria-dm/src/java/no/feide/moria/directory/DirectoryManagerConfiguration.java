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

    /** Internal representation of the index class. */
    private static Class indexClass;

    /** Internal representation of the backend class. */
    private static Class backendFactoryClass;

    
    /**
     * @param config
     * @throws DirectoryManagerConfigurationException
     */
    protected static void read(Properties config)
    throws DirectoryManagerConfigurationException {

        // Sanity check.
        if (config == null)
            DirectoryManager.error("Parameter cannot be NULL", null);

        // Preparing to read configuration from file.
        String configFile = (String) config.get("directoryConfiguration");
        if (configFile == null || configFile.equals(""))
            DirectoryManager.error("Missing basic directory configuration (directoryConfiguration not set)", null);

        // Read index (not the index files themselves, mind you) and backend
        // configuration.
        SAXBuilder builder = new SAXBuilder();
        try {
            Element rootElement = builder.build(new File(configFile)).getRootElement();
            parseIndexConfig(rootElement);
            parseBackendConfig(rootElement);
        } catch (IOException e) {
            DirectoryManager.error("Unable to read from configuration file", e);
        } catch (JDOMException e) {
            DirectoryManager.error("Unable to parse configuration file", e);
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
            DirectoryManager.error(name + " element not unique in configuration file", null);
        elements = null; // Cleanup.

        return rootElement.getChild(name);

    }


    /**
     * Parse the subsection of the configuration file related to the index and
     * update the configuration.
     * @param rootElement
     *            The root configuration element.
     * @throws DirectoryManagerConfigurationException
     *             If the root element is missing, or if the section of the
     *             configuration file relating to the index cannot be parsed as
     *             expected.
     */
    private static void parseIndexConfig(Element rootElement)
    throws DirectoryManagerConfigurationException {

        // Sanity check.
        if (rootElement == null)
            DirectoryManager.error("Missing root element in configuration file", null);

        // Get the index element, with sanity checks.
        Element indexElement = getUniqueElement(rootElement, "Index");
        HashMap indexConfig = new HashMap();

        // Get index class, with sanity checks.
        Attribute a = getUniqueElement(indexElement, "Class").getAttribute("name");
        if ((a == null) || (a.getValue() == null) || (a.getValue() == ""))
            DirectoryManager.error("Index class not set in configuration file", null);
        try {
            indexClass = Class.forName(a.getValue());
        } catch (ClassNotFoundException e) {
            DirectoryManager.error("Index class " + a.getValue() + " not found", e);
        }

    }


    /**
     * Get the index class implementation.
     * @return The index class.
     */
    public static Class getIndexClass() {

        return indexClass;

    }


    /**
     * Parse the subsection of the configuration file related to the backend and
     * update the configuration.
     * @param rootElement
     *            The root configuration element.
     * @throws DirectoryManagerConfigurationException
     *             If the root element is missing, or if the section of the
     *             configuration file relating to the backend cannot be parsed
     *             as expected.
     */
    private static void parseBackendConfig(Element rootElement)
    throws DirectoryManagerConfigurationException {

        // Sanity check.
        if (rootElement == null)
            DirectoryManager.error("Missing root element in configuration file", null);

        // Get the backend element, with sanity checks.
        Element backendElement = getUniqueElement(rootElement, "Backend");
        HashMap backendConfig = new HashMap();

        // Get backend class, with sanity checks.
        Attribute a = getUniqueElement(backendElement, "Class").getAttribute("name");
        if ((a == null) || (a.getValue() == null) || (a.getValue() == ""))
            DirectoryManager.error("Backend class not set in configuration file", null);
        try {
            backendFactoryClass = Class.forName(a.getValue());
        } catch (ClassNotFoundException e) {
            DirectoryManager.error("Backend factory class " + a.getValue() + " not found", e);
        }

    }
    
    
    /**
     * Get the backend factory class implementation.
     * @return The backend factory class.
     */
    public static Class getBackendFactoryClass() {

        return backendFactoryClass;

    }

}