package com.zenika.tutoriels.observabilitydemo.logger.utils

import kotlinx.coroutines.ThreadContextElement
import org.slf4j.MDC
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal
import reactor.util.context.Context
import reactor.util.context.ContextView
import java.util.function.Consumer
import kotlin.coroutines.CoroutineContext


data class RequestInfo(val method: String, val path: String)


class RequestInfoContext(private val requestInfo: RequestInfo) : ThreadContextElement<RequestInfo> {

    override val key: CoroutineContext.Key<RequestInfoContext> = object : CoroutineContext.Key<RequestInfoContext> {}

    override fun restoreThreadContext(context: CoroutineContext, oldState: RequestInfo) {
    }

    override fun updateThreadContext(context: CoroutineContext): RequestInfo {
        MDC.put("request.method", this.requestInfo.method)
        MDC.put("request.uri", this.requestInfo.path)
        return this.requestInfo
    }
}

@AutoConfiguration
class ServiceLoggerAutoConfiguration {

    @Bean
    @Profile("requests-attributes-in-mdc")
    fun addRequestInContext(): WebFilter {
        return WebFilter { exchange: ServerWebExchange, chain: WebFilterChain ->
            chain.filter(exchange)
                    .contextWrite(Context.of(REQUEST_INFO_CONTEXT, RequestInfo(exchange.request.method!!.name,
                            exchange.request.path.pathWithinApplication().value())))
        }
    }

    companion object {
        const val REQUEST_INFO_CONTEXT = "request-info-context"
        fun <T, R> withRequestInfoInMdc(
                mapping: (T) -> Mono<R>): (T) -> Mono<R> {
            return { t: T ->
                Mono.deferContextual { context: ContextView ->
                    return@deferContextual withContextInfo({ context }) { mapping.invoke(t) }
                }
            }
        }

        fun <T> withRequestInfoInMdcForLog(logStatement: (T) -> Unit): Consumer<Signal<T>> {
            return Consumer { signal: Signal<T> ->
                if (!signal.isOnNext) return@Consumer
                withContextInfo({Context.of(signal.contextView)}) { logStatement.invoke(signal.get()!!) }
            }
        }

        private fun <RESULT> withContextInfo(contextSupplier: () -> ContextView, execution: () -> RESULT): RESULT {
            return contextSupplier.invoke().getOrEmpty<RequestInfo>(REQUEST_INFO_CONTEXT)
                    .map { info: RequestInfo ->
                        MDC.putCloseable("request.method", info.method)
                                .use {
                                    MDC.putCloseable("request.uri", info.path)
                                            .use { execution.invoke() }
                                }
                    }
                    .orElseGet { execution.invoke() }
        }
    }
}