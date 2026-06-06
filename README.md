# Dama Game

A JavaFX desktop implementation of Dama/checkers with local play, a minimax-based AI opponent, and online multiplayer through a lightweight socket server.

The project is organized as a Maven Java 17 application. The graphical client runs with JavaFX, while the online mode uses a separate TCP relay server that pairs two players through a short session code.

## Table of Contents

- [Features](#features)
- [Gameplay Overview](#gameplay-overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Requirements](#requirements)
- [Running the Desktop App](#running-the-desktop-app)
- [Running Online Multiplayer Locally](#running-online-multiplayer-locally)
- [Environment Variables](#environment-variables)
- [Build and Test Commands](#build-and-test-commands)
- [Docker and Railway Deployment](#docker-and-railway-deployment)
- [Architecture](#architecture)
- [Game Rules Implemented](#game-rules-implemented)
- [AI Details](#ai-details)
- [Multiplayer Protocol](#multiplayer-protocol)

## Features

- JavaFX desktop interface with a menu screen and an 8x8 board.
- Three game modes:
  - Player vs AI.
  - Local Player vs Player on the same computer.
  - Online multiplayer using host/join session codes.
- Visual move assistance:
  - Selected pieces are highlighted.
  - Moves are shown with green dots.
- Forced capture rule: if a capture is available for the current player, non-capturing moves are blocked.
- Piece promotion to king when a piece reaches the opponent's back row.
- AI opponent using minimax with alpha-beta pruning.
- Socket-based online relay server.
- Dockerfile for deploying the multiplayer server.
- Railway configuration for server deployment.
- JUnit tests for the core board model.

## Gameplay Overview

When the application starts, the main menu lets you choose one of three modes.

### Player vs AI

The human player controls red pieces. The AI controls black pieces. After the human makes a valid move, the AI chooses a move with a depth-limited minimax search.

### Player vs Player

Two players share the same application window and alternate turns locally.

### Online Multiplayer

Online multiplayer needs the relay server to be running.

One player hosts a game and receives a 6-character session code. The second player joins with that code. After both clients are paired, each player sees themselves as the red side locally; moves are mirrored before being sent to the opponent so both players can play from their own perspective.

## Tech Stack

- Java 17
- JavaFX 21
- Maven
- JUnit 5
- Java sockets for networking
- Docker for server deployment
- Railway deployment configuration

## Project Structure

```text
dama-game/
├── pom.xml
├── Dockerfile
├── railway.json
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/dama/
│   │   │   ├── Main.java
│   │   │   ├── controller/
│   │   │   │   ├── AIPlayer.java
│   │   │   │   ├── GameController.java
│   │   │   │   └── GameType.java
│   │   │   ├── model/
│   │   │   │   ├── Board.java
│   │   │   │   ├── Color.java
│   │   │   │   ├── GameState.java
│   │   │   │   ├── Piece.java
│   │   │   │   └── Position.java
│   │   │   ├── network/
│   │   │   │   ├── Client.java
│   │   │   │   ├── ClientHandler.java
│   │   │   │   ├── Move.java
│   │   │   │   ├── Server.java
│   │   │   │   ├── Session.java
│   │   │   │   └── SessionManager.java
│   │   │   └── view/
│   │   │       ├── BoardView.java
│   │   │       ├── CheckersView.java
│   │   │       ├── GameWindow.java
│   │   │       └── MenuView.java
│   │   └── resources/
│   │       └── damaIcon.png
│   └── test/
│       └── java/com/dama/model/
│           └── BoardTest.java
```

## Requirements

Install the following before running the project:

- JDK 17 or newer.
- Maven 3.8+.
- A graphical desktop environment for the JavaFX client.
- Docker, only if you want to run or deploy the multiplayer server in a container.

Check your Java and Maven versions:

```bash
java -version
mvn -version
```

## Running the Desktop App

From the project root:

```bash
mvn clean javafx:run
```

This starts the JavaFX client and opens the main menu.

## Running Online Multiplayer Locally

Online multiplayer requires one server process and two client app instances.

### 1. Start the server

In one terminal:

```bash
mvn clean package -DskipTests
java -cp target/dama-game-1.0-SNAPSHOT.jar com.dama.network.Server
```

By default, the server listens on port `1234`.

### 2. Start the first client

In a second terminal:

```bash
mvn javafx:run
```

Choose `Multiplayer`, then `Host a Game`. The app displays a session code.

### 3. Start the second client

In a third terminal:

```bash
mvn javafx:run
```

Choose `Multiplayer`, enter the host's code, and click `Join`.

## Environment Variables

The server and client can be configured with environment variables.

### Server

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `1234` | TCP port used by `com.dama.network.Server`. |

Example:

```bash
PORT=8080 java -cp target/dama-game-1.0-SNAPSHOT.jar com.dama.network.Server
```

### Client

| Variable | Default | Purpose |
| --- | --- | --- |
| `SERVER_HOST` | `localhost` | Hostname or IP address of the multiplayer server. |
| `SERVER_PORT` | `1234` | TCP port of the multiplayer server. |

Example:

```bash
SERVER_HOST=127.0.0.1 SERVER_PORT=8080 mvn javafx:run
```

## Build and Test Commands

Compile the project:

```bash
mvn compile
```

Run tests:

```bash
mvn test
```

Build the application jar:

```bash
mvn clean package
```

Build without tests:

```bash
mvn clean package -DskipTests
```

Run the multiplayer server from the built jar:

```bash
java -cp target/dama-game-1.0-SNAPSHOT.jar com.dama.network.Server
```

Run the JavaFX client:

```bash
mvn javafx:run
```

## Docker and Railway Deployment

The Dockerfile builds and runs the multiplayer server, not the JavaFX desktop client.

Build the server image:

```bash
docker build -t dama-server .
```

Run the server container locally:

```bash
docker run --rm -p 1234:1234 dama-server
```

Run on a custom port:

```bash
docker run --rm -e PORT=8080 -p 8080:8080 dama-server
```

When the server is deployed remotely, point the desktop client to it:

```bash
SERVER_HOST=your-server-host SERVER_PORT=your-server-port mvn javafx:run
```

The included `railway.json` is configured for Railway deployment with an `ON_FAILURE` restart policy. Railway injects the `PORT` variable automatically, and the Dockerfile uses that variable when starting `com.dama.network.Server`.

## Architecture

The application follows a simple model-view-controller style.

### Model

The `model` package contains the game state and rules:

- `Board` stores the 8x8 piece array, current player, legal move generation, captures, promotion, resets, and win detection.
- `Piece` stores color, position, and king status.
- `Position` stores board coordinates and provides playable-square and mirrored-position helpers.
- `Color` identifies red and black pieces.
- `GameState` tracks whether the game is in progress, won, or drawn.

### View

The `view` package contains the JavaFX interface:

- `MenuView` displays the main menu, multiplayer host screen, join screen, session code, and waiting states.
- `GameWindow` builds the main game layout with the board and sidebar.
- `BoardView` draws tiles, pieces, kings, selected squares, move dots, and capture rings.
- `CheckersView` defines the methods the controller uses to update the UI.

### Controller

The `controller` package coordinates user actions and game flow:

- `GameController` receives board clicks, validates turn ownership, applies moves, updates the UI, schedules AI moves, and handles remote moves.
- `AIPlayer` searches for the best black move using minimax with alpha-beta pruning.
- `GameType` identifies the active game mode.

### Network

The `network` package provides online multiplayer:

- `Server` accepts TCP clients and gives each connection to a `ClientHandler`.
- `ClientHandler` reads session requests and move messages from one client.
- `SessionManager` creates host codes, pairs players, tracks sessions, and forwards moves.
- `Session` stores the two paired clients and sends start messages.
- `Client` connects from the JavaFX app, requests a new or joined session, listens for messages, and sends moves.
- `Move` serializes and parses moves in the format `x y , x y`.

## Game Rules Implemented

The current game logic implements these rules:

- The board is 8x8.
- Only playable dark squares are used.
- Red pieces start on rows 5 through 7.
- Black pieces start on rows 0 through 2.
- Red moves upward toward row 0.
- Black moves downward toward row 7.
- Regular pieces move one diagonal square forward.
- Captures jump two diagonal squares over an opponent piece.
- Captures are mandatory when available.
- If any piece of the current color can capture, other pieces cannot make normal moves.
- Kings can move diagonally forward and backward by one square.
- Kings can capture diagonally forward and backward.
- Red pieces are promoted on row 0.
- Black pieces are promoted on row 7.
- A player wins when the opponent has no pieces left.

Current limitation: the board applies one move at a time and switches turn after that move. Multi-jump capture chains are not currently implemented.

## AI Details

The AI is implemented in `AIPlayer`.

- AI color: black.
- Human color in AI mode: red.
- Search depth: 4 plies.
- Algorithm: minimax with alpha-beta pruning.
- Evaluation factors:
  - Piece value.
  - King value.
  - Advancement toward promotion.
  - Small center-column bonus.

The AI runs on a background JavaFX `Task` so the UI stays responsive while it thinks.

## Multiplayer Protocol

The multiplayer server uses line-based TCP messages.

### Session messages

| Message | Meaning |
| --- | --- |
| `SESSION:NEW` | Client wants to host a new game. |
| `SESSION:JOIN:<code>` | Client wants to join an existing hosted game. |
| `SESSION:CODE:<code>` | Server sends the generated code to the host. |
| `WAITING` | Server tells a client it is waiting for an opponent. |
| `SESSION:INVALID` | Server rejects an invalid or expired code. |
| `START:YOU` | Client starts and has the first local turn. |
| `START:OPPONENT` | Client starts and waits for the opponent. |

### Move messages

Moves are sent as:

```text
fromX fromY , toX toY
```

Example:

```text
5 2 , 4 3
```

In online mode, moves are mirrored before being sent, allowing both players to see themselves as red on their own board.



