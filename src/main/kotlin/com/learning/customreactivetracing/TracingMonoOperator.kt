package com.learning.customreactivetracing

import brave.Tracing
import brave.propagation.B3Propagation
import brave.propagation.Propagation
import brave.propagation.TraceContextOrSamplingFlags
import org.reactivestreams.Subscription
import org.springframework.cloud.sleuth.api.Span
import org.springframework.cloud.sleuth.api.TraceContext
import org.springframework.cloud.sleuth.brave.bridge.BraveSpan
import reactor.core.CoreSubscriber
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoOperator
import reactor.util.context.Context

class TracingMonoOperator :
    MonoOperator<Void, Void> {
    
    private var newSpan: Span
    
    constructor(source: Mono<out Void>, b3SingleHeader: String) : super(source) {
        this.newSpan = B3HeadersExtractor().getSpan(b3SingleHeader)
    }
    
    constructor(source: Mono<out Void>, b3Headers: Map<String, String>) : super(source) {
        this.newSpan = B3HeadersExtractor().getSpan(b3Headers)
    }
    
    override fun subscribe(actual: CoreSubscriber<in Void>) {
        
        var context = actual.currentContext()
        context = context.delete(TraceContext::class.java)
        context = context.put(TraceContext::class.java, newSpan.context())
        
        val traceCoreSubscriber = TraceCoreSubscriber(actual, context)
        this.source.subscribe(traceCoreSubscriber)
    }
    
    class B3HeadersExtractor {
        
        private val tracing: Tracing = Tracing.current()
        
        private val singleHeader: Propagation.Getter<String, String> =
            Propagation.Getter<String, String> { request, _ -> request }
        
        private val headersGetter: Propagation.Getter<Map<String, String>, String> =
            Propagation.Getter<Map<String, String>, String> { request, key -> request[key] }
        
        fun getSpan(b3Headers: Map<String, String>): Span {
            
            val extract: TraceContextOrSamplingFlags = B3Propagation.get().extractor(headersGetter).extract(b3Headers)
            
            return BraveSpan.fromBrave(tracing.tracer().nextSpan(extract))
        }
        
        fun getSpan(b3SingleHeader: String): Span {
            
            val extract: TraceContextOrSamplingFlags =
                B3Propagation.get().extractor(singleHeader).extract(b3SingleHeader)
            
            return BraveSpan.fromBrave(tracing.tracer().nextSpan(extract))
        }
    }
    
    class TraceCoreSubscriber(private val actual: CoreSubscriber<in Void>, private val context: Context) :
        CoreSubscriber<Void> {
        
        override fun currentContext(): Context {
            return context
        }
        
        override fun onSubscribe(s: Subscription) {
            this.actual.onSubscribe(s)
        }
        
        override fun onNext(t: Void?) {
            this.actual.onNext(t)
        }
        
        override fun onError(t: Throwable?) {
            this.actual.onError(t)
        }
        
        override fun onComplete() {
            this.actual.onComplete()
        }
    }
    
}


