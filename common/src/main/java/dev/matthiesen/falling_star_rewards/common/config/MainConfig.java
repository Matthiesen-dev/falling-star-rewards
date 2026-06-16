package dev.matthiesen.falling_star_rewards.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public final class MainConfig {
    @SerializedName("enabled")
    public boolean enabled = true;

    @SerializedName("scheduler")
    public Scheduler scheduler = new Scheduler();

    @SerializedName("activation")
    public Activation activation = new Activation();

    @SerializedName("spawn")
    public Spawn spawn = new Spawn();

    @SerializedName("claim")
    public Claim claim = new Claim();

    @SerializedName("announcements")
    public Announcements announcements = new Announcements();

    @SerializedName("rewards")
    public Rewards rewards = new Rewards();

    @SuppressWarnings("unused")
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    public static final class Scheduler {
        @SerializedName("baseIntervalTicks")
        public int baseIntervalTicks = 20 * 120;

        @SerializedName("intervalJitterTicks")
        public int intervalJitterTicks = 20 * 30;

        @SerializedName("maxStarsPerCycle")
        public int maxStarsPerCycle = 1;
    }

    public static final class Activation {
        @SerializedName("requireNight")
        public boolean requireNight = true;

        @SerializedName("requireSurfaceAccess")
        public boolean requireSurfaceAccess = true;

        @SerializedName("weatherMode")
        public String weatherMode = "any";
    }

    public static final class Spawn {
        @SerializedName("targetScope")
        public String targetScope = "per_player";

        @SerializedName("minRadius")
        public int minRadius = 16;

        @SerializedName("maxRadius")
        public int maxRadius = 48;

        @SerializedName("maxLocationAttempts")
        public int maxLocationAttempts = 12;

        @SerializedName("allowWaterSpawns")
        public boolean allowWaterSpawns = false;
    }

    public static final class Claim {
        @SerializedName("lifeTicks")
        public int lifeTicks = 20 * 45;

        @SerializedName("pickupDelayTicks")
        public int pickupDelayTicks = 10;

        @SerializedName("maxActiveDrops")
        public int maxActiveDrops = 64;
    }

    public static final class Announcements {
        @SerializedName("enabled")
        public boolean enabled = true;

        @SerializedName("scope")
        public String scope = "nearby";

        @SerializedName("spawnMessage")
        public String spawnMessage = "A falling star has appeared nearby!";
    }

    public static final class Rewards {
        @SerializedName("poolMode")
        public String poolMode = "weighted";

        @SerializedName("entries")
        public RewardEntry[] entries = new RewardEntry[] {
                new RewardEntry("minecraft:amethyst_shard", 20, 1, 3),
                new RewardEntry("minecraft:glowstone_dust", 12, 2, 5),
                new RewardEntry("minecraft:nether_star", 1, 1, 1)
        };
    }

    public static final class RewardEntry {
        @SerializedName("id")
        public String id;

        @SerializedName("weight")
        public int weight;

        @SerializedName("minCount")
        public int minCount;

        @SerializedName("maxCount")
        public int maxCount;

        @SuppressWarnings("unused")
        public RewardEntry() {
            this("minecraft:amethyst_shard", 1, 1, 1);
        }

        public RewardEntry(String id, int weight, int minCount, int maxCount) {
            this.id = id;
            this.weight = weight;
            this.minCount = minCount;
            this.maxCount = maxCount;
        }
    }
}
