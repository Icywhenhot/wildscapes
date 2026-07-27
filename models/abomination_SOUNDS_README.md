# Abomination sounds — mapping reference

All audio the author supplied (in `audio/big frog/`) was converted to **mono
Ogg Vorbis** and placed in `src/main/resources/assets/wildscapes/sounds/abomination/`.
Registered as sound events in `WildscapesSounds`, defined in `sounds.json`.

## Animation sounds (played by GeckoLib sound keyframes)
| Event | .ogg file(s) | Original source | Fires during |
|---|---|---|---|
| `abomination.step` | step_a / step_b / step_c | wScene_0.aw_00000040 / 41 / 48.wav | walk (random pick) |
| `abomination.croak` | croak_eleven / croak_freesound / croak_frog | ElevenLabs croak, freesound croak-41408, freesound frog-85649 | croak (random pick) |
| `abomination.tongue` | tongue_say | Breadbug - Say.wav | tongue — start |
| `abomination.slurp` | slurp / swallow | Emperor Bulblax - Slurp/Swallow.wav | tongue — movement |
| `abomination.jump_charge` | jump_charge | trance2.wav | jump — 1st (charge) |
| `abomination.jump_windup` | jump_windup | wScene_0.aw_00000021.wav | jump — 2nd (windup) |
| `abomination.jump_leap` | jump_leap | jump.wav | jump — 3rd (leap) |
| `abomination.jump_fall` | jump_fall | Wollywog - Fall.wav | jump — 4th (fall) |
| `abomination.jump_land` | jump_land | wScene_0.aw_0000020a.wav | jump — peak/impact |
| `abomination.water_stand` | water_stand | water/stand.wav | swim — on entering water |
| `abomination.water_splash` | water_splash | water/wScene_0.aw_0000027a.wav | swim — on entering water |

The jump is now four animations — `pre_jump` (anticipation) carries charge/windup/leap
sounds, `intermediate_jump` (peak) carries fall/impact; `jump_up`/`jump_down` are silent.

## Gameplay sounds (played from `AbominationEntity`)
| Event | .ogg file(s) | Original source | When |
|---|---|---|---|
| `abomination.ambient` | ambient_charge / ambient_throwup | Emperor Bulblax - Charge / Throw up Secret Safe.wav | passive, any time (`getAmbientSound`) |
| `abomination.hurt` | hurt_bite / hurt_lash / hurt_empress | Water Dumple - Bite, Emperor Bulblax - Tongue Lash, Empress Bulblax.wav | on damage (`getHurtSound`) |
| `abomination.death` | death | death2.wav | on death (`getDeathSound`) |

## Notes
- Converted to **mono** on purpose so Minecraft attenuates them with distance
  (stereo files always play at full volume with no 3D position).
- Step + jump sounds are set to **volume 0.5** in `sounds.json` (per request).
- When each sound plays: `croak` fires on the idle croak (every 10–15s); `step`
  during walking; the five `jump_*` stages during the leap attack; `tongue`/`slurp`
  during the tongue-reel attack; `ambient`/`hurt`/`death` from gameplay hooks.
- To swap a clip: drop a new `.ogg` with the same name here (or edit `sounds.json`).
- The `jump_windup` clip (wScene_0.aw_00000021) is the one the author said you
  could skip if it's too annoying — just remove that entry from `sounds.json`.
