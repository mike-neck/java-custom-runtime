/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class App implements Runnable {

  private final String lambdaRuntimeApi;

  private App(final String lambdaRuntimeApi) {
    this.lambdaRuntimeApi = lambdaRuntimeApi;
  }

  @SuppressWarnings("InfiniteLoopStatement")
  public static void main(String[] args) {
    System.out.println("Start lambda");
    final String awsLambdaRuntimeApi = System.getenv("AWS_LAMBDA_RUNTIME_API");
    if (awsLambdaRuntimeApi == null) {
      System.out.println("error AWS_LAMBDA_RUNTIME_API is not available.");
      System.exit(1);
    }
    final App app = new App(awsLambdaRuntimeApi);
    while (true) {
      app.run();
    }
  }

  private static final Consumer<HttpResponse<String>> showHeaders =
      response ->
          response
              .headers()
              .map()
              .entrySet()
              .stream()
              .flatMap(entry -> entry.getValue().stream().map(v -> entry.getKey() + ": " + v))
              .forEach(System.out::println);
  private static final Consumer<HttpResponse<String>> showBody =
      response -> System.out.println(response.body());

  @Override
  public void run() {
    final HttpClient httpClient = HttpClient.newHttpClient();

    final HttpRequest getEvent =
        HttpRequest.newBuilder(
                URI.create("http://" + lambdaRuntimeApi + "/2018-06-01/runtime/invocation/next"))
            .GET()
            .build();

    try {
      final HttpResponse<String> response =
          httpClient.send(getEvent, BodyHandlers.ofString(StandardCharsets.UTF_8));
      final Event<String> event = transformToEvent(response);
      final Event<String> result = event.map(new Handler());
      final HttpRequest request = result.createRequest(lambdaRuntimeApi);
      final HttpResponse<String> resp =
          httpClient.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
      showHeaders.andThen(showBody).accept(resp);
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("failed to run function.", e);
    }
  }

  private static Event<String> transformToEvent(final HttpResponse<String> response) {
    final String requestId =
        response.headers().firstValue("Lambda-Runtime-Aws-Request-Id").orElseThrow();
    return new Event<>(requestId, response.body());
  }
}

class Event<T> {
  private final String id;
  private final T eventData;

  Event(final String id, final T eventData) {
    this.id = id;
    this.eventData = eventData;
  }

  <R> Event<R> map(final Function<? super T, ? extends R> function) {
    final R next = function.apply(eventData);
    return new Event<>(id, next);
  }

  HttpRequest createRequest(final String lambdaRuntimeApi) {
    return HttpRequest.newBuilder(responseUrl(lambdaRuntimeApi))
        .POST(BodyPublishers.ofString(eventData.toString(), StandardCharsets.UTF_8))
        .build();
  }

  private URI responseUrl(final String lambdaRuntimeApi) {
    final String uri =
        "http://" + lambdaRuntimeApi + "/2018-06-01/runtime/invocation/" + id + "/response";
    return URI.create(uri);
  }
}

class Handler implements UnaryOperator<String> {

  @Override
  public String apply(final String s) {
    return "{\"receive\":" + s + "}";
  }
}
