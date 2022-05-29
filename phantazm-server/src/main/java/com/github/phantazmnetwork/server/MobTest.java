package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.api.chat.ChatChannelSendEvent;
import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.goal.FollowPlayerGoal;
import com.github.phantazmnetwork.mob.goal.UseSkillGoal;
import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.mob.skill.PlaySoundSkill;
import com.github.phantazmnetwork.mob.target.NearestEntitySelector;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.GroundMinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.Spawner;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.GoalGroup;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

final class MobTest {

    private MobTest() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull EventNode<Event> phantazm, @NotNull Spawner spawner) {
        TargetSelector<Iterable<Player>> underlyingSelector = new NearestEntitySelector<>(100.0, 1) {
            @Override
            protected @NotNull Optional<Player> mapTarget(@NotNull Entity entity) {
                if (entity instanceof Player targetPlayer) {
                    return Optional.of(targetPlayer);
                }

                return Optional.empty();
            }

            @Override
            protected boolean isTargetValid(@NotNull PhantazmMob mob, @NotNull Entity targetEntity,
                                            @NotNull Player target) {
                return true;
            }
        };
        TargetSelector<Player> playerSelector = targetingMob -> {
            Optional<Iterable<Player>> targetOptional = underlyingSelector.selectTarget(targetingMob);
            if (targetOptional.isPresent()) {
                Iterator<Player> target = targetOptional.get().iterator();
                if (target.hasNext()) {
                    return Optional.of(target.next());
                }
            }

            return Optional.empty();
        };

        String defaultSkillName = "testSkill";
        MobModel model = new MobModel(
                GroundMinestomDescriptor.of(EntityType.ZOMBIE, "test"),
                Map.of(defaultSkillName, new PlaySoundSkill(playerSelector, Sound.sound(
                        Key.key("entity.elder_guardian.curse"),
                        Sound.Source.MASTER,
                        1.0F,
                        1.0F
                ), true)),
                Component.text("Test Mob"),
                Map.of(
                        EquipmentSlot.CHESTPLATE, ItemStack.of(Material.LEATHER_CHESTPLATE)
                )
        );

        AtomicReference<PhantazmMob> mobReference = new AtomicReference<>();
        phantazm.addListener(ChatChannelSendEvent.class, event -> {
            String msg = event.getInput();
            Player player = event.getPlayer();
            Instance instance = player.getInstance();

            if (instance != null) {
                switch (msg) {
                    case "spawnmob":
                        PhantazmMob mob = model.spawn(spawner, instance, player.getPosition());
                        addGoalGroups(mob, playerSelector, mob.model().getSkills().get(defaultSkillName));
                        mobReference.set(mob);
                }
            }
        });
    }

    private static void addGoalGroups(@NotNull PhantazmMob mob, @NotNull TargetSelector<Player> playerSelector,
                                      @NotNull Skill skill) {
        NeuralGoal followPlayerGoal = new FollowPlayerGoal(mob, playerSelector);
        NeuralGoal useSkillGoal = new UseSkillGoal(mob, skill, 5000L);

        mob.entity().addGoalGroup(new GoalGroup(Collections.singleton(followPlayerGoal)));
        mob.entity().addGoalGroup(new GoalGroup(Collections.singleton(useSkillGoal)));
    }

}
