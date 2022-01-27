package org.laolittle.plugin

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
        runCatching { return block() }.onFailure { RiskDetector.logger.error(it) }
    }
    return null
}

internal fun ping(ipAddress: String = "www.qq.com"): Long {
    return measureTimeMillis {
        InetAddress.getByName(ipAddress).isReachable(3000)
    }
}