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
    private final MessageLogger log = new MessageLogger(DirectoryManagerConfiguration.class);


    /**
     * Constructor. Creates a new configuration object and reads the
     * configuration file(s).
     * @param config
     *            The Directory Manager configuration passed on from
     *            <code>DirectoryManager.setConfig(Properties)</code>. Must
     *            include the property <code>no.feide.moria.directory.configuration</code>.
     */
    public DirectoryManagerConfiguration(final Properties config) {

        // Sanity check.
        if (config == null)
            throw new IllegalArgumentException("Configuration properties cannot be NULL");

        // Preparing to read configuration from file.
        final String configFile = (String) config.get("no.feide.moria.directory.configuration");
        if (configFile == null || configFile.equals(""))
            throw new DirectoryManagerConfigurationException("Property no.feide.moria.directory.configuration not set)");

        // Read index (not the index files themselves, mind you) and backend
        // configuration.
        Element rootElement = null;
        try {
            rootElement = (new SAXBuilder()).build(new File(configFile)).getRootElement();
        } catch (IOException e) {
            throw new DirectoryManagerConfigurationException("Unable to read from configuration file", e);
        } catch (JDOMException e) {
            throw new DirectoryManagerConfigurationException("Unable to parse configuration file", e);
        }
        parseIndexConfig(rootElement);
        parseBackendConfig(rootElement);

    }


    /**
     * Look up a given child element from a root element, and make sure the
     * child element is unique (that is, there is one and only one existence).
     * @param rootElement
     *            The root element. Cannot be <code>null</code>.
     * @param name
     *            The child element's name. Cannot be <code>null</code>.
     * @return The child element itself.
     */
    private Element getUniqueElement(final Element rootElement, final String name)
    throws DirectoryManagerConfigurationException {

        // Sanity checks.
        if (rootElement == null)
            throw new IllegalArgumentException("Root element cannot be NULL");
        if (name == null)
            throw new IllegalArgumentException("Element name cannot be NULL");

        // Get the element, with sanity checks.
        List elements = rootElement.getChildren(name);
        if (elements.size() != 1) { throw new DirectoryManagerConfigurationException('\"' + name + "\" element not unique in configuration file"); }
        elements = null; // Cleanup.

        return rootElement.getChild(name);

    }


    /**
     * Parse the subsection of the configuration file related to the index and
     * update the configuration.
     * @param rootElement
     *            The root configuration element. Cannot be <code>null</code>.
     */
    private void parseIndexConfig(final Element rootElement) {

        // Sanity check.
        if (rootElement == null)
            throw new IllegalArgumentException("Missing root element in configuration file");

        // Get the index element, with sanity checks.
        final Element indexElement = getUniqueElement(rootElement, "Index");
        HashMap indexConfig = new HashMap();

        // Get index class, with sanity checks.
        final Attribute a = getUniqueElement(indexElement, "Class").getAttribute("name");
        if ((a == null) || (a.getValue() == null) || (a.getValue() == "")) {
            throw new DirectoryManagerConfigurationException("Index class not set in configuration file");
        }
        try {
            indexClass = Class.forName(a.getValue());
        } catch (ClassNotFoundException e) {
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
     *            The root configuration element. Cannot be <code>null</code>.
     */
    private void parseBackendConfig(final Element rootElement) {

        // Sanity check.
        if (rootElement == null)
            throw new IllegalArgumentException("Missing root element in configuration file");

        // Get the backend element, with sanity checks.
        final Element backendElement = getUniqueElement(rootElement, "Backend");
        HashMap backendConfig = new HashMap();

        // Get backend class, with sanity checks.
        final Attribute a = getUniqueElement(backendElement, "Class").getAttribute("name");
        if ((a == null) || (a.getValue() == null) || (a.getValue() == ""))
            throw new DirectoryManagerConfigurationException("Backend class not set in configuration file");
        try {
            backendFactoryClass = Class.forName(a.getValue());
        } catch (ClassNotFoundException e) {
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