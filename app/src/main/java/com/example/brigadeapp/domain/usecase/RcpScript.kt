package com.example.brigadeapp.domain.usecase

object RcpScript {
    val initialSteps = listOf(
        "Stay calm! First, call the university's emergency number or 123.",
        "Make sure the person is face up on a hard surface.",
        "Place the heel of one hand in the center of the chest, and the other on top.",
        "Extend your elbows and use your body weight to compress the chest 5 to 6 centimeters.",
        "Continue the rhythm of the compressions. Start when I say 'now'."
    )

    const val START_COMPRESSIONS = "Now! Do 30 compressions to the rhythm of the sound."
    const val NEXT_CYCLE = "Continue: another 30 compressions."
    const val STOP_COMPRESSIONS = "Stop compressions. Wait for further instructions or the arrival of help."
}