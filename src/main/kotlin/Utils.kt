package org.laolittle.plugin

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


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