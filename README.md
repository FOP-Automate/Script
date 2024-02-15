# FOP Automate Script

This repository contains the script powering the FOP Automate project.

## What is FOP Automate?

Please refer to the [FOP Automate project](https://github.com/FOP-Automate/FOP-Automate-Template)

## Why Java?

Normally you wouln't use java for such a project. The scripts are originally written
using shell / batch scripts. But they were quite quirky and hard to maintain. Also
the scripts were not cross-platform. So I decided to rewrite the scripts in Java.
Normally something like python would be more suitable for such a project, but everyone
needs to install java for FOP anyway, so java is the best choice for this project.

The codebase is not very clean, its ported scripting code after all.

## Building

We build a fat jar using the `shadow` plugin. To build the jar, run the following command:

```bash
./gradlew shadowJar
```

alternatively

```bash
./gradlew build
```

We need a fat jar as we want to distribute the jar as a single file.
