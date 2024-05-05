package org.mastermind.model

data class Feedback(
    val pegs: List<Peg> = emptyList()) {
    enum class Peg { BLACK, WHITE }
}
