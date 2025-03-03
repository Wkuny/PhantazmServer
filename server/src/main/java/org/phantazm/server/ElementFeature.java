package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.util.ElementSearcher;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;

import java.util.Objects;

/**
 * Initializes features related to Element.
 */
public final class ElementFeature {
    public static final String PHANTAZM_PACKAGE = "org.phantazm";

    private static ContextManager contextManager;

    static void initialize(@NotNull MappingProcessorSource mappingProcessorSource, @NotNull KeyParser keyParser) {
        Objects.requireNonNull(mappingProcessorSource);
        Objects.requireNonNull(keyParser);

        contextManager = ContextManager.builder(Namespaces.PHANTAZM).withKeyParserFunction((ignored) -> keyParser)
            .withMappingProcessorSourceSupplier(() -> mappingProcessorSource).build();
        contextManager.registerElementClasses(ElementSearcher.getElementClassesInPackage(PHANTAZM_PACKAGE));
    }

    public static @NotNull ContextManager getContextManager() {
        return FeatureUtils.check(contextManager);
    }
}
