# Spacemod

A NeoForge Minecraft mod that introduces custom 3D shader-based celestial rendering.

## Features
- **Custom Shaders**: Utilizes GLSL shaders (`planet.vsh` & `planet.fsh`) for true 3D sphere generation and lighting, featuring fresnel approximations.
- **In-Game Configuration**: Integrates with ImGui for real-time manipulation of the rendered planet's settings (Position, Radius, Color).
- **Core Modding Support**: Built using the NeoForge API, with boilerplate setup ready to expand blocks, items, and creative tabs.

## Getting Started

### Prerequisites
- Java Development Kit (JDK) (ensure it matches the required version for your NeoForge target, typically JDK 21 for modern versions)
- Gradle

### Building
Clone the repository and build using Gradle wrapper:

```bash
git clone <repository-url>
cd spacemod
./gradlew build
```

### Running the Mod
You can run the mod in a development environment via Gradle:
```bash
./gradlew runClient
```

## Structure
- **Main Logic**: `src/main/java/dev/linqfy/spacemod/Spacemod.java` handles event subscription and the `RenderLevelStageEvent` for rendering.
- **Config**: `src/main/java/dev/linqfy/spacemod/Config.java` defines mod configuration.
- **Shaders**: Located under `src/main/resources/assets/spacemod/pinwheel/shaders/program/`.

## License
*Provisional README - gotta fix this later heh*
