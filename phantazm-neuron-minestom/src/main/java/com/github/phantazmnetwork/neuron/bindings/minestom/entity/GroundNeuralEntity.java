package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.neuron.bindings.minestom.ContextProvider;
import com.github.phantazmnetwork.neuron.engine.PathContext;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import com.github.phantazmnetwork.neuron.node.GroundTranslator;
import com.github.phantazmnetwork.neuron.node.NodeTranslator;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GroundNeuralEntity extends NeuralEntity {
    private final GroundMinestomDescriptor entityType;

    public GroundNeuralEntity(@NotNull GroundMinestomDescriptor entityType, @NotNull UUID uuid,
                              @NotNull ContextProvider contextProvider) {
        super(entityType, uuid, contextProvider);
        this.entityType = entityType;
    }

    @Override
    public @NotNull NodeTranslator makeTranslator(@NotNull Instance instance, @NotNull PathContext context) {
        return new GroundTranslator(context.getCollider(), (GroundMinestomDescriptor) getDescriptor());
    }

    @Override
    public @NotNull Controller makeController() {
        return new EntityController(this, getAttributeValue(Attribute.MOVEMENT_SPEED), entityType.getJumpHeight());
    }
}
