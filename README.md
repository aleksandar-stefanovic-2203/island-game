# Island Game

Desktop Java game where the goal is to find the island with the highest average height.

## Overview

- The map is a `30 x 30` grid loaded from `Island_Game/Map.txt`.
- `0` means water, any positive number means land with that height.
- You have `3` attempts to click the correct island.
- The winning island is the one with the highest **average** height (not the highest single tile).

## Project Structure

- `Island_Game/src/gui/` - source code
- `Island_Game/Map.txt` - map used by the game
- `Island_Game/PlayerStatistics.txt` - persisted wins/losses/time values
- `QA.txt` - QA notes and bug history

## Requirements

- Java JDK 8+ installed
- Command line access (`javac` and `java`)

## Run Locally

From repo root:

```bash
cd Island_Game
javac src/gui/*.java
java -cp src gui.Menu
```

Important: run from the `Island_Game` directory so relative files like `Map.txt` and `PlayerStatistics.txt` are found correctly.

## Gameplay

- Click **Play!** in the main menu.
- Click a land cell to guess the island.
- Correct guess: island is marked green and you win.
- Wrong guess: guessed island is marked red and attempts decrease.
- You can restart after a win or a game over.

## Custom Maps

- Edit `Island_Game/Map.txt`.
- Keep exactly `30` lines with `30` space-separated integers per line.
- Value rules:
  - `0` = water
  - `1..1000` = land height

After editing the map, restart the game to load new values.
