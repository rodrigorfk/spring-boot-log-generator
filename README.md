# spring-boot-log-generator

This is a demo app to generate a constant amount of log to test the log ingestion pipeline, the main intention is to help debug performance issues in the FluentD data pipeline.

## How to use it

1. Deploy to a Kubernetes Cluster
2. Make a port-forward to 8080
3. Make a request like this one:
```shell script
curl --location --request POST 'http://localhost:8080' \
--header 'Content-Type: application/json' \
--data-raw '{
	"logLevel": "INFO",
	"logsPerSeconds": 5000,
	"durationSeconds": 30,
	"messageSizeMin": 100,
	"messageSizeMax": 300
}'
```