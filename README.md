# Observability tutorial

## Introduction

### Description

In this tutorial, you will learn how to implement observability in a multi-services application.

The application is a simple application based on two services

* **fridge-service**: exposes API that allows to manage stocks of meat and cheese in the fridge. Each `POST` made on
  `/<meats|cheeses>/<kind>` will decrement the stock of the requested good. When none is available, a `404`is returned.
* **burger-maker-service**: exposes a single REST verb allowing to request the confection of a new burger,
  with a selected meat and a selected cheese

Both applications are built in spring web. You can have a look on how things could be implemented with **spring webflux** and **kotlin** in `spring-webflux-kotlin` branch.

### Requirements

* java 17
* maven (tested with 3.8.2)
* docker with its compose plugin (tested with docker 20.10 and compose 2.10)

Also, create a **dedicated** network for the tutorial

```shell
docker network create observability_tutorial
```


## Build

In order to validate your configuration, run the following commands

```shell
mvn clean verify
docker image build --build-arg jar_file=burger-maker-service/target/burger-maker-service-dev-SNAPSHOT.jar --build-arg application_name=burger_maker -t observability-tutorial-burger-maker:latest .
docker image build --build-arg jar_file=fridge-service/target/fridge-service-dev-SNAPSHOT.jar --build-arg application_name=fridge -t observability-tutorial-fridge:latest .
```

One both image are built, run the command

```shell
docker compose up
```

This command should start the two applications.

Run several orders of burger with command

```shell
curl -v  -X POST -H 'Content-Type: application/json'  --data '{"meat": "pork", "cheese": "cheddar"}' http://localhost:8080/burgers/new
```

You're ready :smirk:

## First steps

### Add attribute in context

To begin, let's simply add http request uri and http request method in each service logs.

Take a look at `service-utils` library and add the good profile.

### Add traceId/spanId in console log

Take a look at the `application.yml` of both applications, and enable `sleuth` by setting the right profile.

## Grafana, loki, tempo and prometheus

### Log to file

* create a docker volume named `observability_tutorial`
* modify your `docker-compose.yml` to declare this volume as external and mount it in each service under `/log`
* add the appropriate environment variable and spring profile in `docker-compose.yml` to allow file logging (no json)

Redeploy your application and check by connecting to your running container that file logging is effective.

```shell
# connect to fridge instance
docker compose exec fridge sh
```

### Prepare grafana suite

Configure the `docker-compose.yml` under `grafana` directory.

For grafana:

* set environment variables `GF_AUTH_ANONYMOUS_ENABLED` to `true` and `GF_AUTH_ANONYMOUS_ORG_ROLE` to `Admin`
* mount `./grafana/datasources` to `/etc/grafana/provisioning/datasources`

For Loki: mount `./loki` to `/etc/loki` and `-config.file` to the mounted `config.yml` file location

For promtail: mount the `config.yml` and set the `-config.file` location (or use the default path used by the docker image `/etc/promtail/config.yml`).

You can comment the `prometheus`.

:warning: Do not forget the network configuration

### Start loki and grafana

We will display logs in grafana with loki datasource. In order to use the file logs we made, we will use
promtail to parse our log and send them to loki, and display them in grafana using a loki datasource

In the `docker-compose.yml` file under `grafana` directory, find a way to mount log file generated by both services in the
promtail container and tell promtail to parse them.

