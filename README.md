# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=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

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
