# SamSkyBridge - Island Only (r3 pruned)

This build keeps only the **island/SkyBlock** features and removes:
- Money/Economy, Cheque
- Repair Ticket
- Trading GUI
- Shop GUI
- Lock / Lock Ticket
- Yatu (night vision toggle)
- Pixelmon aliases / Egg steps
- Hunger modifiers

### Main
- `main: com.signition.samskybridge.Main`
- Command: `/섬` (aliases: `/island`, `/is`)

### Notes
- Any Economy/Vault calls have been removed. Upgrade money cost is ignored (only level/XP gating).
- Market GUI/listener removed.
- Resources kept: `blocks.yml`, `config.yml`, `messages_ko.yml`.

### Build (Java 8)
```
mvn -q -e -U -DskipTests package
```
Artifact: `target/SamSkyBridge-IslandOnly-1.0.10-island.jar`
