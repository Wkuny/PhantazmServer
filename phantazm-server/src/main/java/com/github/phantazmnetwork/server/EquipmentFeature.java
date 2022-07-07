package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.api.config.processor.ItemStackConfigProcessors;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.commons.config.ComplexData;
import com.github.phantazmnetwork.commons.config.ComplexDataConfigProcessor;
import com.github.phantazmnetwork.commons.factory.DependencyProvider;
import com.github.phantazmnetwork.commons.factory.Factory;
import com.github.phantazmnetwork.commons.factory.FactoryDependencyProvider;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import com.github.phantazmnetwork.zombies.equipment.gun.GunLevel;
import com.github.phantazmnetwork.zombies.equipment.gun.GunModel;
import com.github.phantazmnetwork.zombies.equipment.gun.audience.AudienceProvider;
import com.github.phantazmnetwork.zombies.equipment.gun.audience.EntityInstanceAudienceProvider;
import com.github.phantazmnetwork.zombies.equipment.gun.audience.EntityAudienceProvider;
import com.github.phantazmnetwork.zombies.equipment.gun.data.GunLevelDataConfigProcessor;
import com.github.phantazmnetwork.zombies.equipment.gun.GunStats;
import com.github.phantazmnetwork.zombies.equipment.gun.data.GunData;
import com.github.phantazmnetwork.zombies.equipment.gun.data.GunLevelData;
import com.github.phantazmnetwork.zombies.equipment.gun.effect.*;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.ReloadTester;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.StateReloadTester;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.actionbar.GradientActionBarChooser;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.actionbar.ReloadActionBarChooser;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.actionbar.StaticActionBarChooser;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.ShootTester;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.StateShootTester;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint.*;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.Firer;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.HitScanFirer;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.projectile.PhantazmProjectileCollisionFilter;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.projectile.ProjectileCollisionFilter;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.projectile.ProjectileFirer;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.SpreadFirer;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler.*;
import com.github.phantazmnetwork.zombies.equipment.gun.target.BasicTargetFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.TargetFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional.AroundEndFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional.BetweenPointsFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional.DirectionalEntityFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.positional.NearbyEntityFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.positional.PositionalEntityFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.headshot.EyeHeightHeadshotTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.headshot.HeadshotTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.headshot.StaticHeadshotTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.intersectionfinder.IntersectionFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.intersectionfinder.RayTraceIntersectionFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.intersectionfinder.StaticIntersectionFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.limiter.DistanceTargetLimiter;
import com.github.phantazmnetwork.zombies.equipment.gun.target.limiter.TargetLimiter;
import com.github.phantazmnetwork.zombies.equipment.gun.target.tester.PhantazmTargetTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.tester.TargetTester;
import com.github.phantazmnetwork.zombies.equipment.gun.visual.ClipStackMapper;
import com.github.phantazmnetwork.zombies.equipment.gun.visual.GunStackMapper;
import com.github.phantazmnetwork.zombies.equipment.gun.visual.ReloadStackMapper;
import com.github.steanky.ethylene.core.bridge.ConfigBridges;
import com.github.steanky.ethylene.core.codec.ConfigCodec;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

final class EquipmentFeature {

    private static final Logger LOGGER = LoggerFactory.getLogger(EquipmentFeature.class);

    private static Map<Key, List<ComplexData>> gunLevelMap = null;

