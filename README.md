# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

[Server Diagram](https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWOZVYSnfoccKQCLAwwAIIgQKAM4TMAE0HAARsAkoYMhZkzowUAJ4TcRNAHMYABgB0ATkzGoEAK7YYAYjTAqumACUUxpBI6gkgQaK4A7gAWSGAciKikALQAfCzUlABcMADaAAoA8mQAKgC6MAD0DipQADpoAN4ARFWU7gC2KI0ZjTCNADS9uFLh0DJdPf29KG3ASAjjvQC+mBTpsClpbOJZUH4BsVAAFL7+gZS+AI4OkmAAlCus7DAbAkKi4lJZxihgAKrVhxaUHaKHurxEYkkEmeqXUGQAYkg0DIYP9KOCYAovECQZhwe8oc8NqstigsmgHAgEA9qE8XvICZ8QLtBCg0UcgeCwQzIVIYWoFBlhDIUezwTTSfz8byJBlmShWcIHGBIodgMrIly8TyPtCNnDhSilSrxSS6cTWJkfHszlBfBJKVgSZQial4tosgAmMxmOpNHHADo9LITAaNdUqooQADW6GDvSWWmRmjsjmcLmgvG+MAAMhB-GEXFEYnFkNp+c6oFk8oVShUVFIQmg-QGOgMhhIRlAZMtKxXHtsYAh80jDnmC5droF7mbxFKdVCvj92YDqiDuW8Zfy4YjkajqhisTBWxxpbrXZt2FlOfIJXTUmfF-LFRq1RqtY++frBYaYMbNbes6qIkFprFk45IvajoSi6IFumWGBej6frNGugadDAIa9GGEaRFGsZoPGEzLOgMgpvYTiuHYKDoLm+aOMwRbRLEmDukkqSVtWfAAKI5txRTcWU5QNhITZ1Lh+HoDB6wcQOZJDvRypjopYCTjcM5yfOm66kuYD-m+kYxugG4Que34IkiKISUZYRHtZBHatphJwZeg72VJn56rCgp8CgCA-Ko7loHec4uZxdHGAxUEIE6lrrBsbFgEhvoNCRya2BR6a7CiOb7DAADi6HQkxJasQhzCgTQVY5Pl-FCcY6HiRqknBX2lVXkO+xjvsanTiFwEPgunwwN8emvkFJmMl5AoWXuQWYl4QWOaZzntW5zU2f1WkrcNo25YEhyTVu5m7lZVIjehai3p5F5AVkhUdKamlhXF4E9ZI0FtQl5XJX6vQNR0EhdDkTQAygACSfDAwAjJ6ADMAAsYbFrE7IguMkyNAoCCgNGaPoRjYZgwAcuh3SNIsMAlGlZEZWmrjYA4UDYP58AsrEBWXSVLGJf2YE5AUxT1Y17gbQRfok+hvZxXzpJZM+sSHHA7MoL1dxbfSTm7T8+kTctU3boKp0wPNdlix5Q16mt8lLUB21TbpD0oIcksdEdZnebNKJO4eXiu6elu3XJ5KUtSduaztsoKygTuHD78ju85nu-vHQga5VVrKwqsTRbFawXolv0NP96GQzD8MIzANPkfTLi6H5w7hDAABSEBIpzHSuNjuNlQkFWyfzeS-HW5Rg01hniw0LPAPXUBwBAw5QAMYOQ9L+fW1kABWbdoIcreQSgVzqRrg1a7KI06+N5toInX6e8bpuLdf+vHRvJvP+Hp+R476Eu+hpNuxfh7GaxtU7AAWhdDoACA5nyDrSQcYCT4wE8hkbeSJY5gNvtNOEvxsByA5mAmA4QYiRBgGgFATdp6z3TgPKqWR95oFztJAuP0YDehSvUaudNKIuDsDPeA-k8Am2wCzQgwRQgRGYqWPussrTZB4nxASQl1DMLfiAQRRxEAKjtIfKc6tP7IMtlkMU8gLDqO0YdJBKC-zvlMeYvAliDHWLAWYjRlibrmV8v5DmwBzpgyumnDxnsvEBRNudIEATgBAKTjNEJPjzq4UidEr8b8KRUhoZsTOGimFfXgn3IunCkxkSAA)
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
