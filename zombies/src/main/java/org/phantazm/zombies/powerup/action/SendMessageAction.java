package org.phantazm.zombies.powerup.action;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;

import java.util.Objects;
import java.util.function.Supplier;

@Model("zombies.powerup.action.send_message")
public class SendMessageAction implements Supplier<PowerupAction> {
    private final Data data;
    private final Instance instance;

    @FactoryMethod
    public SendMessageAction(@NotNull Data data, @NotNull Instance instance) {
        this.data = Objects.requireNonNull(data, "data");
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    @Override
    public PowerupAction get() {
        return new Action(data, instance);
    }

    @DataObject
    public record Data(@NotNull Component message, boolean broadcast) {
    }

    private static class Action extends InstantAction {
        private final Data data;
        private final Instance instance;

        private Action(Data data, Instance instance) {
            this.data = data;
            this.instance = instance;
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            if (data.broadcast) {
                instance.sendMessage(data.message);
            }
            else {
                player.getPlayer().ifPresent(p -> p.sendMessage(data.message));
            }
        }
    }
}
