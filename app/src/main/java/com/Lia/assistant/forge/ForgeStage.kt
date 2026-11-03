package com.Lia.assistant.forge

enum class ForgeStage(val label: String, val progress: Float) {
    INITIALIZING("Initializing Forge Core", 0.05f),
    CONCEPT("Ideating Architecture & 3D Space", 0.20f),
    STRUCTURE("Forging Semantic HTML & Layout", 0.40f),
    STYLING("Infusing Glassmorphism & Visuals", 0.65f),
    INTERACTION("Igniting Three.js & Micro-Interactions", 0.85f),
    FINALIZING("Polishing Responsive Experience", 0.95f),
    READY("Website Successfully Generated", 1.0f)
}
