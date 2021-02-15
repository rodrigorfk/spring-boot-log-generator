package com.github.rodrigorfk.logs.loggenerator;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@SpringBootApplication
@RestController
@Validated
public class LogGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogGeneratorApplication.class, args);
	}

	@Autowired
	private GeneratorService generatorService;

	@PostMapping
	public void generate(@RequestBody @Valid Params params){
		generatorService.generate(params);
	}

	@Data
	public static class Params {
		@NotNull
		private Integer threads = 10;
		@NotNull
		private Integer logsPerSeconds;
		@NotNull
		private Long durationSeconds = 60L*5L;
		@NotNull
		private LogLevel logLevel = LogLevel.INFO;
		@NotNull
		private Integer messageSizeMin = 30;
		@NotNull
		private Integer messageSizeMax = 150;
	}

	public enum LogLevel {
		DEBUG, INFO, WARN, ERROR
	}
}
