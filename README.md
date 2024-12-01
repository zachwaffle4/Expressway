# Welcome to Expressway!

This library provides additions to the RoadRunner actions and geometry libraries as well as an ActionRunner system for OpMode usage, and helpers for SDK conversions.

## Docs

Docs are here: https://exp.zharel.me/introduction.html
Discord is here: https://discord.gg/ehtyVfcdMv

## Installation

Ensure this block is in the "repositories" block of your TeamCode module build.gradle:
```groovy
    maven {
        url = 'https://repo.dairy.foundation/releases'
    }
```

Add the following lines to the "dependencies" block of that same build.gradle:
```groovy
implementation "page.j5155.roadrunner.actionadditions:core:0.2.0"
implementation "page.j5155.roadrunner.actionadditions:ftc:0.2.0"
```


## Examples

See the examples folder for usage.
