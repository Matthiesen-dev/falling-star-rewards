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
- All activation logic now lives inside schedule presets under `/config/falling_star_rewards/schedules.toml`.
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

### Hard Cutover Notes

- Presets are now managed only via FSConfig-backed TOML config files.
- Legacy preset folders (`events/*.json`, `rewards/*.json`, `visuals/*.json`, `schedules/*.json`) are no longer read.
- No automatic migration/import is performed from legacy JSON presets.
- Default preset IDs are standardized to `base` across all preset categories.

### `/config/falling_star_rewards/server.toml` - Main server settings

- `enabled`: master switch.
- `enabledSchedules`: list of active schedule preset IDs.
- `enablePresetGeneration`: deprecated; retained for one release, currently unused.
- `claim.lifeTicks`: despawn timer for spawned stars.
- `claim.pickupDelayTicks`: delay before pickup is allowed.
- `claim.maxActiveDrops`: hard cap for concurrently tracked star drops.

```toml
[general]
enabled = true
enablePresetGeneration = true # Deprecated/unused
enabledSchedules = ["base"]

[claim]
lifeTicks = 900
pickupDelayTicks = 10
maxActiveDrops = 64
```

### `/config/falling_star_rewards/events.toml` - Event preset list

- `events.eventPresets`: list of event preset objects.
- Each preset includes:
  - `eventId`: unique ID (default preset is `base`).
  - `enabled`.
  - `rewardsPresetId`, `visualsPresetId`.
  - `commands[]`: optional post-spawn commands.
  - `spawn.targetScope`: `per_player | global`.
  - `spawn.minRadius`, `spawn.maxRadius`, `spawn.maxLocationAttempts`, `spawn.allowWaterSpawns`.
  - `announcement.enabled`, `announcement.scope`, `announcement.useActionBar`, `announcement.messages[]`.

### `/config/falling_star_rewards/rewards.toml` - Reward preset list

- `rewards.rewardPresets`: list of reward preset objects.
- Each preset includes:
  - `rewardId`: unique ID (default preset is `base`).
  - `entries[]` with `itemId`, `weight`, `minCount`, `maxCount`.
  - Optional `entries[].customModelData`.
  - Optional `entries[].customData` (SNBT payload for `custom_data`).

### `/config/falling_star_rewards/visuals.toml` - Visuals preset list

- `visuals.visualsPresets`: list of visuals preset objects.
- Each preset includes:
  - `visualsId`: unique ID (default preset is `base`).
  - `enabled`, `particlePreset`, `fallDistance`, `emissionIntervalTicks`, `particlesPerEmission`.
  - `impact`: `burstEnabled`, `particlePreset`, `particleCount`, `spread`, `soundEnabled`, `soundId`, `soundVolume`, `soundPitchMin`, `soundPitchMax`.
  - `travelSound`: `enabled`, `id`, `volume`, `pitchMin`, `pitchMax`, `intervalTicks`.

### `/config/falling_star_rewards/schedules.toml` - Schedule preset list

- `schedules.schedulePresets`: list of schedule preset objects.
- Each preset includes:
  - `scheduleId`: unique ID (default preset is `base`).
  - `enabled`, `baseIntervalTicks`, `intervalJitterTicks`, `maxStarsPerCycle`.
  - `selectionMode`: `random | weighted | rotation`.
  - `eventEntries[]`: `eventId`, `enabled`, `weight`.
  - `conditions`: `timeMode`, `requireSurfaceAccess`, `weatherMode`, `moonPhases[]`.
  - `state.rotationCursor`: persisted cursor for deterministic rotation.

