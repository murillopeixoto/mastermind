package org.mastermind.model

import java.util.UUID

data class EventId(val id: String) {

    constructor(id: UUID) : this(id.toString())

    companion object {
        fun generate() = EventId(UUID.randomUUID().toString())
    }
}
