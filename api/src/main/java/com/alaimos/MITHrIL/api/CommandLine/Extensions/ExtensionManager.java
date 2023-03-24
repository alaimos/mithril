package com.alaimos.MITHrIL.api.CommandLine.Extensions;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.Types.ExtensionTypeInterface;
import org.pf4j.PluginManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExtensionManager {

    public static final ExtensionManager INSTANCE = new ExtensionManager();
    private static PluginManager defaultPluginManager = null;
    private final Map<String, Map<String, ExtensionInterface>> extensions = new HashMap<>();
    private Map<String, ExtensionTypeInterface> extensionTypes = null;
    private Map<Class<? extends ExtensionInterface>, String> extensionTypeNames = null;

    /**
     * Private constructor for singleton
     */
    private ExtensionManager() {
    }

    /**
     * Set the default plugin manager. This method should be called only once. However, if called more than once, it
     * will be ignored.
     *
     * @param pluginManager the default plugin manager
     */
    public static void setDefaultPluginManager(PluginManager pluginManager) {
        if (defaultPluginManager != null) return;
        defaultPluginManager = pluginManager;
    }

    /**
     * Get the list of extensions supported by this app
     *
     * @return a map of extension types
     */
    public Map<String, ExtensionTypeInterface> getExtensionTypes() {
        init();
        return extensionTypes;
    }

    /**
     * Get the all extensions of a given type
     *
     * @param type the type of extension
     * @return a map of extensions
     */
    public Map<String, ? extends ExtensionInterface> getExtensions(String type) {
        init();
        initByType(type);
        return extensions.get(type);
    }

    /**
     * Get the all extensions of a given type
     *
     * @param type the type of extension
     * @return a map of extensions
     */
    public Map<String, ? extends ExtensionInterface> getExtensions(Class<? extends ExtensionInterface> type) {
        init();
        initByType(extensionTypeNames.get(type));
        return extensions.get(extensionTypeNames.get(type));
    }

    /**
     * Get an extension by its name
     *
     * @param type the type of the extension
     * @param name the name of the extension
     * @param <E>  the java type of the extension
     * @return the extension
     */
    @SuppressWarnings("unchecked")
    public <E extends ExtensionInterface> E getExtension(String type, String name) {
        init();
        initByType(type);
        return (E) extensions.get(type).get(name);
    }

    /**
     * Initialize the extension manager
     */
    private void init() {
        if (extensionTypes != null) return;
        extensionTypes = defaultPluginManager.getExtensions(ExtensionTypeInterface.class)
                                             .stream()
                                             .collect(Collectors.toMap(ExtensionTypeInterface::name,
                                                                       Function.identity()
                                             ));
        extensionTypeNames = extensionTypes.values()
                                           .stream()
                                           .collect(Collectors.toMap(ExtensionTypeInterface::classType,
                                                                     ExtensionTypeInterface::name
                                           ));
    }

    /**
     * Initializes all extensions of a given type
     *
     * @param type the type of extension
     */
    private void initByType(String type) {
        if (extensions.containsKey(type)) return;
        var typeDefinition = extensionTypes.get(type);
        if (typeDefinition == null) return;
        var list = new HashMap<String, ExtensionInterface>();
        for (ExtensionInterface e : defaultPluginManager.getExtensions(typeDefinition.classType())) {
            list.put(e.name(), e);
        }
        extensions.put(typeDefinition.name(), list);
    }

}
