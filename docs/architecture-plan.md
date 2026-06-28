# Falling Star Rewards - Architecture Plan (Server-Side)

## Goals
- Run entirely server-side (no custom client dependency required).
- Spawn star reward opportunities around players on a configurable schedule.
- Keep balancing and reward tuning in config so pack/server owners can iterate quickly.
- Custom animated star entities and custom client rendering.

## Non-Goals (v1)
- Per-biome or per-dimension reward pools beyond simple allow/deny filters.
- Complex anti-cheese systems (basic spawn safety checks only in v1).

## Commands

- `/fallingstar help` - Show help message.
- `/fallingstar reload` - Reload config files.
- `/fallingstar status` - Show condensed status output.
- `/fallingstar status breif` - Show condensed status output.
- `/fallingstar status full` - Show detailed status output.
- `/fallingstar force` - Force one spawn cycle.
- `/fallingstar force <count>` - Force up to <count> spawn cycles.
- `/fallingstar cleanup` - Remove tracked active drops

## Runtime Flow (Target)
1. Every server tick, loader callback calls `FallingStarRewards.onServerTick(gameTick)`.
2. `StarEventOrchestrator` checks each enabled schedule and decides if that schedule should run a cycle.
3. If a schedule cycle starts, runtime chooses one event preset (`random`, `weighted`, or `rotation`) and then picks eligible target players/worlds (depending on scope).
4. For each target, runtime finds a safe nearby location within configured radius.
5. Mod spawns an item marker or reward payload and schedules cleanup.
6. Optional announcement is sent from the selected event preset to nearby players or globally.
7. Optional event commands are executed as server commands after a successful spawn.

## Event Preset Commands
- Event presets now support a `commands[]` list for post-spawn automation.
- Each command is normalized to remove a leading `/` (if present) before execution.
- Commands run as the dedicated server command source (`RunSlashCommand.asServer`).
- Placeholder expansion is currently supported for `%nearbyPlayer%`, `%spawnPos%`, and `%rewardItem%`.
- Command execution is tied to successful spawn completion in `StarEventService`.

## Breaking Change (Beta)
- Legacy `config.scheduler` and event `activation` fields have been hard-removed.
- All activation logic now lives inside schedule presets under `/config/falling_star_rewards/schedules/*.json`.
- `config.enabledSchedules` controls which schedules are active.
- Legacy `/config/falling_star_rewards/announcements.json` has been removed.
- Announcement behavior now lives in each event preset under `announcement`.

## Planned Common Services
- `StarEventOrchestrator`
  - Tracks next cycle tick and computes interval with jitter.
- `SpawnSelector`
  - Chooses candidate locations with safety and radius constraints.
- `RewardRoller` 
  - Picks a weighted reward entry and stack size.
- `StarLifecycleService`
  - Creates star drop, handles despawn timeout, and tracks active events.

Current implementation note: active item drops are tracked and explicitly discarded when `claim.lifeTicks` is reached.

## Config Contract (v2)

### `/config/falling_star_rewards/config.json` - Main config file.

- `enabled`: master switch.
- `enabledSchedules`: list of active schedule preset IDs.
- `claim`
  - `lifeTicks`: despawn timer for spawned stars.
  - `pickupDelayTicks`: delay before pickup is allowed.
  - `maxActiveDrops`: hard cap for concurrently tracked star drops.

```json
{
  "enabled": true,
  "enablePresetGeneration": true,
  "enabledSchedules": ["base"],
  "claim": {
    "lifeTicks": 900,
    "pickupDelayTicks": 10,
    "maxActiveDrops": 64
  }
}
```

### `/config/falling_star_rewards/events/<id>.json` - Event preset file.

- `enabled`: toggles this event preset.
- `rewardsPresetId`: reward preset ID to use for this event.
- `visualsPresetId`: visuals preset ID to use for this event.
- `commands[]`: optional server commands to run after a successful spawn.
  - Leading `/` is optional.
  - Supports `%nearbyPlayer%`, `%spawnPos%`, and `%rewardItem%` placeholders.
- `spawn`
  - `targetScope`: `per_player | global`.
  - `minRadius`, `maxRadius`: distance from target player.
  - `maxLocationAttempts`: attempts to find safe spawn location.
  - `allowWaterSpawns`: whether liquid blocks are valid spawn points.
- `announcement`
  - `enabled`: toggles spawn announcement for this event preset.
  - `scope`: `nearby | global`.
  - `useActionBar`: whether to send as overlay/action bar instead of chat line.
  - `messages[]`: message pool; one message is selected randomly per spawn.

