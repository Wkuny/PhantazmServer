package org.phantazm.server.role;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface RoleCreator extends Supplier<@NotNull Role> {

}
