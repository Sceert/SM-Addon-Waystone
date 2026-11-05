# SM Addon: Waystone

Addon independiente de Skyblock Multi para Minecraft Java **26.3 / Fabric**.
Versión para 26.3: **1.0.1**; conserva el comportamiento de la beta probada
in-game por el usuario. Ver revisión final en [docs/verification.md](docs/verification.md).
Icono compartido con SM Addon: BOP, autor Treecs. Código propio bajo MIT.

## Instalación

Instala el JAR en `mods` del servidor y de los clientes, junto con:

- Java 25; Fabric Loader >= 0.20.1; Fabric API 0.165.2+26.3.
- Skyblock Multi **0.2.4-v042-beta o posterior con AddonRewardApi v1**.
  La base solicitada v041 contiene los interruptores, pero carece de los hooks;
  por eso esta entrega incluye una revisión genérica del núcleo. El manifiesto
  exige v042 para impedir arranques incompatibles con el JAR original v041.
- [Waystones](https://github.com/TwelveIterations/Waystones/tree/26.3)
  Fabric 26.3.0.2, y sus dependencias Balm y Shogi para 26.3.
- Cloth Config 26.3.159. Mod Menu es opcional y solo facilita abrir la pantalla.

No se distribuye Waystones dentro del addon. Waystones pertenece a
BlayTheNinth / Twelve Iterations y mantiene su propia licencia (All Rights
Reserved). Este proyecto no copia su código, texturas ni objetos.

## Configuración

Skyblock Multi descubre ambas funciones mediante
`custom.skyblockmulti.addon_features` y muestra dos interruptores separados en
**Addons** de su pantalla Cloth Config. El núcleo genera el archivo también
en servidores dedicados sin Mod Menu:

`config/skyblockmulti/addons/skyblockmulti_waystones.toml`

```toml
[features]
"skyblockmulti_waystones:starter_waystone" = false
"skyblockmulti_waystones:infernal_trial_loot" = true
```

Edita el archivo con el servidor detenido. En cliente integrado usa la pantalla
de Skyblock Multi antes de abrir el mundo. En multijugador, la configuración del servidor gobierna las
recompensas. Las decisiones existentes y recompensas pendientes no se recalculan.

| Cofre | Loot | Resultado |
|---|---|---|
| OFF | OFF | Sin aportes nuevos |
| ON | OFF | Una Waystone por isla nueva |
| OFF | ON | Solo materiales personales de Trial (predeterminado) |
| ON | ON | Ambas funciones |

### Waystone en cofre inicial

Tipo `starter_chest`, desactivado por defecto. Resuelve el objeto registrado a
través de la API pública `WaystoneType.get(waystones:andesite_waystone)` y su
bloque registrado. Añade exactamente una unidad en un espacio vacío. Si la
dificultad no incluye cofre, crea uno con esa unidad. No revisita islas existentes.
La inicialización guarda un marcador por isla incluso cuando el interruptor está
apagado. Reconexiones, recargas y reinicios normales no vuelven a entregar nada.

### Loot en Trials Infernales

Tipo `challenge_loot`, activado por defecto e independiente del cofre.
Cada fila es una tirada independiente, personal, al completar el Trial:

| Material de warp_stone | Nivel 1 | Nivel 2 | Nivel 3 |
|---|---|---|---|
| Amatista | 35%, 1 | 50%, 1–2 | 65%, 1–3 |
| Perla de Ender | 15%, 1 | 25%, 1 | 35%, 1–2 |
| Lingote de oro | 20%, 1 | 30%, 1 | 40%, 1 |

Son materiales de la [receta oficial 26.3](https://github.com/TwelveIterations/Waystones/blob/26.3/common/src/generated/resources/data/waystones/recipe/warp_stone.json).
No se entrega una Waystone completa ni una warp_stone completa. Puede no salir
ningún material. Los Trials de datapack sin nivel numérico usan nivel 1.

Los materiales se guardan con la recompensa personal de la Bóveda Infernal;
los participantes desconectados conservan el derecho a cobrarlos. Desactivar el
loot afecta a futuras finalizaciones, no borra recompensas ganadas. Un segundo
callback de finalización no genera otra recompensa. Los suplementos de Trials
de datapack se cobran en la misma Bóveda; su loot original mantiene su conducta.

## API y persistencia

Sin mixins ni clases internas de Waystones. El núcleo no depende del addon ni de
Waystones. Contrato genérico: `SkyblockMulti/docs/datapacks/addon-rewards-v1.md`.
El nuevo formato lee recompensas antiguas, conserva copias `.bak` y sustituye
archivos de forma atómica. No vuelvas a un núcleo anterior con recompensas nuevas
pendientes. Haz copia del mundo antes de actualizar.

La garantía cubre callbacks repetidos, desconexión y reinicio limpio. Un cierre
forzado del proceso entre guardado de inventario y ledger no es una transacción
atómica; esta limitación existe también en la Bóveda base.

## Desarrollo

Mantén este proyecto junto a `SkyblockMulti`. Primero compila el núcleo; después
ejecuta `C:\Gradle\bin\gradle.bat build --stacktrace` aquí. El build lee la versión
del núcleo desde su `gradle.properties`; no empaqueta ese JAR ni Waystones.
Consulta `docs/internal-revisions.md` y `docs/verification.md`.
