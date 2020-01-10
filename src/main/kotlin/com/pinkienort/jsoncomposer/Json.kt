package com.pinkienort.jsoncomposer

import java.lang.IllegalStateException

interface JsonItem {
    fun build(): String
}

interface JsonContainer : JsonItem {
    fun Item(lake: () -> Any)
}

fun Json(block: JsonContainer.() -> Unit): JsonContainer {
    return JsonObjectContainerImpl(block)
}

fun JsonContainer.Array(block: JsonContainer.() -> Unit): JsonContainer {
    return JsonArrayContainerImpl(block)
}

abstract class BaseJsonContainer(
    private val block: JsonContainer.() -> Unit
) : JsonContainer {

    val elements = mutableListOf<Any>()

    override fun Item(lake: () -> Any) {
        val item = lake()
        elements.add(item)
    }

    fun compose() {
        this.block()
    }

    abstract override fun build(): String
}

class JsonObjectContainerImpl(
    block: JsonContainer.() -> Unit
) : BaseJsonContainer(block) {

    override fun build(): String {
        compose()
        return elements.joinToString(
            separator = ", ",
            prefix = "{ ",
            postfix = " }"
        ) {
            JsonEncoder.encode(it)
        }
    }
}

class JsonArrayContainerImpl(
    block: JsonContainer.() -> Unit
) : BaseJsonContainer(block) {

    override fun build(): String {
        compose()
        return elements.joinToString(
            separator = ", ",
            prefix = "[ ",
            postfix = " ]"
        ) {
            JsonEncoder.encode(it)
        }
    }
}

object JsonEncoder {
    fun encode(obj: Any): String {
        return when (obj) {
            is String -> "\"$obj\""
            is Boolean -> "$obj"
            is Number -> "$obj"
            is JsonItem -> obj.build()
            is Pair<*, *> -> {
                val key = obj.first as String
                val value = encode(obj.second as Any)
                "\"$key\" : $value"
            }
            else -> throw IllegalStateException("cannot encode a type: ${obj::class}")
        }
    }
}


