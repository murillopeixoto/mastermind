package org.mastermind.model

data class Code(val pegs: List<Peg>) {
    constructor(vararg pegs: String) : this(pegs.map(::Peg))

    data class Peg(val name: String)

    val length: Int get() = pegs.size
}
