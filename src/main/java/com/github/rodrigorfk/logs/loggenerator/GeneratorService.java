package com.github.rodrigorfk.logs.loggenerator;

import com.github.javafaker.Faker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.vavr.CheckedRunnable;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class GeneratorService {

    private final Faker faker = new Faker();

    public Future<Void> generate(LogGeneratorApplication.Params params) {
        ExecutorService service = Executors.newFixedThreadPool(params.getThreads());
        ScheduledExecutorService canceller = Executors.newSingleThreadScheduledExecutor();

        final AtomicInteger counter = new AtomicInteger();
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(params.getLogsPerSeconds())
                .timeoutDuration(Duration.ofMillis(25))
                .build();
        RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(config);
        RateLimiter rateLimiter = rateLimiterRegistry
                .rateLimiter("name1");
        CheckedRunnable restrictedCall = RateLimiter
                .decorateCheckedRunnable(rateLimiter, () -> doLog(params, counter));

        CompletableFuture<Void> result = new CompletableFuture<>();
        List<Future<Void>> futures = new ArrayList<>();
        for (int i=0; i<params.getThreads(); i++){
            futures.add(this.pushThread(service, restrictedCall, result));
        }

        canceller.schedule((Callable<Void>) () -> {
            futures.forEach(item -> item.cancel(false));
            service.shutdown();
            canceller.shutdown();
            result.complete(null);
            log.info("execution done {} messages generated", counter.get());
            return null;
        }, params.getDurationSeconds(), TimeUnit.SECONDS);

        return result;
    }

    private Future<Void> pushThread(ExecutorService service, CheckedRunnable restrictedCall, CompletableFuture<Void> result) {
        return (Future<Void>) service.submit(() -> {
            while (!result.isDone()) {
                Try.run(restrictedCall);
            }
        });
    }

    private void doLog(LogGeneratorApplication.Params params, AtomicInteger counter) {

        String message = faker.lorem().characters(params.getMessageSizeMin(), params.getMessageSizeMax());
        switch (params.getLogLevel()){
            case DEBUG:
                log.debug(message);
                break;
            case INFO:
                log.info(message);
                break;
            case WARN:
                log.warn(message);
                break;
            case ERROR:
                log.error(message);
                break;
        }
        counter.incrementAndGet();

    }


}
