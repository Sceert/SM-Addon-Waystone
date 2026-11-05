# Verificación final local — 1.0.0

Revisión del 19-09-2026: código funcional sin cambios respecto a la beta.
El usuario confirmó que Waystone funcionó en sus pruebas in-game; no se atribuye
esa confirmación a casos concretos que no especificó.

Build satisfactorio contra el núcleo local actual, cuya versión se obtiene de
su gradle.properties. Pasan 30.000 tiradas de balance, cantidades y determinismo.
JAR final inspeccionado: versión 1.0.0, ID conservado, 13 traducciones y sin clases
del harness de integración. Hash actualizado en SHA256SUMS.txt.

Se deja como versión definitiva local a petición del usuario. No se repitió el
fixture consumido de integración ni se alteró el algoritmo de recompensas. Las
limitaciones transaccionales del ledger del núcleo siguen siendo las descritas
en README. No se publicó ni instaló el JAR en perfiles.

## Evidencia histórica — 1.0.0-v001-beta

Fecha: 2026-09-14. Java 25.0.3; Gradle instalado 9.7.0; Loom 1.17.20.
Minecraft 26.2, Loader 0.19.3, Fabric API 0.156.0+26.2,
Cloth Config 26.2.155, Waystones 26.2.0.12, Balm 26.2.0.8, Shogi 26.2.0.5.
Núcleo local: Skyblock Multi 0.2.4-v042-beta. No se instaló nada en perfiles de juego.

## Verificado

- Build del núcleo y sus verificadores existentes: PASS.
- Build del addon, manifiesto, 13 traducciones y 30.000 tiradas de balance: PASS.
- Arranque dedicado sin Mod Menu/OpenPAC, con una sola copia de cada mod: PASS.
- Descubrimiento de dos features y generación TOML con estados independientes: PASS.
- Cuatro combinaciones ON/OFF con la función real
  `skyblockmulti:island/starter_chest`: PASS.
- Cofre nuevo, callback repetido, marcadores incluso con feature apagada: PASS.
- Suplementos de Trial para UUID desconectado, tiradas estables y callbacks de
  finalización repetidos: PASS.
- Reinicio real del servidor: cofres/decisiones y recompensas pendientes iguales: PASS.
- Cobro con ServerPlayer sintético: entrega objetos registrados, consume una
  recompensa y no la restaura al recargar el ledger: PASS.
- Icono idéntico por SHA-256 al de SM Addon BOP; JAR sin clases de pruebas: PASS.
- `git diff --check` del núcleo sin errores.

La prueba invoca el hook de finalización de Trial; no simula combate completo ni
una conexión de red de cliente. El jugador sintético permite inspeccionar
inventario en el servidor, pero no sustituye una prueba multijugador.
Los avisos OSHI/Perflib de Windows no impidieron el arranque ni las comprobaciones.

Evidencia local: `run-integration-final/verification.txt` y `logs/latest.log`.
La infraestructura de pruebas está en `src/test` y se activa exclusivamente con
`-PintegrationTest runServer`. Son dos ejecuciones consecutivas sobre un mundo
de prueba nuevo: la primera prepara estado, la segunda comprueba persistencia y
consume una recompensa. No repetir sobre mundos de usuarios ni reutilizar ese
fixture ya consumido. Se necesitan las dependencias externas en `runtime-deps`.

## Pruebas manuales pendientes

1. Abrir Cloth Config desde el menú principal y revisar ambos interruptores y
   sus traducciones en un cliente real.
2. Crear isla mediante el flujo completo de selección (incluida dificultad con
   cofre vacío), quitar la Waystone, hacer `/reload`, salir y volver; confirmar
   que no reaparece y que una isla anterior permanece intacta.
3. Completar un Trial real con un participante conectado y otro desconectado;
   reconectar, reiniciar y cobrar en la Bóveda. Revisar doble clic, inventario
   lleno y un Trial de datapack.

## Artefactos

`build/libs/SM-Addon-Waystone-1.0.0-v001-beta.jar`

SHA-256: `727f52f05dc6b99414033be0f97adfd67c780e37d6f70c17f537655011688b26`

Núcleo acompañante: `../SkyblockMulti/build/libs/Skyblock-Multi-0.2.4-v042-beta.jar`

SHA-256: `0b60a97337976b990029f3c556648d3552fa4bd3154ce60775d37b6b0907ce79`