[Promtail pipeline configuration documentation](https://grafana.com/docs/loki/latest/clients/promtail/pipelines/) 
list some useful stages. One possible way of achieving this may be to activate `json_log_file` spring profile and do something like

```yaml
pipeline_stages:
  - multiline:
      firstline: "^{"
  - json:
      expressions:
        application: '"service.name"'
        # some other fields we may want to promote
```

Some tricks:

* think about time
* do not forget labels that will get indexed.... Yet think about cardinality

Once it is well configure, run a `docker compose up` in the `grafana` directory and navigate into
[grafana ui](http://localhost:3000),  add a loki data source with the appropriate loki url and see your log
once the data source is created with the explore section.

### Write promtail configuration to parse logs

So far promtail can only try to detect some fields, yet it does not do it very well.
Let's write its configuration to add the label application and read timestamp from the `@timestamp` log line.
Also, we want to rename `traceId`  to `traceID` and `spanId` to `spanID`.


Take a look at the [promtail pipeline documentation](https://grafana.com/docs/loki/latest/clients/promtail/pipelines/).
and more specifically [stages](https://grafana.com/docs/loki/latest/clients/promtail/stages/).

You may want to use `regex`, or find the right spring profile to log into `json`, elastic common schema to be more precise.

Also, know that you can test your promtail configuration by running `promtail` with `--inspect` and `--dry-run` option. This [documentation](https://grafana.com/docs/loki/latest/clients/promtail/troubleshooting/) provides details on troubleshooting commands, which you can run from your docker image `docker compose exec promtail bash`.

### Send traces to tempo

Add spans and trace to grafana. For this, you will need to

* add tempo to our grafana `docker-compose.yml`
* configure tempo to expose a otlp receiver
* add a tempo data source in grafana

On application side, you will need to activate the appropriate profile that activates trace exportation. You will
need to set the `OTEL_EXPORTER_OTLP_ENDPOINT` environment variable for both application.

When done, you can also link your loki entries to your tempo entries using the `jsonData.derivedFields` section
of your loki datasource declarations in grafana. If set accordingly, any log line of your loki datasource will
display a button next to the `traceID` value, allowing you to directly open the tempo trace execution.

You can also do the same thing on tempo side to jump to loki to see the log sources according to the trace displayed.

To achieve this, you may need to add a global label for every trace to do get all the logs of all services.

This can be done by setting the map property `spring.sleuth.otel.resource.attributes`.


### Expose metrics and get them with prometheus

Now we will expose application metrics and poll them with prometheus, and, consult them in grafana.

On application side, the `metrics` profile for **both** applications

This will expose metrics under `actuator/prometheus`.

In order to also illustrate the notion of application, we already added a counter in `BurgerMakerControllerAdvice.unknownFridgeError`:

```java
counter("application.fridge.unknown-error").increment();
```

and another counter when no more food is present in order to create an alert, for example by doing

```java
// error.getErrorBody().parameters() contains a pair of food kind (cheese/meat) and its nature (beed, pork for meat...)
counter("application.fridge.no-more-in-stocks",
        "foodStuff", error.getErrorBody().parameters()[0],
        "kind", error.getErrorBody().parameters()[1]).increment();
```

In `grafana/docker-compose.yml`, add a prometheus service, with image  `bitnami/prometheus:2.41.0`,
on `observability_tutorial` network. Configure it to have a `scrape_configs` made of one job with `metrics_path` set
to `/actuator/prometheus` with the burger-maker and fridge service targets.

Once it is done, generate some traffics to get some metrics.

## OTEL exporter

Let's change our point of view now. Let's suppose you are not able to implement some stuff on application side.
We can do it, using a java agent.

It is already packaged in your docker image under `/opt/opentelemetry-javaagent.jar`.


Remember using environment variables as [described here](https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure)

Configuring the collector can be done using [the documentation](https://github.com/open-telemetry/opentelemetry-collector).

Use the `otel/opentelemetry-collector-contrib:0.69.0` image for the exporter.

### Init collector

The first things to do is to

* add the java option to use the agent
* **remove all profile but `docker` and the one setting the log format you want to use and the profile `requests-attributes-in-mdc`**
* comment promtail in the grafana docker compose
* add a collector service using the image above
* init a configuration for the [otel collector](https://opentelemetry.io/docs/collector/configuration/)
* set environment variables  `OTEL_TRACES_EXPORTER`, `OTEL_METRICS_EXPORTER` and `OTEL_LOGS_EXPORTER` to `"none"` for both `fridge` and `burger_maker` service


### Send metrics with otel exporter


* configure the agent to send the metrics only to the exporter
* configure the exporter to collect the metrics and expose them
* configure prometheus to poll metrics from the collector

### Send traces with otel exporter

Same logic for the traces

### Send logs with otel exporter

Let's use a `filelog` receiver and configure the correct [operator](https://github.com/open-telemetry/opentelemetry-collector-contrib/blob/main/pkg/stanza/docs/operators/README.md#what-operators-are-available)
sequence to extract the right labels.
You may take into mind that the subject is quite fresh. Good luck!

