# 1.0.1-v001 — Minecraft 26.3

2026-11-04. Actualiza Fabric, Loom y Waystones a 26.3.0.2. Sustituye los JAR
locales de 26.2 en runtime-deps por dependencias de desarrollo para 26.3:
Balm 26.3.0.3, Shogi 26.3.0.3 y Cloth Config 26.3.159.
La receta upstream warp_stone conserva amatista, perla de Ender y oro.

Build, recursos y 30.000 tiradas de balance correctos. Prueba dedicada en
build/run-integration-26.3: cuatro combinaciones de interruptores, cofre
idempotente, tiradas estables, cola offline y deduplicación. Tras reiniciar,
conserva las decisiones y recompensas, entrega una reclamación sintética y
verifica que no reaparece al recargar el ledger.

Con -PintegrationTest -PupgradeSuite, carga además los JAR locales de Nexus
Parties y BOP y sus dependencias 26.3. Valida 11 árboles, 84 recetas BOP,
23 recetas gestionadas del núcleo y dos recargas para desactivarlas/restaurarlas.
Los artefactos locales se derivan de sus gradle.properties. La prueba no se
empaqueta en el JAR distribuido. No se tocaron perfiles ni mundos personales.

Para repetir una prueba desde cero, usa un directorio de integración nuevo;
el archivo verification.txt selecciona la fase de reinicio en ejecuciones posteriores.
