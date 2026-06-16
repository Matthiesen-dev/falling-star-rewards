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
2. `StarEventOrchestrator` checks if a cycle should run based on scheduler config.
3. If cycle starts, runtime picks eligible target players/worlds (depending on scope).
4. For each target, runtime finds a safe nearby location within configured radius.
5. Mod spawns an item marker or reward payload and schedules cleanup.
6. Optional announcement is sent to nearby players or globally.

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

## Config Contract (v1)

### `/config/falling_star_rewards/config.json` - Main config file.

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
  }
}
```

### `/config/falling_star_rewards/announcements.json` - Announcement configs

- `enabled`: show spawn message.
- `scope`: `nearby | global`.
- `spawnMessage`: chat message on spawn.

```json
{
    "enabled": true,
    "scope": "nearby",
    "spawnMessage": "A falling star has appeared nearby!"
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

