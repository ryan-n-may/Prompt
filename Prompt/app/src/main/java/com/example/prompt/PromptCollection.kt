package com.example.prompt

import timber.log.Timber

class PromptCollection {
    private var triggers: MutableMap<Trigger, MutableList<Relationship>> = mutableMapOf()
        get() = field
        set(value) = throw IllegalAccessError("Triggers not directly mutable")

    /**
     * Adds a trigger to the prompt collection with no relationships
     */
    public fun addTrigger(trigger: Trigger) {
        if (this.triggers.containsKey(trigger)) {
            Timber.e("Trigger already exists in the record.")
            throw IllegalArgumentException("Trigger already exists in the record.")
        }
        this.triggers.put(trigger, mutableListOf())
    }

    /**
     * Removes a trigger from the PromptCollection and handles unsubscribing all it's relationships
     * from the pub.sub clock stream.
     */
    public fun removeTrigger(trigger: Trigger) {
        if (!this.triggers.containsKey(trigger)) {
            Timber.w("Trigger does not exist in the record, not removing.")
            return
        }

        fun handleUnSub(it: Relationship) {
            it.unSubscribeToClock()
        }
        triggers[trigger]?.forEach(::handleUnSub)
        triggers.remove(trigger)
    }

    /**
     * Adds a relationship to a trigger in the prompt collection and subscribes it to the clock
     * stream.
     */
    public fun addRelationship(
        trigger: Trigger,
        relationship: Relationship,
    ) {
        if (!this.triggers.containsKey(trigger)) {
            Timber.e("Trigger does not exist in the record.")
            throw IllegalArgumentException("Trigger does not exist in the record.")
        }
        relationship.trigger = trigger
        var rels: MutableList<Relationship>? = this.triggers[trigger]
        if (rels == null) {
            Timber.w("Relationships were not found for $trigger.name: creating empty MutableList in its' place.")
            rels = mutableListOf()
        }
        relationship.subscribeToClock()
        rels.add(relationship)
        this.triggers.replace(trigger, rels)
    }

    /**
     * Removes a relationship object from a trigger in the prompt collection and unsubscribes if
     * from the clock stream.
     */
    public fun removeRelationship(relationship: Relationship) {
        val trigger = relationship.trigger
        if (trigger == null) {
            Timber.e("Relationship does not contain backwards trigger reference")
            throw IllegalArgumentException("Relationship does not contain backwards trigger reference")
        }
        if (!this.triggers.containsKey(trigger as Trigger)) {
            Timber.e("Trigger does not exist in the record.")
            throw IllegalArgumentException("Trigger does not exist in the record.")
        }
        val rels: MutableList<Relationship>? = this.triggers[trigger as Trigger]
        if (rels == null) {
            Timber.w("Relationships were not found for $trigger.name: No need to delete relationship")
            return
        }
        (rels as MutableList<Relationship>).remove(relationship)
        relationship.unSubscribeToClock()
        this.triggers.replace(trigger as Trigger, rels as MutableList<Relationship>)
    }
}
