package no.feide.moria.directory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import no.feide.moria.directory.DirectoryManagerConfigurationException;
import no.feide.moria.log.MessageLogger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Configuration handler for the directory manager.
 */
public class DirectoryManagerConfiguration {

    /** Internal representation of the index class. */
    private Class indexClass;

    /** Internal representation of the backend class. */
    private Class backendFactoryClass;

    /** The message logger. */
    private MessageLogger log = new MessageLogger(DirectoryManagerConfiguration.class);


    /**
     * Constructor. Creates a new configuration object and reads the
     * configuration file(s).
     * @param config
     *            The Directory Manager configuration passed on from
     *            <code>DirectoryManager.setConfig(Properties)</code>. Must
     *            include the property <code>directoryConfiguration</code>.
     * @throws DirectoryManagerConfigurationException
     *             If the configuration file(s) cannot be properly processed.
     */
    public DirectoryManagerConfiguration(Properties config)
    throws DirectoryManagerConfigurationException {

        // Sanity check.
        if (config == null) {
            log.logCritical("Configuration properties cannot be NULL");
            throw new IllegalArgumentException("Configuration properties cannot be NULL");
        }

        // Preparing to read configuration from file.
        String configFile = (String) config.get("directoryConfiguration");
        if (configFile == null || configFile.equals("")) {
            log.logCritical("Missing basic directory configuration (directoryConfiguration not set)");
            throw new DirectoryManagerConfigurationException("Missing basic directory configuration (directoryConfiguration not set)");
        }

        // Read index (not the index files themselves, mind you) and backend
        // configuration.
        SAXBuilder builder = new SAXBuilder();
        try {
            Element rootElement = builder.build(new File(configFile)).getRootElement();
            parseIndexConfig(rootElement);
            parseBackendConfig(rootElement);
        } catch (IOException e) {
            log.logCritical("Unable to read from configuration file", e);
            throw new DirectoryManagerConfigurationException("Unable to read from configuration file", e);
        } catch (JDOMException e) {
            log.logCritical("Unable to parse configuration file", e);
            throw new DirectoryManagerConfigurationException("Unable to parse configuration file", e);
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
    private Element getUniqueElement(Element rootElement, String name)
    throws DirectoryManagerConfigurationException {

        // Get the element, with sanity checks.
        List elements = rootElement.getChildren(name);
        if (elements.size() != 1) {
            log.logCritical('\"' + name + " element not unique in configuration file");
            throw new DirectoryManagerConfigurationException('\"' + name + "\" element not unique in configuration file");
        }
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
    private void parseIndexConfig(Element rootElement)
    throws DirectoryManagerConfigurationException {

        // Sanity check.
        if (rootElement == null) {
            log.logCritical("Missing root element in configuration file");
            throw new IllegalArgumentException("Missing root element in configuration file");
        }

        // Get the index element, with sanity checks.
        Element indexElement = getUniqueElement(rootElement, "Index");
        HashMap indexConfig = new HashMap();

        // Get index class, with sanity checks.
        Attribute a = getUniqueElement(indexElement, "Class").getAttribute("name");
        if ((a == null) || (a.getValue() == null) || (a.getValue() == "")) {
            log.logCritical("Index class not set in configuration file");
            throw new DirectoryManagerConfigurationException("Index class not set in configuration file");
        }
        try {
            indexClass = Class.forName(a.getValue());
        } catch (ClassNotFoundException e) {
            log.logCritical("Index class " + a.getValue() + " not found", e);
            throw new DirectoryManagerConfigurationException("Index class " + a.getValue() + " not found", e);
        }

    }


    /**
     * Get the index class implementation.
     * @return The index class.
     */
    public Class getIndexClass() {

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
    private void parseBackendConfig(Element rootElement)
    throws DirectoryManagerConfigurationException {

        // Sanity check.
        if (rootElement == null) {
            log.logCritical("Missing root element in configuration file");
            throw new IllegalArgumentException("Missing root element in configuration file");
        }

        // Get the backend element, with sanity checks.
        Element backendElement = getUniqueElement(rootElement, "Backend");
        HashMap backendConfig = new HashMap();

        // Get backend class, with sanity checks.
        Attribute a = getUniqueElement(backendElement, "Class").getAttribute("name");
        if ((a == null) || (a.getValue() == null) || (a.getValue() == "")) {
            log.logCritical("Backend class not set in configuration file");
            throw new DirectoryManagerConfigurationException("Backend class not set in configuration file");
        }
        try {
            backendFactoryClass = Class.forName(a.getValue());
        } catch (ClassNotFoundException e) {
            log.logCritical("Backend factory class " + a.getValue() + " not found", e);
            throw new DirectoryManagerConfigurationException("Backend factory class " + a.getValue() + " not found", e);
        }

    }


    /**
     * Get the backend factory class implementation.
     * @return The backend factory class.
     */
    public Class getBackendFactoryClass() {

        return backendFactoryClass;

    }

}