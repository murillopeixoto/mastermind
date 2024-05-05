package org.mastermind.model

class EventStream private constructor(
    val identifier: EventId,
    var events: List<GameEvent> = listOf()
) : List<GameEvent> {
    override val size
        get() = events.size

    companion object {
        fun create(identifier: EventId) = EventStream(identifier)
    }

    fun add(vararg event: GameEvent) {
        events = events + event
    }

    override fun get(index: Int) = events[index]

    override fun isEmpty() = events.isEmpty()

    override fun iterator() = events.iterator()

    override fun listIterator() = events.listIterator()

    override fun listIterator(index: Int) = events.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int) = events.subList(fromIndex, toIndex)

    override fun lastIndexOf(element: GameEvent) = events.lastIndexOf(element)

    override fun indexOf(element: GameEvent) = events.indexOf(element)

    override fun containsAll(elements: Collection<GameEvent>) = events.containsAll(elements)

    override fun contains(element: GameEvent) = events.contains(element)
}
