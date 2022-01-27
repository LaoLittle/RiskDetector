package org.laolittle.plugin

import kotlinx.serialization.json.Json
import java.net.InetAddress
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.system.measureTimeMillis


@OptIn(ExperimentalContracts::class)
internal inline fun <R> retryWhenFailed(limit: Int, block: () -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }
    repeat(limit) {
        runCatching { return block() }
    }
    return null
}

internal fun ping(ipAddress: String = "www.qq.com"): Long {
    return measureTimeMillis {
        InetAddress.getByName(ipAddress).isReachable(3000)
    }
}

internal val Json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    isLenient = true
    allowStructuredMapKeys = true
}