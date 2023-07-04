package org.phantazm.zombies.equipment.perk.effect.shot;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.document.Description;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.Tags;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Deque;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

@Description("""
        An entity action that sets an entity on fire for a configurable amount of time, health, and damage interval.
        """)
@Model("zombies.perk.effect.shot_entity.apply_fire")
@Cache(false)
public class ApplyFireShotEffect implements ShotEffect, Tickable {
    private final Data data;
    private final Tag<Long> lastDamageTime;
    private final Deque<DamageTarget> activeEntities;
    private final MobStore mobStore;

    private record DamageTarget(UUID damager, LivingEntity target) {
    }

    @FactoryMethod
    public ApplyFireShotEffect(@NotNull Data data, @NotNull MobStore mobStore) {
        this.data = Objects.requireNonNull(data, "data");

        UUID uuid = UUID.randomUUID();
        this.lastDamageTime = Tag.Long("last_fire_damage_time_" + uuid).defaultValue(-1L);

        this.activeEntities = new ConcurrentLinkedDeque<>();
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
    }

    @Override
    public void perform(@NotNull Entity entity, @NotNull ZombiesPlayer zombiesPlayer) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            //can't set non-LivingEntity on fire as they have no health
            return;
        }

        PhantazmMob mob = mobStore.getMob(entity.getUuid());
        if (mob != null && mob.model().getExtraNode().getBooleanOrDefault(false, ExtraNodeKeys.RESIST_FIRE)) {
            return;
        }

        livingEntity.setFireForDuration(data.fireTicks);

        boolean alreadyActive = entity.getTag(lastDamageTime) != -1;
        entity.setTag(lastDamageTime, System.currentTimeMillis());

        if (!alreadyActive) {
            activeEntities.add(new DamageTarget(zombiesPlayer.getUUID(), livingEntity));
        }
    }

    @Override
    public void tick(long time) {
        activeEntities.removeIf(target -> {
            LivingEntity entity = target.target;

            if (entity.isRemoved() || entity.isDead() || !entity.isOnFire()) {
                stopFire(entity);
                return true;
            }

            long lastDamageTime = entity.getTag(this.lastDamageTime);
            if ((time - lastDamageTime) / MinecraftServer.TICK_MS >= data.damageInterval) {
                doDamage(entity, target.damager);
                entity.setTag(this.lastDamageTime, time);
            }

            return false;
        });
    }

    private void doDamage(LivingEntity entity, UUID damager) {
        Damage damage = new Damage(DamageType.ON_FIRE, null, null, null, data.damage);
        damage.setTag(Tags.LAST_HIT_BY, damager);

        entity.damage(damage, data.bypassArmor);
    }

    private void stopFire(Entity entity) {
        entity.removeTag(lastDamageTime);
    }

    @DataObject
    public record Data(@Description("The number of ticks the hit entity will be set on fire") int fireTicks,
                       @Description("The number of ticks between fire damage applications") int damageInterval,
                       @Description("The amount of damage dealt on each application") float damage,
                       @Description("Whether fire damage should bypass armor damage reduction") boolean bypassArmor) {
    }
}
