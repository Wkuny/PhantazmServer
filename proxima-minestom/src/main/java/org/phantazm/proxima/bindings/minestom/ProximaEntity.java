package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.proxima.Navigator;
import com.github.steanky.proxima.node.Node;
import com.github.steanky.proxima.path.PathResult;
import com.github.steanky.proxima.path.PathTarget;
import com.github.steanky.vector.Vec3D;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.MathUtils;
import org.phantazm.core.VecUtils;
import org.phantazm.proxima.bindings.minestom.controller.Controller;
import org.phantazm.proxima.bindings.minestom.goal.GoalGroup;

import java.time.Duration;
import java.util.*;

/**
 * An entity with navigation capabilities based on the Proxima library.
 */
public class ProximaEntity extends LivingEntity {
    private static final double NODE_REACH_DISTANCE_SQ = 0.2;
    private static final double NODE_DEVIATION_DISTANCE_SQ = 2.5;
    private static final double ENTITY_LOOK_DISTANCE_SQ = 100;

    protected final Pathfinding pathfinding;
    protected final List<GoalGroup> goalGroups;

    private PathTarget destination;
    private PathResult currentPath;

    private Node current;
    private Node target;

    private long recalculationDelay;
    private long lastPathfind;
    private long lastMoved;

    private double lastX;
    private double lastY;
    private double lastZ;

    private int removalAnimationDelay = 1000;

    protected ProximaEntity(@NotNull EntityType entityType, @NotNull UUID uuid, @NotNull Pathfinding pathfinding,
        boolean register) {
        super(entityType, uuid, false);
        this.pathfinding = Objects.requireNonNull(pathfinding);
        this.goalGroups = new ArrayList<>(5);
        pathfinding.setSelf(this);

        if (register) super.register();
    }

    public ProximaEntity(@NotNull EntityType entityType, @NotNull UUID uuid, @NotNull Pathfinding pathfinding) {
        this(entityType, uuid, pathfinding, true);
    }

    public @NotNull Pathfinding pathfinding() {
        return pathfinding;
    }

    public void setRemovalAnimationDelay(int delay) {
        this.removalAnimationDelay = delay;
    }

    private void resetPath() {
        pathfinding.cancel();

        currentPath = null;

        current = null;
        target = null;

        lastMoved = 0;

        lastX = 0;
        lastY = 0;
        lastZ = 0;
    }

    private void destroyPath() {
        resetPath();
        this.destination = null;
        pathfinding.target = null;

        recalculationDelay = 0;
        lastPathfind = 0;
    }

    public void setDestination(@Nullable PathTarget destination) {
        if (destination == null && this.destination != null) {
            destroyPath();
            return;
        }

        if (this.destination == destination) {
            return;
        }

        destroyPath();
        pathfinding.target = null;
        this.destination = destination;
    }

    public <T extends Entity> void setDestination(@Nullable T targetEntity) {
        if (pathfinding.target == targetEntity) {
            return;
        }

        if (targetEntity == null || targetEntity.isRemoved()) {
            destroyPath();
            return;
        }

        destroyPath();
        pathfinding.target = targetEntity;
        this.destination = PathTarget.resolving(() -> {
            if (!pathfinding.isValidTarget(targetEntity)) {
                return null;
            }

            return VecUtils.toDouble(targetEntity.getPosition());
        }, pathfinding.positionResolverForTarget(targetEntity), pathfinding.targetChangePredicate(targetEntity));
    }

    public @Nullable Entity getTargetEntity() {
        return pathfinding.target;
    }

    public void attack(@NotNull Entity target, boolean swingHand) {
        if (swingHand) {
            swingMainHand();
        }

        EventDispatcher.call(new EntityAttackEvent(this, target));
    }

    public void attack(@NotNull Entity target) {
        attack(target, false);
    }

    /**
     * Adds a {@link GoalGroup} to this entity.
     *
     * @param group The {@link GoalGroup} to add
     */
    public void addGoalGroup(@NotNull GoalGroup group) {
        Objects.requireNonNull(group);
        goalGroups.add(group);
    }

    public @NotNull
    @Unmodifiable List<GoalGroup> goalGroups() {
        return Collections.unmodifiableList(goalGroups);
    }

    @Override
    public void update(long time) {
        super.update(time);

        navigatorTick(time);
        aiTick(time);
    }

    @Override
    public void kill() {
        super.kill();

        if (removalAnimationDelay > 0) {
            scheduleRemove(Duration.of(removalAnimationDelay, TimeUnit.MILLISECOND));
        } else {
            remove();
        }
    }

    @Override
    public void remove() {
        super.remove();
        destroyPath();
    }

    protected boolean canNavigate() {
        return !isDead() && getInstance() != null;
    }

