package net.dawson.adorablehamsterpets.integration.iris;

import dev.architectury.platform.Platform;
import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.client.render.HamsterPBRTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Safely manages the integration between Adorable Hamster Pets and Iris Shaders.
 *
 * <p>Iris looks up PBR loaders by the exact class of a texture. The hamster's composite
 * textures are generated in memory ({@link HamsterPBRTexture}), so Iris cannot discover
 * their {@code _n}/{@code _s} maps from resource files the way it does for normal textures.
 * This registers a loader that hands Iris the generated normal and specular maps directly.
 *
 * <p>Same behaviour as the original integration, but resolved through reflection so the mod
 * compiles without an Iris jar and keeps working across Iris builds that ship the stable
 * {@code net.irisshaders.iris.pbr.loader} API (verified against Iris 1.11.4 for 26.2).
 */
public final class IrisIntegration {

    private static final String REGISTRY_CLASS = "net.irisshaders.iris.pbr.loader.PBRTextureLoaderRegistry";
    private static final String LOADER_CLASS = "net.irisshaders.iris.pbr.loader.PBRTextureLoader";
    private static final String CONSUMER_CLASS = "net.irisshaders.iris.pbr.loader.PBRTextureLoader$PBRTextureConsumer";

    private IrisIntegration() {}

    /**
     * Called during client initialization.
     * Safely checks for the Iris mod before attempting to touch any Iris class.
     */
    public static void init() {
        if (!Platform.isModLoaded("iris")) {
            return;
        }
        try {
            registerPbrLoader();
            AdorableHamsterPets.LOGGER.info("[AHP] Successfully integrated with Iris PBR registry.");
        } catch (Throwable t) {
            AdorableHamsterPets.LOGGER.error("[AHP] Failed to integrate with Iris PBR registry.", t);
        }
    }

    private static void registerPbrLoader() throws ReflectiveOperationException {
        ClassLoader cl = IrisIntegration.class.getClassLoader();
        Class<?> registryClass = Class.forName(REGISTRY_CLASS, true, cl);
        Class<?> loaderInterface = Class.forName(LOADER_CLASS, false, cl);
        Class<?> consumerInterface = Class.forName(CONSUMER_CLASS, false, cl);

        Method acceptNormal = consumerInterface.getMethod("acceptNormalTexture", AbstractTexture.class);
        Method acceptSpecular = consumerInterface.getMethod("acceptSpecularTexture", AbstractTexture.class);

        // Equivalent of: new PBRTextureLoader<HamsterPBRTexture>() { load(texture, resourceManager, consumer) {...} }
        InvocationHandler handler = (proxy, method, args) -> {
            switch (method.getName()) {
                case "load" -> {
                    if (args != null && args.length == 3 && args[0] instanceof HamsterPBRTexture texture) {
                        Object consumer = args[2];
                        // Feed the normal and specular maps to Iris when it asks for them
                        if (texture.getNormalTexture() != null) {
                            acceptNormal.invoke(consumer, texture.getNormalTexture());
                        }
                        if (texture.getSpecularTexture() != null) {
                            acceptSpecular.invoke(consumer, texture.getSpecularTexture());
                        }
                    }
                    return null;
                }
                case "hashCode" -> { return System.identityHashCode(proxy); }
                case "equals" -> { return proxy == args[0]; }
                case "toString" -> { return "AHP HamsterPBRTexture loader"; }
                default -> { return null; }
            }
        };
        Object loader = Proxy.newProxyInstance(cl, new Class<?>[]{loaderInterface}, handler);

        Object registry = registryClass.getField("INSTANCE").get(null);
        Method register = registryClass.getMethod("register", Class.class, loaderInterface);
        register.invoke(registry, HamsterPBRTexture.class, loader);
    }
}
