package org.phantazm.zombies.powerup.action.component;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.action.InstantAction;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.Optional;

@Model("zombies.powerup.action.heal")
@Cache(false)
public class HealAction implements PowerupActionComponent {
    private final Data data;

    @FactoryMethod
    public HealAction(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PowerupAction apply(@NotNull ZombiesScene scene) {
        return new Action(data);
    }

    @DataObject
    public record Data(float amount) {
    }

    private static class Action extends InstantAction {
        private final Data data;

        private Action(Data data) {
            this.data = data;
        }

        @Override
        public boolean activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer zombiesPlayer, long time) {
            if (!zombiesPlayer.canDoGenericActions()) {
                return false;
            }

            Optional<Player> playerOptional = zombiesPlayer.getPlayer();
            if (playerOptional.isEmpty()) {
                return false;
            }

            Player player = playerOptional.get();
            if (player.getHealth() == player.getMaxHealth()) {
                return false;
            }

            player.setHealth(player.getHealth() + data.amount);
            return true;
        }
    }
}
