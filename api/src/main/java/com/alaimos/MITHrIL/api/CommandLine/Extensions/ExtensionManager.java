package com.alaimos.MITHrIL.api.CommandLine.Extensions;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.Types.ExtensionTypeInterface;
import org.jetbrains.annotations.NotNull;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
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
     * Return the class loaders of all plugins
     *
     * @return an array of class loaders
     */
    public static ClassLoader @NotNull [] getClassLoaders() {
        return defaultPluginManager.getPlugins()
                                   .stream()
                                   .map(PluginWrapper::getPluginClassLoader)
                                   .toArray(ClassLoader[]::new);
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
     * Get an extension by its name
     *
     * @param type the type of the extension as a java class object
     * @param name the name of the extension
     * @param <E>  the java type of the extension
     * @return the extension
     */
    @SuppressWarnings("unchecked")
    public <E extends ExtensionInterface> E getExtension(Class<E> type, String name) {
        init();
        initByType(extensionTypeNames.get(type));
        return (E) extensions.get(extensionTypeNames.get(type)).get(name);
    }

    /**
     * Given an extension type and name, return a supplier that will create a new instance of the extension class. This
     * is useful when you want to create many instances of the same extension. This method might not work if the
     * extension class does not have a default constructor.
     *
     * @param type the type of the extension
     * @param name the name of the extension
     * @param <E>  the java type of the extension
     * @return the supplier
     */
    @SuppressWarnings("unchecked")
    public <E extends ExtensionInterface> Supplier<E> getExtensionSupplier(Class<E> type, String name) {
        init();
        initByType(extensionTypeNames.get(type));
        try {
            var baseClass = (Class<E>) extensions.get(extensionTypeNames.get(type)).get(name).getClass();
            var constructor = baseClass.getDeclaredConstructor();
            return () -> {
                try {
                    return constructor.newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initialize the extension manager
     */
    private void init() {
        if (extensionTypes != null) return;
        extensionTypes     = defaultPluginManager.getExtensions(ExtensionTypeInterface.class)
                                                 .stream()
                                                 .collect(Collectors.toMap(
                                                         ExtensionTypeInterface::name,
                                                         Function.identity()
                                                 ));
        extensionTypeNames = extensionTypes.values()
                                           .stream()
                                           .collect(Collectors.toMap(
                                                   ExtensionTypeInterface::classType,
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