    protected void navigatorTick(long time) {
        if (!canNavigate()) {
            return;
        }

        Navigator navigator = pathfinding.getNavigator(getBoundingBox());

        if (pathfinding.target != null && !pathfinding.isValidTarget(pathfinding.target)) {
            destroyPath();
            return;
        }

        if (pathfinding.target != null && getDistanceSquared(pathfinding.target) < ENTITY_LOOK_DISTANCE_SQ) {
            lookAt(pathfinding.target);
        }

        if (navigator.navigationComplete()) {
            currentPath = navigator.getResult();
            if (!initPath(currentPath)) {
                currentPath = null;
            }
        } else if (canNavigate(time)) {
            navigator.navigate(position.x(), position.y(), position.z(), destination);
            this.lastPathfind = time;
        }

        if (currentPath != null && current != null) {
            if (moveAlongPath(time) != MoveResult.CONTINUE) {
                resetPath();
            }
        }
    }

    protected boolean canNavigate(long time) {
        return destination != null && pathfinding.canPathfind(this) &&
            (time - lastPathfind > recalculationDelay &&
                (destination.hasChanged() || (currentPath == null || !currentPath.isSuccessful())));
    }

    protected void aiTick(long time) {
        if (isDead()) {
            return;
        }

        for (GoalGroup group : goalGroups) {
            group.tick(time);
        }
    }

    protected boolean initPath(@NotNull PathResult pathResult) {
        recalculationDelay = pathfinding.recalculationDelay(pathResult);

        Node head = pathResult.head();
        if (head == null) {
            return false;
        }

        Node node = head;
        Point currentPosition = getPosition();

        double closestNodeDistance = Double.POSITIVE_INFINITY;
        Node closestNode = null;

        while (node != null) {
            double thisDistance =
                Vec3D.distanceSquared(node.x + 0.5, 0, node.z + 0.5, currentPosition.x(), 0, currentPosition.z());

            if (thisDistance < closestNodeDistance) {
                closestNodeDistance = thisDistance;
                closestNode = node;
            }

            if (thisDistance < 1) {
                break;
            }

            node = node.parent;
        }

        assert closestNode != null;

        current = closestNode;

        Node currentParent = closestNode.parent;
        target = currentParent == null ? closestNode : currentParent;

        return true;
    }

    protected boolean withinDistance(@Nullable Node node) {
        if (node == null) {
            return false;
        }

        Pos position = getPosition();
        return position.distanceSquared(new Vec(node.x + 0.5, node.y + node.blockOffset, node.z + 0.5)) <
            NODE_REACH_DISTANCE_SQ && (int) Math.floor(position.y()) == node.y;
    }

    protected MoveResult moveAlongPath(long time) {
        Point pos = getPosition();

        if (pos.distanceSquared(current.x + 0.5, current.y + current.blockOffset, current.z + 0.5) >
            NODE_DEVIATION_DISTANCE_SQ && (current.parent != null && !current.equals(currentPath.head()))) {
            return MoveResult.CANCEL;
        }

        Controller controller = pathfinding.getController(this);

        if (withinDistance(target)) {
            current = target;
            target = current.parent;
        }

        Node target = this.target;
        if (target == null && pathfinding.useSynthetic() && pathfinding.target != null && currentPath != null &&
            pathfinding.target.getDistanceSquared(this) < NODE_DEVIATION_DISTANCE_SQ) {
            Pos targetPosition = pathfinding.target.getPosition();
            int sx = targetPosition.blockX();
            int sy = targetPosition.blockY();
            int sz = targetPosition.blockZ();

            if (!pathfinding.getSettings(getBoundingBox()).successPredicate().test(sx, sy, sz,
                current.x, current.y, current.z)) {
                target = new Node(sx, sy, sz, 0, 0, (float) (pathfinding.target.getPosition().y() -
                    pathfinding.target.getPosition().blockY()));
            } else {
                target = current;
            }
        }

        if (target != null) {
            double currentX = pos.x();
            double currentY = pos.y();
            double currentZ = pos.z();

            controller.advance(current, target, pathfinding.target);

            if (!controller.hasControl()) {
                if (!(MathUtils.fuzzyEquals(currentX, lastX, Pathfinding.MOB_PATH_EPSILON) &&
                    MathUtils.fuzzyEquals(currentY, lastY, Pathfinding.MOB_PATH_EPSILON) &&
                    MathUtils.fuzzyEquals(currentZ, lastZ, Pathfinding.MOB_PATH_EPSILON))) {
                    lastMoved = time;
                } else if (time - lastMoved > pathfinding.immobileThreshold()) {
                    //if we don't have any movement, stop moving along this path
                    return MoveResult.CANCEL;
                }
            } else {
                //if jumping, keep updating lastMoved, so we don't consider ourselves stuck
                lastMoved = time;
            }

            lastX = currentX;
            lastY = currentY;
            lastZ = currentZ;
            return MoveResult.CONTINUE;
        }

        return MoveResult.KEEP_DESTINATION;
    }

    protected enum MoveResult {
        CONTINUE,
        CANCEL,
        KEEP_DESTINATION
    }
}
