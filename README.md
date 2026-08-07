# Aghanim

> Render Dota 2 replay files into high-quality minimap videos.

Aghanim is a Kotlin application that parses Dota 2 replay (`.dem`) files, extracts game state over time, renders the match onto a static minimap, and encodes the result into an MP4 video.

It is designed as a foundation for replay visualization, analytics, and content creation.

---

## Features

* 🎮 Parse Dota 2 replay (`.dem`) files
* 🗺️ Render hero positions on a minimap
* 🏰 Render building positions and state on a minimap
* 🧿 Render ward positions on a minimap
* 🔮 Render rune positions on a minimap
* 👹 Render Roshan position on a minimap
* 🎞️ Generate frame-by-frame replay visualization
* 🎥 Encode rendered frames into an MP4 video using FFmpeg

---

## Prerequisites

Before running AncientForge, ensure you have:

* Java 17 or newer
* FFmpeg installed and available on your system `PATH`

Verify your installation:

```bash
java -version
ffmpeg -version
```

---

## Building

Clone the repository and build the project:

```bash
git clone https://github.com/pepela/aghanim.git
cd aghanim
./gradlew build
```

---

## Running

The application expects two arguments:

1. Path to the Dota 2 replay (`.dem`) file
2. Path to the output directory

```bash
./gradlew run --args="<path-to-replay.dem> <path-to-output-directory>"
```

Example (Linux/macOS):

```bash
./gradlew run --args="/home/user/replays/match.dem /home/user/output"
```

Example (Windows):

```powershell
.\gradlew.bat run --args="C:\Replays\match.dem C:\Output"
```

---

## Output

The output directory will contain:

```text
output/
├── frames/
│   ├── frame_000001.png
│   ├── frame_000002.png
│   └── ...
└── replay.mp4
```

---

## Architecture

```text
Replay (.dem)
      │
      ▼
Replay Parser (Clarity)
      │
      ▼
Game State Timeline
      │
      ▼
Renderer
      │
      ▼
PNG Frames
      │
      ▼
FFmpeg Encoder
      │
      ▼
MP4 Video
```

---

## Technology

* Kotlin
* Gradle
* Clarity 4
* Java2D
* FFmpeg

---

## Planned Features

* Hero movement directions
* Fog of War visualization

---

## Contributing

Contributions, feature requests, and bug reports are welcome.

If you'd like to contribute, feel free to open an issue or submit a pull request.
