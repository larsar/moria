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
 * Configuration handler for the Directory Manager. Parses the configuration
 * file.
 */
public class DirectoryManagerConfiguration {

    /** The message logger. */
    private final MessageLogger log = new MessageLogger(DirectoryManagerConfiguration.class);

    /** The location of the index file. */
    private String indexFilename;

    /** The index update frequency, in milliseconds. */
    private long indexUpdateFrequency;

    /** Internal representation of the backend class. */
    private Class backendFactoryClass;

    /**
     * The required configuration file property, for external reference.
     * Currently contains the value
     * <code>no.feide.moria.directory.configuration</code>.
     */
    public final static String CONFIGURATION_PROPERTY = "no.feide.moria.directory.configuration";


    /**
     * Constructor. Creates a new configuration object and reads the Directory
     * Manager configuration file. <br>
     * <br>
     * Note that the actual parsing of the configuration file is done by
     * <code>parseIndexConfig(Element)</code> and
     * <code>parseBackendConfig(Element)</code>.
     * @param config
     *            The Directory Manager configuration passed on from
     *            <code>DirectoryManager.setConfig(Properties)</code>. Must
     *            include the property given by
     *            <code>DirectoryManagerConfiguration.CONFIGURATION_PROPERTY</code>.
     * @throws IllegalArgumentException
     *             If <code>config</code> is <code>null</code>, or if the
     *             property given by
     *             <code>DirectoryManagerConfiguration.CONFIGURATION_PROPERTY</code>
     *             is not set or is an empty string. Also thrown if unable to
     *             read from or parse the configuration file.
     * @see DirectoryManager#setConfig(Properties)
     * @see #CONFIGURATION_PROPERTY
     * @see #parseBackendConfig(Element)
     * @see #parseIndexConfig(Element)
     */
    public DirectoryManagerConfiguration(final Properties config) {

        // Sanity check.
        if (config == null)
            throw new IllegalArgumentException("Configuration properties cannot be NULL");

        // Preparing to read configuration from file.
        final String configFile = (String) config.get(CONFIGURATION_PROPERTY);
        if (configFile == null || configFile.equals(""))
            throw new DirectoryManagerConfigurationException("Property " + DirectoryManagerConfiguration.CONFIGURATION_PROPERTY + " not set)");

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
     * Used by <code>parseBackendConfig(Element)</code> and
     * <code>parseIndexConfig(Element)</code> to make sure the configuration
     * file is non-ambiguous.
     * @param rootElement
     *            The root element of the configuration, as parsed from file.
     *            Cannot be <code>null</code>.
     * @param name
     *            The child element's name. Cannot be <code>null</code>.
     * @return The child element itself, as per the
     *         <code>org.jdom.Element.getChild(String)</code> method.
     * @throws IllegalArgumentException
     *             If <code>rootElement</code> or <code>name</code> is
     *             <code>null</code>.
     * @throws DirectoryManagerConfigurationException
     *             If more than one child node with the given name was found.
     * @see #parseBackendConfig(Element)
     * @see #parseIndexConfig(Element)
     * @see org.jdom.Element#getChild(java.lang.String)
     */
    private Element getUniqueElement(final Element rootElement, final String name)
    throws DirectoryManagerConfigurationException {

        // Sanity checks.
        if (rootElement == null)
            throw new IllegalArgumentException("Root element cannot be NULL");
        if (name == null)
            throw new IllegalArgumentException("Element name cannot be NULL");

        // Get any child elements matching the given name.
        List elements = rootElement.getChildren(name);
        if (elements.size() != 1) {

            // The element was not unique.
            throw new DirectoryManagerConfigurationException('\"' + name + "\" element not unique in configuration file");

        }

        // Return the element.
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

        // Get index filename, with sanity checks.
        Attribute a = indexElement.getAttribute("file");
        if ((a == null) || (a.getValue() == null) || (a.getValue() == ""))
            throw new DirectoryManagerConfigurationException("Index file not set in configuration file");
        indexFilename = a.getValue();

        // Get index update frequency, with sanity checks.
        a = indexElement.getAttribute("update");
        if ((a == null) || (a.getValue() == null) || (a.getValue() == ""))
            throw new DirectoryManagerConfigurationException("Index update frequency not set in configuration file");
        indexUpdateFrequency = 1000 * Integer.parseInt(a.getValue());
        if (indexUpdateFrequency <= 0)
            throw new DirectoryManagerConfigurationException("Index update frequency must be greater than zero");

    }


    /**
     * Get the serialized index file name.
     * @return The index file name.
     */
    public String getIndexFilename() {

        return indexFilename;

    }


    /**
     * Get the index update frequency.
     * @return The index update frequency, in milliseconds.
     */
    public long getIndexUpdateFrequency() {

        return indexUpdateFrequency;

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