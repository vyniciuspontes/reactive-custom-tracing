package com.learning.customreactivetracing

import org.slf4j.Logger
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

@Component
class ApplicationStart(
    private val logger: Logger
) : ApplicationRunner {
    
    override fun run(args: ApplicationArguments?) {
        
        val publisher =
            Flux.fromArray(arrayOf(1, 2, 3, 4, 5, 6, 7))
                .parallel()
                .runOn(Schedulers.parallel())
                .doOnNext {
                    logger.info("$it - Logging with TraceId and SpanId !")
                }.then()
        
        val tracingMonoSingleHeader = TracingMonoOperator(publisher, "80f198ee56343ba864fe8b2a57d3eff7-e457b5a2e4d86bd1-1")
        tracingMonoSingleHeader.subscribe()
    
    /*** Alternative Headers Map
      
        val headers = mapOf(
            "X-B3-TraceId" to "92f198ee56343ba864fe8b2a57d3eff7",
            "X-B3-SpanId" to "05e3ac9a4f6e3b90",
            "X-B3-Sampled" to "1"
        )

        val tracingMonoHeaders = TracingMonoOperator(publisher, headers)
        tracingMonoHeaders.subscribe()*/
    }
}
