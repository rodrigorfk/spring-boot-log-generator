package com.github.rodrigorfk.logs.loggenerator;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Future;

class GeneratorServiceTest {

    @Test
    void doExecute1(){
        GeneratorService service = new GeneratorService();

        LogGeneratorApplication.Params params = new LogGeneratorApplication.Params();
        params.setDurationSeconds(30L);
        params.setLogLevel(LogGeneratorApplication.LogLevel.INFO);
        params.setLogsPerSeconds(10);
        params.setThreads(10);
        Future<Void> future = service.generate(params);

        Awaitility.await().atMost(Duration.ofSeconds(60)).until(future::isDone);
    }
}