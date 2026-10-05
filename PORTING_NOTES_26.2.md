# Port auf Minecraft 26.2 (Fabric only)

## Build
- **Patchouli (Pflicht):** den selbst gebauten Patchouli-26.2-Port (`patchouli-26.2-94-fabric.jar` aus dessen
  `build/libs/`) nach `libs/` kopieren. Im Spiel muss dasselbe Jar ebenfalls installiert sein.
- `./gradlew build` (Java 25, Gradle 9.5.1, Fabric Loom 1.17, keine Mappings – 26.2 ist unobfuskiert)
- Datagen: `./gradlew runDatagen` schreibt nach `src/main/generated` (Models, Item-Definitionen, Rezepte, Loot, Tags, Lang).
  Vor dem ersten Build einmal ausführen, sonst fehlen Models/Rezepte im Jar.
- Versionen stehen in `gradle.properties` (Mod Menu / Jade bitte gegen deine Versionen prüfen).

## Entfernt / deaktiviert
- Forge/NeoForge/Architectury-Multiloader-Struktur entfernt (Architectury API bleibt als Laufzeit-Abhängigkeit).
- Trinkets/Accessories-Integration: `disabled/optional-integrations/`.
- Iris-Integration ist wieder aktiv (`integration/iris/IrisIntegration`): registriert den PBR-Loader für die
  generierten Hamster-Texturen per Reflection – kein Iris-Jar zum Bauen nötig, getestet gegen die API von Iris 1.11.4 (26.2).
  Wichtig: Iris 26.x kennt eine DynamicTexture erst nach einem Aufruf von `getTexture()`; deshalb ruft
  `HamsterTextureUtil` das nach dem Registrieren auf (sonst keine Hamster-PBR und keine leuchtenden Trims).
- Wieder aktiv: Eichel-Ring in der Nebenhand ausblenden (`HeldItemRendererMixin` -> `ItemInHandRenderer#submitArmWithItem`,
  `HeldItemFeatureRendererMixin` -> `ItemInHandLayer#submitArmWithItem`) und Schattenversatz beim Rollen
  (`EntityRenderDispatcherMixin` um `SubmitNodeCollector#submitShadow`, Offset via DataTicket `HamsterRenderer.ROLL_SHADOW_OFFSET`).
- Block-Wackeln ist wieder aktiv: `BlockJiggleRenderer` (LevelExtractionEvents.END_EXTRACTION +
  LevelRenderEvents.COLLECT_SUBMITS, `submitMovingBlock` wie Vanillas FallingBlockRenderer) und
  `BlockEntityRenderDispatcherMixin` (injiziert jetzt in `submit` statt `render`).

- Reiter wieder am animierten Bone `body_child`: `HamsterPassengerLayer` (GeckoLib per-bone render, nur T+R des Bones,
  Sitz-Offset, Vanilla-Yaw neutralisiert). Vanilla-Reiter wird via `EntityRendererRidingTagMixin` (Tag im RenderState)
  + `EntityRenderDispatcherMixin` (Cancel in `submit`) unterdrückt. `disabled/render-mixins` enthält nur noch
  obsolete Dateien (durch Fabric-API-Callbacks ersetzt).

## Bekannte Vereinfachungen
- Partikel aus Animations-Keyframes starten an geschätzter Nasen-/Fußposition.
- Item-Tag `minecraft:flowers` existiert nicht mehr -> Erkennung über Block-Tag `minecraft:flowers`.
- `random_patch`-Feature existiert nicht mehr -> Sonnenblumen/Büsche über SIMPLE_BLOCK + Placement-Regeln.

## Speicherformat
- Entity-/Bett-/Spielerdaten werden über eine NBT-Brücke im alten Layout geschrieben -> bestehende Welten bleiben lesbar.

## Armor Trims
- Hamster-Masken für alle 18 Vanilla-Muster vorhanden (neu: bolt, dune, flow, host, raiser, rib, shaper, silence,
  snout, spire, tide, ward, wayfinder). Muster ohne eigene Maske nutzen die generische `border`-Maske.
- Materialien (inkl. resin) kommen direkt aus den Vanilla-Paletten; Leuchten über LabPBR-Specular-Alpha + Iris.

## Behobene Portierungsfehler
- Besitzer-Vergleiche: `getOwnerReference()` liefert in 26.x eine `EntityReference`, keine UUID.
  `uuid.equals(getOwnerReference())` war deshalb immer false (Flöten-Schulterruf, Teleport-Rettung inkl. Eltern,
  Bett-Rettung beim Schlafen). Jetzt über `PetOwnershipUtil.isOwnedBy(pet, uuid)`.
- Käse-Kaugeräusch über die Consumable-Komponente (`Consumables.defaultFood().sound(...)`) wiederhergestellt.
