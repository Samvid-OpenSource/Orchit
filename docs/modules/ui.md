# Ui module

## Owns

All Compose screens and editor interaction surfaces for phone and tablet.

## Exposes

Kotlin contracts in `org.samvid.orchit.ui`. Editor state and user action contracts for Compose hosts.

## Must not touch

Media processing, persistence implementation, format ownership, audio DSP, or network access.

## Dependencies

The UI consumes public contracts. The engine is the composition root and may wire effects, audio, and text contracts. Feature contracts do not depend on each other; all app behavior remains local and offline.
