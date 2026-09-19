# Africa Adventures

A safari tycoon game written in Java, built from scratch without a game engine (plain Java and JavaFX).

This is a group project made for the **Software Engineering** course.

## Key features

- **Build your own safari park:** buy and place plants, ponds, animals, roads, and other items from the shop.
- **Living ecosystem:** herbivores (antelope, zebra) and carnivores (lion, cheetah) roam the park, eat, drink, and age.
- **Tourists and jeeps:** visitors enter through the gate and tour the park in jeeps along the roads you build.
- **Poachers and rangers:** protect your animals by hiring rangers to catch poachers.
- **Day and night cycle:** the park is only partly visible at night.
- **Three difficulty levels:** Easy, Normal and Hard, each with its own goals (time, visitors, money and animal counts).
- **Game speed control** (slow, normal or fast), plus **save and load** support.

## Requirements

- JDK 21 (the project is configured for Java 21)
- Maven (or use the included Maven wrapper, `mvnw`)

## Running the game

```
./mvnw javafx:run
```

On Windows, use `mvnw.cmd javafx:run`. You can also open the project in IntelliJ IDEA and run `Main`.

## Running the tests

```
./mvnw test
```
