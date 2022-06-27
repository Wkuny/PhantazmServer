package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.ConfigProcessorUtils;
import com.github.phantazmnetwork.commons.vector.Region3I;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.commons.vector.VectorConfigProcessors;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class MapProcessors {
    private static final ConfigProcessor<MapInfo> mapInfo = new ConfigProcessor<>() {
        @Override
        public MapInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key id = AdventureConfigProcessors.key().dataFromElement(element.getElementOrThrow("id"));
            Vec3I origin = VectorConfigProcessors.vec3I().dataFromElement(element.getElementOrThrow("origin"));
            Vec3I spawn = VectorConfigProcessors.vec3I().dataFromElement(element.getElementOrThrow("spawn"));
            float pitch = element.getNumberOrThrow("pitch").floatValue();
            float yaw = element.getNumberOrThrow("yaw").floatValue();
            Component displayName = AdventureConfigProcessors.component().dataFromElement(element.getElementOrThrow("displayName"));
            String displayItemTag = element.getStringOrThrow("displayItemSnbt");
            List<Component> introMessages = componentList.dataFromElement(element.getElementOrThrow("introMessages"));
            Component scoreboardHeader = AdventureConfigProcessors.component().dataFromElement(element.getElementOrThrow("scoreboardHeader"));
            Vec3I leaderboardPosition = VectorConfigProcessors.vec3I().dataFromElement(element
                    .getElementOrThrow("leaderboardPosition"));
            int leaderboardLength = element.getNumberOrThrow("leaderboardLength").intValue();
            int worldTime = element.getNumberOrThrow("worldTime").intValue();
            int maxPlayers = element.getNumberOrThrow("maxPlayers").intValue();
            int minPlayers = element.getNumberOrThrow("minPlayers").intValue();
            int startingCoins = element.getNumberOrThrow("startingCoins").intValue();
            int repairCoins = element.getNumberOrThrow("repairCoins").intValue();
            double windowRepairRadius = element.getNumberOrThrow("windowRepairRadius").doubleValue();
            int windowRepairTicks = element.getNumberOrThrow("windowRepairTicks").intValue();
            int corpseDeathTicks = element.getNumberOrThrow("corpseDeathTicks").intValue();
            double reviveRadius = element.getNumberOrThrow("reviveRadius").doubleValue();
            boolean canWallshoot = element.getBooleanOrThrow("canWallshoot");
            boolean perksLostOnDeath = element.getBooleanOrThrow("perksLostOnDeath");
            int baseReviveTicks = element.getNumberOrThrow("baseReviveTicks").intValue();
            int rollsPerChest = element.getNumberOrThrow("rollsPerChest").intValue();
            List<Integer> milestoneRounds = integerList.dataFromElement(element.getElementOrThrow("milestoneRounds"));
            List<Key> defaultEquipment = keyList.dataFromElement(element.getElementOrThrow("defaultEquipment"));
            return new MapInfo(id, origin, spawn, pitch, yaw, displayName, displayItemTag, introMessages,
                    scoreboardHeader, leaderboardPosition, leaderboardLength, worldTime, maxPlayers, minPlayers,
                    startingCoins, repairCoins, windowRepairRadius, windowRepairTicks, corpseDeathTicks, reviveRadius,
                    canWallshoot, perksLostOnDeath, baseReviveTicks, rollsPerChest, milestoneRounds, defaultEquipment);
        }

        @Override
        public @NotNull ConfigElement elementFromData(MapInfo mapConfig) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(26);
            node.put("id", AdventureConfigProcessors.key().elementFromData(mapConfig.id()));
            node.put("origin", VectorConfigProcessors.vec3I().elementFromData(mapConfig.origin()));
            node.put("spawn", VectorConfigProcessors.vec3I().elementFromData(mapConfig.spawn()));
            node.put("pitch", new ConfigPrimitive(mapConfig.pitch()));
            node.put("yaw", new ConfigPrimitive(mapConfig.yaw()));
            node.put("displayName", AdventureConfigProcessors.component().elementFromData(mapConfig.displayName()));
            node.put("displayItemSnbt", new ConfigPrimitive(mapConfig.displayItemSnbt()));
            node.put("introMessages", componentList.elementFromData(mapConfig.introMessages()));
            node.put("scoreboardHeader", AdventureConfigProcessors.component().elementFromData(mapConfig.scoreboardHeader()));
            node.put("leaderboardPosition", VectorConfigProcessors.vec3I().elementFromData(mapConfig
                    .leaderboardPosition()));
            node.put("leaderboardLength", new ConfigPrimitive(mapConfig.leaderboardLength()));
            node.put("worldTime", new ConfigPrimitive(mapConfig.worldTime()));
            node.put("maxPlayers", new ConfigPrimitive(mapConfig.maxPlayers()));
            node.put("minPlayers", new ConfigPrimitive(mapConfig.minPlayers()));
            node.put("startingCoins", new ConfigPrimitive(mapConfig.startingCoins()));
            node.put("repairCoins", new ConfigPrimitive(mapConfig.repairCoins()));
            node.put("windowRepairRadius", new ConfigPrimitive(mapConfig.windowRepairRadius()));
            node.put("windowRepairTicks", new ConfigPrimitive(mapConfig.windowRepairTicks()));
            node.put("corpseDeathTicks", new ConfigPrimitive(mapConfig.corpseDeathTicks()));
            node.put("reviveRadius", new ConfigPrimitive(mapConfig.reviveRadius()));
            node.put("canWallshoot", new ConfigPrimitive(mapConfig.canWallshoot()));
            node.put("perksLostOnDeath", new ConfigPrimitive(mapConfig.perksLostOnDeath()));
            node.put("baseReviveTicks", new ConfigPrimitive(mapConfig.baseReviveTicks()));
            node.put("rollsPerChest", new ConfigPrimitive(mapConfig.rollsPerChest()));
            node.put("milestoneRounds", integerList.elementFromData(mapConfig.milestoneRounds()));
            node.put("defaultEquipment", keyList.elementFromData(mapConfig.defaultEquipment()));
            return node;
        }
    };

    private static final ConfigProcessor<RoomInfo> roomInfo = new ConfigProcessor<>() {
        @Override
        public RoomInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key id = AdventureConfigProcessors.key().dataFromElement(element.getElementOrThrow("id"));
            Component displayName = AdventureConfigProcessors.component().dataFromElement(element.getElementOrThrow("displayName"));
            List<Region3I> regions = regionInfoList.dataFromElement(element.getElementOrThrow("regions"));
            System.out.println("dataFromElement: " + element);
            return new RoomInfo(id, displayName, regions);
        }

        @Override
        public @NotNull ConfigElement elementFromData(RoomInfo roomInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(3);
            node.put("id", AdventureConfigProcessors.key().elementFromData(roomInfo.id()));
            node.put("displayName", AdventureConfigProcessors.component().elementFromData(roomInfo.displayName()));
            node.put("regions", regionInfoList.elementFromData(roomInfo.regions()));
            System.out.println("elementFromData: " + node);
            return node;
        }
    };

    private static final ConfigProcessor<DoorInfo> doorInfo = new ConfigProcessor<>() {
        @Override
        public DoorInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key id = AdventureConfigProcessors.key().dataFromElement(element.getElementOrThrow("id"));
            List<Key> opensTo = keyList.dataFromElement(element.getElementOrThrow("opensTo"));
            List<Integer> costs = integerList.dataFromElement(element.getElementOrThrow("costs"));
            List<HologramInfo> hologramInfos = hologramInfoList.dataFromElement(element.getElementOrThrow("holograms"));
            List<Region3I> regions = regionInfoList.dataFromElement(element.getListOrThrow("regions"));
            Sound openSound = AdventureConfigProcessors.sound().dataFromElement(element.getElementOrThrow("openSound"));
            return new DoorInfo(id, opensTo, costs, hologramInfos, regions, openSound);
        }

        @Override
        public @NotNull ConfigElement elementFromData(DoorInfo doorInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(5);
            node.put("id", AdventureConfigProcessors.key().elementFromData(doorInfo.id()));
            node.put("opensTo", keyList.elementFromData(doorInfo.opensTo()));
            node.put("costs", integerList.elementFromData(doorInfo.costs()));
            node.put("holograms", hologramInfoList.elementFromData(doorInfo.holograms()));
            node.put("regions", regionInfoList.elementFromData(doorInfo.regions()));
            node.put("openSound", AdventureConfigProcessors.sound().elementFromData(doorInfo.openSound()));
            return node;
        }
    };

    private static final ConfigProcessor<ShopInfo> shopInfo = new ConfigProcessor<>() {
        @Override
        public ShopInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key id = AdventureConfigProcessors.key().dataFromElement(element.getElementOrThrow("id"));
            Vec3I triggerLocation = VectorConfigProcessors.vec3I().dataFromElement(element
                    .getElementOrThrow("triggerLocation"));
            return new ShopInfo(id, triggerLocation);
        }

        @Override
        public @NotNull ConfigElement elementFromData(ShopInfo shopInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(2);
            node.put("id", AdventureConfigProcessors.key().elementFromData(shopInfo.id()));
            node.put("triggerLocation", VectorConfigProcessors.vec3I().elementFromData(shopInfo.triggerLocation()));
            return node;
        }
    };

    private static final ConfigProcessor<WindowInfo> windowInfo = new ConfigProcessor<>() {
        @Override
        public @NotNull WindowInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Region3I frameRegion = VectorConfigProcessors.region3I().dataFromElement(element
                    .getElementOrThrow("frameRegion"));
            List<String> repairBlocks = stringList.dataFromElement(element.getElementOrThrow("repairBlocks"));
            Sound repairSound = AdventureConfigProcessors.sound().dataFromElement(element.getElementOrThrow("repairSound"));
            Sound repairAllSound = AdventureConfigProcessors.sound().dataFromElement(element.getElementOrThrow("repairAllSound"));
            Sound breakSound = AdventureConfigProcessors.sound().dataFromElement(element.getElementOrThrow("breakSound"));
            Sound breakAllSound = AdventureConfigProcessors.sound().dataFromElement(element.getElementOrThrow("breakAllSound"));
            return new WindowInfo(frameRegion, repairBlocks, repairSound, repairAllSound, breakSound, breakAllSound);
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull WindowInfo windowData) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(6);
            node.put("frameRegion", VectorConfigProcessors.region3I().elementFromData(windowData.frameRegion()));
            node.put("repairBlocks", stringList.elementFromData(windowData.repairBlocks()));
            node.put("repairSound", AdventureConfigProcessors.sound().elementFromData(windowData.repairSound()));
            node.put("repairAllSound", AdventureConfigProcessors.sound().elementFromData(windowData.repairAllSound()));
            node.put("breakSound", AdventureConfigProcessors.sound().elementFromData(windowData.breakSound()));
            node.put("breakAllSound", AdventureConfigProcessors.sound().elementFromData(windowData.breakAllSound()));
            return node;
        }
    };

    private static final ConfigProcessor<RoundInfo> roundInfo = new ConfigProcessor<>() {
        @Override
        public RoundInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            int round = element.getNumberOrThrow("round").intValue();
            List<WaveInfo> waves = waveInfoList.dataFromElement(element.getListOrThrow("waves"));
            return new RoundInfo(round, waves);
        }

        @Override
        public @NotNull ConfigElement elementFromData(RoundInfo roundInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(2);
            node.put("round", new ConfigPrimitive(roundInfo.round()));
            node.put("waves", waveInfoList.elementFromData(roundInfo.waves()));
            return node;
        }
    };

    private static final ConfigProcessor<WaveInfo> waveInfo = new ConfigProcessor<>() {
        @Override
        public WaveInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            List<SpawnInfo> spawns = spawnInfoList.dataFromElement(element.getListOrThrow("spawns"));
            return new WaveInfo(spawns);
        }

        @Override
        public @NotNull ConfigElement elementFromData(WaveInfo waveInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(1);
            node.put("spawns", spawnInfoList.elementFromData(waveInfo.spawns()));
            return node;
        }
    };

    private static final ConfigProcessor<SpawnInfo> spawnInfo = new ConfigProcessor<>() {
        @Override
        public SpawnInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key id = AdventureConfigProcessors.key().dataFromElement(element.getElementOrThrow("id"));
            int amount = element.getNumberOrThrow("amount").intValue();
            return new SpawnInfo(id, amount);
        }

        @Override
        public @NotNull ConfigElement elementFromData(SpawnInfo spawnInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(2);
            node.put("id", AdventureConfigProcessors.key().elementFromData(spawnInfo.id()));
            node.put("amount", new ConfigPrimitive(spawnInfo.amount()));
            return node;
        }
    };

    private static final ConfigProcessor<SpawnpointInfo> spawnpointInfo = new ConfigProcessor<>() {
        @Override
        public SpawnpointInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Vec3I position = VectorConfigProcessors.vec3I().dataFromElement(element.getElementOrThrow("position"));
            Key spawnRule = AdventureConfigProcessors.key().dataFromElement(element.getElementOrThrow("spawnRule"));
            SpawnType type = spawnType.dataFromElement(element.getElementOrThrow("type"));
            return new SpawnpointInfo(position, spawnRule, type);
        }

        @Override
        public @NotNull ConfigElement elementFromData(SpawnpointInfo spawnpointInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(3);
            node.put("position", VectorConfigProcessors.vec3I().elementFromData(spawnpointInfo.position()));
            node.put("spawnRule", AdventureConfigProcessors.key().elementFromData(spawnpointInfo.spawnRule()));
            node.put("type", spawnType.elementFromData(spawnpointInfo.type()));
            return node;
        }
    };

    private static final ConfigProcessor<SpawnruleInfo> spawnruleInfo = new ConfigProcessor<>() {
        @Override
        public SpawnruleInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Key id = AdventureConfigProcessors.key().dataFromElement(element.getElementOrThrow("id"));
            List<Key> spawns = keyList.dataFromElement(element.getElementOrThrow("spawns"));
            boolean isBlacklist = element.getBooleanOrThrow("isBlacklist");
            return new SpawnruleInfo(id, spawns, isBlacklist);
        }

        @Override
        public @NotNull ConfigElement elementFromData(SpawnruleInfo spawnruleInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(3);
            node.put("id", AdventureConfigProcessors.key().elementFromData(spawnruleInfo.id()));
            node.put("spawns", keyList.elementFromData(spawnruleInfo.spawns()));
            node.put("isBlacklist", new ConfigPrimitive(spawnruleInfo.isBlacklist()));
            return node;
        }
    };

    private static final ConfigProcessor<HologramInfo> hologramInfo = new ConfigProcessor<>() {
        @Override
        public HologramInfo dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            Component text = AdventureConfigProcessors.component().dataFromElement(element.getElementOrThrow("text"));
            Vec3D position = VectorConfigProcessors.vec3D().dataFromElement(element.getElementOrThrow("position"));
            return new HologramInfo(text, position);
        }

        @Override
        public @NotNull ConfigElement elementFromData(HologramInfo hologramInfo) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(2);
            node.put("text", AdventureConfigProcessors.component().elementFromData(hologramInfo.text()));
            node.put("position", VectorConfigProcessors.vec3D().elementFromData(hologramInfo.position()));
            return node;
        }
    };

    private static final ConfigProcessor<List<Key>> keyList = ConfigProcessorUtils
            .newListProcessor(AdventureConfigProcessors.key());

    private static final ConfigProcessor<List<Component>> componentList = ConfigProcessorUtils
            .newListProcessor(AdventureConfigProcessors.component());
    private static final ConfigProcessor<SpawnType> spawnType = ConfigProcessorUtils.newEnumProcessor(SpawnType.class);

    private static final ConfigProcessor<List<Region3I>> regionInfoList = ConfigProcessorUtils
            .newListProcessor(VectorConfigProcessors.region3I());

    private static final ConfigProcessor<List<HologramInfo>> hologramInfoList = ConfigProcessorUtils
            .newListProcessor(hologramInfo);

    private static final ConfigProcessor<List<WaveInfo>> waveInfoList = ConfigProcessorUtils.newListProcessor(waveInfo);

    private static final ConfigProcessor<List<SpawnInfo>> spawnInfoList = ConfigProcessorUtils
            .newListProcessor(spawnInfo);

    private static final ConfigProcessor<List<Integer>> integerList = ConfigProcessorUtils.newListProcessor(
            new ConfigProcessor<>() {
        @Override
        public Integer dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            try {
                return element.asNumber().intValue();
            }
            catch (IllegalStateException e) {
                throw new ConfigProcessException(e);
            }
        }

        @Override
        public @NotNull ConfigElement elementFromData(Integer integer) {
            return new ConfigPrimitive(integer);
        }
    });

    private static final ConfigProcessor<List<String>> stringList = ConfigProcessorUtils.newListProcessor(
            new ConfigProcessor<>() {
                @Override
                public String dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                    try {
                        return element.asString();
                    }
                    catch (IllegalStateException e) {
                        throw new ConfigProcessException(e);
                    }
                }

                @Override
                public @NotNull ConfigElement elementFromData(String string) {
                    return new ConfigPrimitive(string);
                }
            });

    private MapProcessors() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull ConfigProcessor<MapInfo> mapInfo() {
        return mapInfo;
    }

    public static @NotNull ConfigProcessor<RoomInfo> roomInfo() {
        return roomInfo;
    }

    public static @NotNull ConfigProcessor<DoorInfo> doorInfo() {
        return doorInfo;
    }

    public static @NotNull ConfigProcessor<ShopInfo> shopInfo() { return shopInfo; }

    public static @NotNull ConfigProcessor<WindowInfo> windowInfo() {
        return windowInfo;
    }

    public static @NotNull ConfigProcessor<SpawnpointInfo> spawnpointInfo() {
        return spawnpointInfo;
    }

    public static @NotNull ConfigProcessor<SpawnruleInfo> spawnruleInfo() {
        return spawnruleInfo;
    }

    public static @NotNull ConfigProcessor<SpawnType> spawnType() {
        return spawnType;
    }

    public static @NotNull ConfigProcessor<RoundInfo> roundInfo() {
        return roundInfo;
    }

    public static @NotNull ConfigProcessor<WaveInfo> waveInfo() {
        return waveInfo;
    }

    public static @NotNull ConfigProcessor<SpawnInfo> spawnInfo() {
        return spawnInfo;
    }
}