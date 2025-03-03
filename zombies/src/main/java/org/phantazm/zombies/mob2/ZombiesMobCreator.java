package org.phantazm.zombies.mob2;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.proxima.path.Pathfinder;
import com.github.steanky.toolkit.collection.Wrapper;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.MobCreatorBase;
import org.phantazm.mob2.MobData;
import org.phantazm.mob2.goal.GoalApplier;
import org.phantazm.mob2.skill.SkillComponent;
import org.phantazm.proxima.bindings.minestom.InstanceSpawner;
import org.phantazm.proxima.bindings.minestom.Pathfinding;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.map.Window;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class ZombiesMobCreator extends MobCreatorBase {
    public ZombiesMobCreator(@NotNull MobData data, Pathfinding.@NotNull Factory pathfinding,
        @NotNull List<SkillComponent> skills, @NotNull List<GoalApplier> goalAppliers,
        @NotNull Pathfinder pathfinder,
        @NotNull Function<? super Instance, ? extends InstanceSpawner.InstanceSettings> settingsFunction,
        @NotNull Map<EquipmentSlot, ItemStack> equipmentMap, @NotNull Object2FloatMap<String> attributeMap) {
        super(data, pathfinding, skills, goalAppliers, pathfinder, settingsFunction, equipmentMap, attributeMap);
    }

    @Override
    protected void setup(@NotNull Mob mob, @NotNull InjectionStore store) {
        super.setup(mob, store);
        setPathfinding(mob, store);
        setTasks(mob);
    }


    protected void setPathfinding(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        ZombiesScene scene = injectionStore.get(Keys.SCENE);
        BoundedTracker<Window> windowTracker = scene.map().objects().windowTracker();

        mob.pathfinding().setPenalty((x, y, z, h) -> {
            Optional<Window> windowOptional = windowTracker.atPoint(x, y + 1, z);

            //noinspection OptionalIsPresent
            if (windowOptional.isEmpty()) {
                return h;
            }

            return windowOptional.get().isBlockBroken(x, y + 1, z) ? h : h * 10;
        });
    }

    protected void setTasks(@NotNull Mob mob) {
        ConfigNode extraNode = mob.data().extra();

        int ticksUntilDeath = extraNode.getNumberOrDefault(6000, ExtraNodeKeys.TICKS_UNTIL_DEATH).intValue();
        int speedupIncrements = extraNode.getNumberOrDefault(5, ExtraNodeKeys.SPEEDUP_INCREMENTS).intValue();
        int speedupInterval = extraNode.getNumberOrDefault(1200, ExtraNodeKeys.SPEEDUP_INTERVAL).intValue();
        double speedupAmount = extraNode.getNumberOrDefault(0.1D, ExtraNodeKeys.SPEEDUP_AMOUNT).doubleValue();

        if (ticksUntilDeath >= 0) {
            mob.scheduler()
                .scheduleTask(mob::kill, TaskSchedule.tick(ticksUntilDeath), TaskSchedule.stop());
        }

        if (speedupIncrements > 0) {
            Wrapper<Task> taskWrapper = Wrapper.ofNull();
            Task speedupTask = mob.scheduler().scheduleTask(new Runnable() {
                private int times = 0;

                @Override
                public void run() {
                    if (++times == speedupIncrements) {
                        taskWrapper.get().cancel();
                    }

                    UUID modifierUUID = UUID.randomUUID();
                    mob.getAttribute(Attribute.MOVEMENT_SPEED).addModifier(
                        new AttributeModifier(modifierUUID, modifierUUID.toString(), speedupAmount,
                            AttributeOperation.MULTIPLY_BASE));
                }
            }, TaskSchedule.tick(speedupInterval), TaskSchedule.tick(speedupInterval));

            taskWrapper.set(speedupTask);
        }
    }
}