```json
{
  "enabled": true,
  "rewardsPresetId": "base",
  "visualsPresetId": "base",
  "commands": [
    "tellraw %nearbyPlayer% {\"text\":\"A star landed at %spawnPos% with %rewardItem%!\",\"color\":\"gold\"}",
    "playsound minecraft:entity.experience_orb.pickup player %nearbyPlayer%"
  ],
  "spawn": {
    "targetScope": "per_player",
    "minRadius": 16,
    "maxRadius": 48,
    "maxLocationAttempts": 12,
    "allowWaterSpawns": false
  },
  "announcement": {
    "enabled": true,
    "scope": "nearby",
    "useActionBar": false,
    "messages": [
      "A falling star has appeared nearby!",
      "A falling star has appeared in the sky!",
      "A falling star has appeared in the world!"
    ]
  }
}
```

### `/config/falling_star_rewards/schedules/<id>.json` - Schedule preset file.

- `enabled`: toggles this schedule.
- `baseIntervalTicks`: base delay between schedule cycles.
- `intervalJitterTicks`: random extra delay (0..jitter).
- `maxStarsPerCycle`: cap events started for this schedule cycle.
- `selectionMode`: `random | weighted | rotation`.
- `eventEntries[]`
  - `eventPresetId`: event preset to select from.
  - `enabled`: include/exclude this entry.
  - `weight`: used by `weighted` mode, defaults to `1` when missing/non-positive.
- `conditions`
  - `timeMode`: `any | day | night`.
  - `requireSurfaceAccess`: skip players in covered areas/caves.
  - `weatherMode`: `any | clear | rain | thunder`.
  - `moonPhases`: accepts names and/or numeric IDs (`0..7`), mixed values are de-duplicated.
- `state.rotationCursor`: persisted rotation pointer for deterministic `rotation` mode.

```json
{
  "enabled": true,
  "baseIntervalTicks": 2400,
  "intervalJitterTicks": 600,
  "maxStarsPerCycle": 1,
  "selectionMode": "weighted",
  "eventEntries": [
    { "eventPresetId": "base", "enabled": true, "weight": 3 },
    { "eventPresetId": "rare", "enabled": true, "weight": 1 }
  ],
  "conditions": {
    "timeMode": "night",
    "requireSurfaceAccess": true,
    "weatherMode": "any",
    "moonPhases": ["full_moon", "0", "new_moon", "4"]
  },
  "state": {
    "rotationCursor": 0
  }
}
```


### `/config/falling_star_rewards/rewards.json` - Reward pool configs

- `poolMode`: currently `weighted`.
- `entries`: weighted rewards with count range.
- `entries[].customModelData`: optional custom model data value.
- `entries[].customData`: optional SNBT payload applied to the `custom_data` item component.

```json
{
    "poolMode": "weighted",
    "entries": [
      {
        "id": "minecraft:amethyst_shard",
        "weight": 20,
        "minCount": 1,
        "maxCount": 3,
        "customModelData": 12001,
        "customData": "{star_token:1b}"
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
```

### `/config/falling_star_rewards/visuals.json` - Visual effect configs

- `enabled`: toggles falling-star particle trail above active drops.
- `particlePreset`: `end_rod | ash | glow | firework`.
- `fallDistance`: vertical distance above drop where the trail starts.
- `emissionIntervalTicks`: particle emission cadence.
- `particlesPerEmission`: amount of particles emitted per cadence.
- `impactBurstEnabled`: toggles one-shot particle burst on spawn.
- `impactParticlePreset`: preset used for the landing burst.
- `impactParticleCount`: number of burst particles.
- `impactSpread`: horizontal/vertical spread for burst particles.
- `impactSoundEnabled`: toggles one-shot landing sound on spawn.
- `impactSoundId`: sound event id (for example `minecraft:entity.firework_rocket.twinkle`).
- `impactSoundVolume`: sound volume.
- `impactSoundPitchMin` and `impactSoundPitchMax`: random pitch range per impact.
- `travelSoundEnabled`: toggles low-volume whoosh audio while trail is active.
- `travelSoundId`: sound event id for whoosh loop.
- `travelSoundVolume`: whoosh volume.
- `travelSoundPitchMin` and `travelSoundPitchMax`: random pitch range for whoosh.
- `travelSoundIntervalTicks`: cadence for whoosh playback while active.

```json
{
    "enabled": true,
    "particlePreset": "end_rod",
    "fallDistance": 10,
    "emissionIntervalTicks": 2,
    "particlesPerEmission": 5,
    "impactBurstEnabled": true,
    "impactParticlePreset": "firework",
    "impactParticleCount": 14,
    "impactSpread": 0.35,
    "impactSoundEnabled": true,
    "impactSoundId": "minecraft:entity.firework_rocket.twinkle",
    "impactSoundVolume": 0.8,
    "impactSoundPitchMin": 0.9,
    "impactSoundPitchMax": 1.2,
    "travelSoundEnabled": false,
    "travelSoundId": "minecraft:entity.phantom.flap",
    "travelSoundVolume": 0.12,
    "travelSoundPitchMin": 1.3,
    "travelSoundPitchMax": 1.7,
    "travelSoundIntervalTicks": 12
}
```