    private EquipmentFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull Path equipmentPath, @NotNull ConfigCodec codec) {
        String ending;
        if (codec.getPreferredExtensions().isEmpty()) {
            ending = "";
        }
        else {
            ending = "." + codec.getPreferredExtensions().get(0);
        }
        PathMatcher matcher = equipmentPath.getFileSystem().getPathMatcher("glob:**" + ending);

        Path guns = equipmentPath.resolve("guns");
        try {
            Files.createDirectories(guns);
        } catch (IOException e) {
            LOGGER.warn("Failed to create guns directory.", e);
            return;
        }

        ConfigProcessor<GunData> gunDataProcessor = GunData.processor();
        ConfigProcessor<ComplexData> gunLevelProcessor = createGunLevelProcessor();
        Map<Key, BiConsumer<? extends Keyed, Collection<Key>>> dependencyAdders = createDependencyAdders();

        gunLevelMap = new HashMap<>();
        try (Stream<Path> gunDirectories = Files.list(guns)) {
            gunIteration: for (Path gunDirectory : (Iterable<? extends Path>) gunDirectories::iterator) {
                if (!Files.isDirectory(gunDirectory)) {
                    continue;
                }

                String infoFileName;
                if (codec.getPreferredExtensions().isEmpty()) {
                    infoFileName = "info";
                }
                else {
                    infoFileName = "info." + codec.getPreferredExtensions().get(0);
                }
                Path infoPath = gunDirectory.resolve(infoFileName);
                if (!Files.isRegularFile(infoPath)) {
                    LOGGER.warn("No info file at {}.", infoPath);
                    continue;
                }

                GunData gunData;
                try {
                    gunData = ConfigBridges.read(infoPath, codec, gunDataProcessor);
                } catch (ConfigProcessException e) {
                    LOGGER.warn("Failed to read info file at {}.", infoPath, e);
                    continue;
                }

                Int2ObjectMap<ComplexData> levelDataMap = new Int2ObjectOpenHashMap<>();
                Path levelsPath = gunDirectory.resolve("levels");
                try (Stream<Path> levelDirectories = Files.list(levelsPath)) {
                    for (Path levelFile : (Iterable<? extends Path>) levelDirectories::iterator) {
                        if (!(Files.isRegularFile(levelFile) && matcher.matches(levelFile))) {
                            continue;
                        }

                        try {
                            ComplexData data = ConfigBridges.read(levelFile, codec, gunLevelProcessor);

                            Set<Key> required = new HashSet<>();
                            for (Keyed object : data.objects().values()) {
                                BiConsumer<? extends Keyed, Collection<Key>> dependencyAdder
                                        = dependencyAdders.get(object.key());
                                if (dependencyAdder != null) {
                                    invokeDependencyAdder(dependencyAdder, object, required);
                                }
                            }
                            required.removeAll(data.objects().keySet());

                            if (!required.isEmpty()) {
                                LOGGER.warn("Invalid gun level at {}. Missing required keys: {}", levelFile, required);
                                continue;
                            }

                            Keyed mainObject = data.objects().get(data.mainKey());
                            if (!(mainObject instanceof GunLevelData gunLevelData)) {
                                LOGGER.warn("Invalid gun level at {}. No gun level data.", levelFile);
                                continue;
                            }

                            int order = gunLevelData.order();
                            levelDataMap.put(order, data);
                        }
                        catch (IOException e) {
                            LOGGER.warn("Failed to read level file at {}.", levelFile, e);
                        }
                    }
                }
                catch (IOException e) {
                    LOGGER.warn("Failed to read levels directory at {}.", levelsPath, e);
                    continue;
                }

                int maxOrder = -1;
                for (int order : levelDataMap.keySet()) {
                    if (order > maxOrder) {
                        maxOrder = order;
                    }
                }
                if (maxOrder == -1) {
                    LOGGER.warn("The gun at {} needs to have at least one level}.", gunDirectory);
                    continue;
                }

                List<ComplexData> levelData = new ArrayList<>(levelDataMap.size());
                for (int i = 0; i <= maxOrder; i++) {
                    ComplexData data = levelDataMap.get(i);
                    if (data != null) {
                        levelData.add(data);
                    }
                    else {
                        LOGGER.warn("Missing level {} for gun {}.", i, gunData.name());
                        continue gunIteration;
                    }
                }

                gunLevelMap.put(gunData.name(), levelData);
            }
        }
        catch (IOException e) {
            LOGGER.warn("Failed to list guns directory at {}", guns, e);
        }

        LOGGER.info("Loaded {} guns.", gunLevelMap.size());
    }

    // this should be private when GunTest is deleted
    public static @NotNull ConfigProcessor<ComplexData> createGunLevelProcessor() {
        Map<Key, ConfigProcessor<? extends Keyed>> gunProcessors = new HashMap<>(38);
        gunProcessors.put(GunStats.SERIAL_KEY, GunStats.processor());
        gunProcessors.put(GunLevelData.SERIAL_KEY, new GunLevelDataConfigProcessor(ItemStackConfigProcessors.snbt()));
        gunProcessors.put(EntityInstanceAudienceProvider.Data.SERIAL_KEY, EntityInstanceAudienceProvider.processor());
        gunProcessors.put(EntityAudienceProvider.Data.SERIAL_KEY, EntityAudienceProvider.processor());
        gunProcessors.put(AmmoLevelEffect.Data.SERIAL_KEY, AmmoLevelEffect.processor());
        gunProcessors.put(PlaySoundEffect.Data.SERIAL_KEY, PlaySoundEffect.processor());
        gunProcessors.put(ReloadActionBarEffect.Data.SERIAL_KEY, ReloadActionBarEffect.processor());
        gunProcessors.put(SendMessageEffect.Data.SERIAL_KEY, SendMessageEffect.processor());
        gunProcessors.put(ShootExpEffect.Data.SERIAL_KEY, ShootExpEffect.processor());
        gunProcessors.put(GradientActionBarChooser.Data.SERIAL_KEY, GradientActionBarChooser.processor());
        gunProcessors.put(StaticActionBarChooser.Data.SERIAL_KEY, StaticActionBarChooser.processor());
        gunProcessors.put(StateReloadTester.Data.SERIAL_KEY, StateReloadTester.processor());
        gunProcessors.put(BasicShotEndpointSelector.Data.SERIAL_KEY, BasicShotEndpointSelector.processor());
        gunProcessors.put(RayTraceBlockIteration.Data.SERIAL_KEY, RayTraceBlockIteration.processor());
        gunProcessors.put(WallshotBlockIteration.Data.SERIAL_KEY, WallshotBlockIteration.processor());
        gunProcessors.put(HitScanFirer.Data.SERIAL_KEY, HitScanFirer.processor());
        gunProcessors.put(PhantazmProjectileCollisionFilter.Data.SERIAL_KEY, PhantazmProjectileCollisionFilter.processor());
        gunProcessors.put(ProjectileFirer.Data.SERIAL_KEY, ProjectileFirer.processor());
        gunProcessors.put(SpreadFirer.Data.SERIAL_KEY, SpreadFirer.processor());
        gunProcessors.put(ChainShotHandler.Data.SERIAL_KEY, ChainShotHandler.processor());
        gunProcessors.put(DamageShotHandler.Data.SERIAL_KEY, DamageShotHandler.processor());
        gunProcessors.put(ExplosionShotHandler.Data.SERIAL_KEY, ExplosionShotHandler.processor());
        gunProcessors.put(FeedbackShotHandler.Data.SERIAL_KEY, FeedbackShotHandler.processor());
        gunProcessors.put(GuardianBeamShotHandler.Data.SERIAL_KEY, GuardianBeamShotHandler.processor());
        gunProcessors.put(IgniteShotHandler.Data.SERIAL_KEY, IgniteShotHandler.processor());
        gunProcessors.put(KnockbackShotHandler.Data.SERIAL_KEY, KnockbackShotHandler.processor());
        gunProcessors.put(ParticleTrailShotHandler.Data.SERIAL_KEY, ParticleTrailShotHandler.processor());
        gunProcessors.put(PotionShotHandler.Data.SERIAL_KEY, PotionShotHandler.processor());
        gunProcessors.put(SoundShotHandler.Data.SERIAL_KEY, SoundShotHandler.processor());
        gunProcessors.put(StateShootTester.Data.SERIAL_KEY, StateShootTester.processor());
        gunProcessors.put(AroundEndFinder.Data.SERIAL_KEY, AroundEndFinder.processor());
        gunProcessors.put(BetweenPointsFinder.Data.SERIAL_KEY, BetweenPointsFinder.processor());
        gunProcessors.put(NearbyEntityFinder.Data.SERIAL_KEY, NearbyEntityFinder.processor());
        gunProcessors.put(PhantazmTargetTester.Data.SERIAL_KEY, PhantazmTargetTester.processor());
        gunProcessors.put(EyeHeightHeadshotTester.Data.SERIAL_KEY, EyeHeightHeadshotTester.processor());
        gunProcessors.put(StaticHeadshotTester.Data.SERIAL_KEY, StaticHeadshotTester.processor());
        gunProcessors.put(RayTraceIntersectionFinder.Data.SERIAL_KEY, RayTraceIntersectionFinder.processor());
        gunProcessors.put(StaticIntersectionFinder.Data.SERIAL_KEY, StaticIntersectionFinder.processor());
        gunProcessors.put(BasicTargetFinder.Data.SERIAL_KEY, BasicTargetFinder.processor());
        gunProcessors.put(DistanceTargetLimiter.Data.SERIAL_KEY, DistanceTargetLimiter.processor());
        gunProcessors.put(ClipStackMapper.Data.SERIAL_KEY, ClipStackMapper.processor());
        gunProcessors.put(ReloadStackMapper.Data.SERIAL_KEY, ReloadStackMapper.processor());
        return new ComplexDataConfigProcessor(gunProcessors);
    }

    private static @NotNull Map<Key, BiConsumer<? extends Keyed, Collection<Key>>> createDependencyAdders() {
        Map<Key, BiConsumer<? extends Keyed, Collection<Key>>> dependencyAdders = new HashMap<>(13);
        dependencyAdders.put(GunLevelData.SERIAL_KEY, GunLevelData.dependencyConsumer());
        dependencyAdders.put(PlaySoundEffect.Data.SERIAL_KEY, PlaySoundEffect.dependencyConsumer());
        dependencyAdders.put(ReloadActionBarEffect.Data.SERIAL_KEY, ReloadActionBarEffect.dependencyConsumer());
        dependencyAdders.put(ShootExpEffect.Data.SERIAL_KEY, ShootExpEffect.dependencyConsumer());
        dependencyAdders.put(StateReloadTester.Data.SERIAL_KEY, StateReloadTester.dependencyConsumer());
        dependencyAdders.put(StateShootTester.Data.SERIAL_KEY, StateShootTester.dependencyConsumer());
        dependencyAdders.put(BasicShotEndpointSelector.Data.SERIAL_KEY, BasicShotEndpointSelector.dependencyConsumer());
        dependencyAdders.put(HitScanFirer.Data.SERIAL_KEY, HitScanFirer.dependencyConsumer());
        dependencyAdders.put(ProjectileFirer.Data.SERIAL_KEY, ProjectileFirer.dependencyConsumer());
        dependencyAdders.put(SpreadFirer.Data.SERIAL_KEY, SpreadFirer.dependencyConsumer());
        dependencyAdders.put(ChainShotHandler.Data.SERIAL_KEY, ChainShotHandler.dependencyConsumer());
        dependencyAdders.put(SoundShotHandler.Data.SERIAL_KEY, SoundShotHandler.dependencyConsumer());
        dependencyAdders.put(BasicTargetFinder.Data.SERIAL_KEY, BasicTargetFinder.dependencyConsumer());
        dependencyAdders.put(ClipStackMapper.Data.SERIAL_KEY, ClipStackMapper.dependencyConsumer());
        dependencyAdders.put(ReloadStackMapper.Data.SERIAL_KEY, ReloadStackMapper.dependencyConsumer());

        return dependencyAdders;
    }

    @SuppressWarnings("unchecked")
    private static <TObject extends Keyed> void invokeDependencyAdder(@NotNull BiConsumer<TObject, Collection<Key>> dependencyAdder,
                                                                      @NotNull Keyed object,
                                                                      @NotNull Collection<Key> dependencies) {
        dependencyAdder.accept((TObject) object, dependencies);
    }

    public static @NotNull Gun createGun(@NotNull Key key, @NotNull EventNode<Event> node, @NotNull MobStore store,
                                         @NotNull PlayerView playerView, @NotNull Random random) {
        List<ComplexData> complexDataList = gunLevelMap.get(key);
        if (complexDataList == null) {
            throw new IllegalArgumentException("No gun level data found for key " + key);
        }

        Factory<GunStats, GunStats> gunStats = (provider, data) -> data; // this is a little weird
        Factory<GunLevelData, GunLevel> gunLevel = (provider, data) -> {
            GunStats stats = provider.getDependency(data.stats());
            ShootTester shootTester = provider.getDependency(data.shootTester());
            ReloadTester reloadTester = provider.getDependency(data.reloadTester());
            Firer firer = provider.getDependency(data.firer());
            Collection<GunEffect> shootEffects = provider.getDependency(data.shootEffects());
            Collection<GunEffect> reloadEffects = provider.getDependency(data.reloadEffects());
            Collection<GunEffect> tickEffects = provider.getDependency(data.tickEffects());
            Collection<GunEffect> noAmmoEffects = provider.getDependency(data.noAmmoEffects());
            Collection<GunStackMapper> gunStackMappers = provider.getDependency(data.gunStackMappers());

            return new GunLevel(data.stack(), stats, shootTester, reloadTester, firer, shootEffects,
                    reloadEffects, tickEffects, noAmmoEffects, gunStackMappers);
        };
        Factory<EntityInstanceAudienceProvider.Data, EntityInstanceAudienceProvider> entityInstanceAudienceProvider
                = (provider, data) -> new EntityInstanceAudienceProvider(playerView::getPlayer);
        Factory<EntityAudienceProvider.Data, EntityAudienceProvider> playerAudienceProvider
                = (provider, data) -> new EntityAudienceProvider(playerView::getPlayer);
        Factory<AmmoLevelEffect.Data, AmmoLevelEffect> ammoLevelEffect
                = (provider, data) -> new AmmoLevelEffect(playerView);
        Factory<PlaySoundEffect.Data, PlaySoundEffect> playSoundEffect = (provider, data) -> {
            AudienceProvider audienceProvider = provider.getDependency(data.audienceProviderKey());
            return new PlaySoundEffect(data, audienceProvider);
        };
        Factory<ReloadActionBarEffect.Data, ReloadActionBarEffect> reloadActionBarEffect = (provider, data) -> {
            GunStats stats = provider.getDependency(data.statsKey());
            ReloadTester reloadTester = provider.getDependency(data.reloadTesterKey());
            ReloadActionBarChooser chooser = provider.getDependency(data.reloadActionBarChooserKey());
            return new ReloadActionBarEffect(playerView, stats, reloadTester, chooser);
        };
        Factory<SendMessageEffect.Data, SendMessageEffect> sendMessageEffect
                = (provider, data) -> new SendMessageEffect(data, playerView);
        Factory<ShootExpEffect.Data, ShootExpEffect> shootExpEffect = (provider, data) -> {
            GunStats stats = provider.getDependency(data.statsKey());
            return new ShootExpEffect(playerView, stats);
        };
        Factory<GradientActionBarChooser.Data, GradientActionBarChooser> gradientActionBarChooser
                = (provider, data) -> new GradientActionBarChooser(data);
        Factory<StaticActionBarChooser.Data, StaticActionBarChooser> staticActionBarChooser
                = (provider, data) -> new StaticActionBarChooser(data);
        Factory<StateReloadTester.Data, StateReloadTester> stateReloadTester = (provider, data) -> {
            GunStats stats = provider.getDependency(data.statsKey());
            return new StateReloadTester(stats);
        };
        Factory<BasicShotEndpointSelector.Data, BasicShotEndpointSelector> basicShotEndpointSelector = (provider, data) -> {
            BlockIteration blockIteration = provider.getDependency(data.blockIterationKey());
            return new BasicShotEndpointSelector(data, playerView::getPlayer, blockIteration);
        };
        Factory<RayTraceBlockIteration.Data, RayTraceBlockIteration> rayTraceBlockIteration
                = (provider, data) -> new RayTraceBlockIteration();
        Factory<WallshotBlockIteration.Data, WallshotBlockIteration> wallshotBlockIteration
                = (provider, data) -> new WallshotBlockIteration();
        Factory<HitScanFirer.Data, HitScanFirer> hitScanFirer = (provider, data) -> {
            ShotEndpointSelector endSelector = provider.getDependency(data.endSelectorKey());
            TargetFinder targetFinder = provider.getDependency(data.targetFinderKey());
            Collection<ShotHandler> shotHandlers = provider.getDependency(data.shotHandlerKeys());

            return new HitScanFirer(playerView::getPlayer, endSelector, targetFinder, shotHandlers);
        };
        Factory<PhantazmProjectileCollisionFilter.Data, PhantazmProjectileCollisionFilter> phantazmProjectileCollisionFilter
                = (provider, data) -> new PhantazmProjectileCollisionFilter(store);
        Factory<ProjectileFirer.Data, ProjectileFirer> projectileFirer = (provider, data) -> {
            ShotEndpointSelector endSelector = provider.getDependency(data.endSelectorKey());
            TargetFinder targetFinder = provider.getDependency(data.targetFinderKey());
            ProjectileCollisionFilter collisionFilter = provider.getDependency(data.collisionFilterKey());
            Collection<ShotHandler> shotHandlers = provider.getDependency(data.shotHandlerKeys());

            ProjectileFirer firer = new ProjectileFirer(data, playerView::getPlayer, playerView.getUUID(), endSelector,
                    targetFinder, collisionFilter, shotHandlers);
            node.addListener(ProjectileCollideWithBlockEvent.class, firer::onProjectileCollision);
            node.addListener(ProjectileCollideWithEntityEvent.class, firer::onProjectileCollision);

            return firer;
        };
        Factory<SpreadFirer.Data, SpreadFirer> spreadFirer = (provider, data) -> {
            Collection<Firer> subFirers = provider.getDependency(data.subFirerKeys());
            return new SpreadFirer(data, random, subFirers);
        };
        Factory<ChainShotHandler.Data, ChainShotHandler> chainShotHandler = (provider, data) -> {
            PositionalEntityFinder finder = provider.getDependency(data.finderKey());
            Firer firer = provider.getDependency(data.firerKey());

            return new ChainShotHandler(data, finder, firer);
        };
        Factory<DamageShotHandler.Data, DamageShotHandler> damageShotHandler
                = (provider, data) -> new DamageShotHandler(data);
        Factory<ExplosionShotHandler.Data, ExplosionShotHandler> explosionShotHandler
                = (provider, data) -> new ExplosionShotHandler(data);
        Factory<FeedbackShotHandler.Data, FeedbackShotHandler> feedbackShotHandler
                = (provider, data) -> new FeedbackShotHandler(data, playerView);
        Factory<GuardianBeamShotHandler.Data, GuardianBeamShotHandler> guardianBeamShotHandler
                = (provider, data) -> new GuardianBeamShotHandler(data);
        Factory<IgniteShotHandler.Data, IgniteShotHandler> igniteShotHandler
                = (provider, data) -> new IgniteShotHandler(data);
        Factory<KnockbackShotHandler.Data, KnockbackShotHandler> knockbackShotHandler
                = (provider, data) -> new KnockbackShotHandler(data);
        Factory<ParticleTrailShotHandler.Data, ParticleTrailShotHandler> particleTrailShotHandler
                = (provider, data) -> new ParticleTrailShotHandler(data);
        Factory<PotionShotHandler.Data, PotionShotHandler> potionShotHandler
                = (provider, data) -> new PotionShotHandler(data);
        Factory<SoundShotHandler.Data, SoundShotHandler> soundShotHandler
                = (provider, data) -> {
            AudienceProvider audienceProvider = provider.getDependency(data.audienceProviderKey());
            return new SoundShotHandler(data, audienceProvider);
        };
        Factory<StateShootTester.Data, StateShootTester> stateShootTester = (provider, data) -> {
            GunStats stats = provider.getDependency(data.statsKey());
            ReloadTester reloadTester = provider.getDependency(data.reloadTesterKey());
            return new StateShootTester(stats, reloadTester);
        };
        Factory<AroundEndFinder.Data, AroundEndFinder> aroundEndFinder = (provider, data) -> new AroundEndFinder(data);
        Factory<BetweenPointsFinder.Data, BetweenPointsFinder> betweenPointsFinder
                = (provider, data) -> new BetweenPointsFinder();
        Factory<NearbyEntityFinder.Data, NearbyEntityFinder> nearbyEntityFinder
                = (provider, data) -> new NearbyEntityFinder(data);
        Factory<PhantazmTargetTester.Data, PhantazmTargetTester> phantazmTargetTester
                = (provider, data) -> new PhantazmTargetTester(data, store);
        Factory<EyeHeightHeadshotTester.Data, EyeHeightHeadshotTester> eyeHeightHeadshotTester
                = (provider, data) -> new EyeHeightHeadshotTester();
        Factory<StaticHeadshotTester.Data, StaticHeadshotTester> staticHeadshotTester
                = (provider, data) -> new StaticHeadshotTester(data);
        Factory<RayTraceIntersectionFinder.Data, RayTraceIntersectionFinder> rayTraceTargetTester
                = (provider, data) -> new RayTraceIntersectionFinder();
        Factory<StaticIntersectionFinder.Data, StaticIntersectionFinder> staticTargetTester
                = (provider, data) -> new StaticIntersectionFinder();
        Factory<BasicTargetFinder.Data, BasicTargetFinder> basicTargetFinder = (provider, data) -> {
            DirectionalEntityFinder finder = provider.getDependency(data.entityFinderKey());
            TargetTester targetTester = provider.getDependency(data.targetTesterKey());
            IntersectionFinder intersectionFinder = provider.getDependency(data.intersectionFinderKey());
            HeadshotTester headshotTester = provider.getDependency(data.headshotTesterKey());
            TargetLimiter targetLimiter = provider.getDependency(data.targetLimiterKey());
            return new BasicTargetFinder(finder, targetTester, intersectionFinder, headshotTester, targetLimiter);
        };
        Factory<DistanceTargetLimiter.Data, DistanceTargetLimiter> distanceTargetLimiter
                = (provider, data) -> new DistanceTargetLimiter(data);
        Factory<ClipStackMapper.Data, ClipStackMapper> clipStackMapper = (provider, data) -> {
            ReloadTester reloadTester = provider.getDependency(data.reloadTesterKey());
            return new ClipStackMapper(reloadTester);
        };
        Factory<ReloadStackMapper.Data, ReloadStackMapper> reloadStackMapper = (provider, data) -> {
            GunStats stats = provider.getDependency(data.statsKey());
            ReloadTester reloadTester = provider.getDependency(data.reloadTesterKey());
            return new ReloadStackMapper(stats, reloadTester);
        };

        Map<Key, Factory<?, ?>> factories = new HashMap<>(38);
        factories.put(GunStats.SERIAL_KEY, gunStats);
        factories.put(GunLevelData.SERIAL_KEY, gunLevel);
        factories.put(EntityInstanceAudienceProvider.Data.SERIAL_KEY, entityInstanceAudienceProvider);
        factories.put(EntityAudienceProvider.Data.SERIAL_KEY, playerAudienceProvider);
        factories.put(AmmoLevelEffect.Data.SERIAL_KEY, ammoLevelEffect);
        factories.put(PlaySoundEffect.Data.SERIAL_KEY, playSoundEffect);
        factories.put(ReloadActionBarEffect.Data.SERIAL_KEY, reloadActionBarEffect);
        factories.put(SendMessageEffect.Data.SERIAL_KEY, sendMessageEffect);
        factories.put(ShootExpEffect.Data.SERIAL_KEY, shootExpEffect);
        factories.put(GradientActionBarChooser.Data.SERIAL_KEY, gradientActionBarChooser);
        factories.put(StaticActionBarChooser.Data.SERIAL_KEY, staticActionBarChooser);
        factories.put(StateReloadTester.Data.SERIAL_KEY, stateReloadTester);
        factories.put(BasicShotEndpointSelector.Data.SERIAL_KEY, basicShotEndpointSelector);
        factories.put(RayTraceBlockIteration.Data.SERIAL_KEY, rayTraceBlockIteration);
        factories.put(WallshotBlockIteration.Data.SERIAL_KEY, wallshotBlockIteration);
        factories.put(HitScanFirer.Data.SERIAL_KEY, hitScanFirer);
        factories.put(PhantazmProjectileCollisionFilter.Data.SERIAL_KEY, phantazmProjectileCollisionFilter);
        factories.put(ProjectileFirer.Data.SERIAL_KEY, projectileFirer);
        factories.put(SpreadFirer.Data.SERIAL_KEY, spreadFirer);
        factories.put(ChainShotHandler.Data.SERIAL_KEY, chainShotHandler);
        factories.put(DamageShotHandler.Data.SERIAL_KEY, damageShotHandler);
        factories.put(ExplosionShotHandler.Data.SERIAL_KEY, explosionShotHandler);
        factories.put(FeedbackShotHandler.Data.SERIAL_KEY, feedbackShotHandler);
        factories.put(GuardianBeamShotHandler.Data.SERIAL_KEY, guardianBeamShotHandler);
        factories.put(IgniteShotHandler.Data.SERIAL_KEY, igniteShotHandler);
        factories.put(KnockbackShotHandler.Data.SERIAL_KEY, knockbackShotHandler);
        factories.put(ParticleTrailShotHandler.Data.SERIAL_KEY, particleTrailShotHandler);
        factories.put(PotionShotHandler.Data.SERIAL_KEY, potionShotHandler);
        factories.put(SoundShotHandler.Data.SERIAL_KEY, soundShotHandler);
        factories.put(StateShootTester.Data.SERIAL_KEY, stateShootTester);
        factories.put(AroundEndFinder.Data.SERIAL_KEY, aroundEndFinder);
        factories.put(BetweenPointsFinder.Data.SERIAL_KEY, betweenPointsFinder);
        factories.put(NearbyEntityFinder.Data.SERIAL_KEY, nearbyEntityFinder);
        factories.put(PhantazmTargetTester.Data.SERIAL_KEY, phantazmTargetTester);
        factories.put(EyeHeightHeadshotTester.Data.SERIAL_KEY, eyeHeightHeadshotTester);
        factories.put(StaticHeadshotTester.Data.SERIAL_KEY, staticHeadshotTester);
        factories.put(RayTraceIntersectionFinder.Data.SERIAL_KEY, rayTraceTargetTester);
        factories.put(StaticIntersectionFinder.Data.SERIAL_KEY, staticTargetTester);
        factories.put(BasicTargetFinder.Data.SERIAL_KEY, basicTargetFinder);
        factories.put(DistanceTargetLimiter.Data.SERIAL_KEY, distanceTargetLimiter);
        factories.put(ClipStackMapper.Data.SERIAL_KEY, clipStackMapper);
        factories.put(ReloadStackMapper.Data.SERIAL_KEY, reloadStackMapper);

        List<GunLevel> gunLevels = new ArrayList<>(complexDataList.size());
        for (ComplexData complexData : complexDataList) {
            DependencyProvider getter = new FactoryDependencyProvider(complexData.objects(), factories);
            gunLevels.add(getter.getDependency(complexData.mainKey()));
        }

        return new Gun(playerView, new GunModel(gunLevels, key));
    }

}
