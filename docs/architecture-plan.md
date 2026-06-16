# Falling Star Rewards - Architecture Plan (Server-Side)

## Goals
- Run entirely server-side (no custom client dependency required).
- Spawn star reward opportunities around players on a configurable schedule.
- Keep balancing and reward tuning in config so pack/server owners can iterate quickly.
- Custom animated star entities and custom client rendering.

## Non-Goals (v1)
- Per-biome or per-dimension reward pools beyond simple allow/deny filters.
- Complex anti-cheese systems (basic spawn safety checks only in v1).

## Runtime Flow (Target)
1. Every server tick, loader callback calls `FallingStarRewards.onServerTick(gameTick)`.
2. `StarEventOrchestrator` checks if a cycle should run based on scheduler config.
3. If cycle starts, runtime picks eligible target players/worlds (depending on scope).
4. For each target, runtime finds a safe nearby location within configured radius.
5. Mod spawns an item marker or reward payload and schedules cleanup.
6. Optional announcement is sent to nearby players or globally.

## Planned Common Services
- `StarEventOrchestrator`
  - Tracks next cycle tick and computes interval with jitter.
- `SpawnSelector` (planned)
  - Chooses candidate locations with safety and radius constraints.
- `RewardRoller` 
  - Picks a weighted reward entry and stack size.
- `StarLifecycleService`
  - Creates star drop, handles despawn timeout, and tracks active events.

Current implementation note: active item drops are tracked and explicitly discarded when `claim.lifeTicks` is reached.

## Config Contract (v1)
Current implementation in `MainConfig` introduces these sections:

- `enabled`: master switch.
- `scheduler`
  - `baseIntervalTicks`: base delay between cycles.
  - `intervalJitterTicks`: random extra delay (0..jitter).
  - `maxStarsPerCycle`: cap events started in one cycle.
- `activation`
  - `requireNight`: require nighttime to start events.
  - `requireSurfaceAccess`: skip players in covered areas/caves (planned check).
  - `weatherMode`: `any | clear | rain | thunder`.
- `spawn`
  - `targetScope`: `per_player | global`.
  - `minRadius`, `maxRadius`: distance from target player.
  - `maxLocationAttempts`: attempts to find safe spawn location.
  - `allowWaterSpawns`: whether liquid blocks are valid spawn points.
- `claim`
  - `lifeTicks`: despawn timer for spawned stars.
  - `pickupDelayTicks`: delay before pickup is allowed.
  - `maxActiveDrops`: hard cap for concurrently tracked star drops.
- `announcements`
  - `enabled`: show spawn message.
  - `scope`: `nearby | global`.
  - `spawnMessage`: chat message on spawn.
- `rewards`
  - `poolMode`: currently `weighted`.
  - `entries`: weighted rewards with count range.

### Example Config
```json
{
  "enabled": true,
  "scheduler": {
    "baseIntervalTicks": 2400,
    "intervalJitterTicks": 600,
    "maxStarsPerCycle": 1
  },
  "activation": {
    "requireNight": true,
    "requireSurfaceAccess": true,
    "weatherMode": "any"
  },
  "spawn": {
    "targetScope": "per_player",
    "minRadius": 16,
    "maxRadius": 48,
    "maxLocationAttempts": 12,
    "allowWaterSpawns": false
  },
  "claim": {
    "lifeTicks": 900,
    "pickupDelayTicks": 10,
    "maxActiveDrops": 64
  },
  "announcements": {
    "enabled": true,
    "scope": "nearby",
    "spawnMessage": "A falling star has appeared nearby!"
  },
  "rewards": {
    "poolMode": "weighted",
    "entries": [
      {
        "id": "minecraft:amethyst_shard",
        "weight": 20,
        "minCount": 1,
        "maxCount": 3
      },
      {
        "id": "minecraft:glowstone_dust",
        "weight": 12,
        "minCount": 2,
        "maxCount": 5
      },
      {
        "id": "minecraft:nether_star",
        "weight": 1,
        "minCount": 1,
        "maxCount": 1
      }
    ]
  }
}
```

