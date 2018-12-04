/*
 * Copyright 2018 Shinya Mochida
 *
 * Licensed under the Apache License,Version2.0(the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,software
 * Distributed under the License is distributed on an"AS IS"BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class LambdaApp {

  @SuppressWarnings("InfiniteLoopStatement")
  public static void main(String[] args) {
    System.out.println("Start lambda");
    final String awsLambdaRuntimeApi = System.getenv("AWS_LAMBDA_RUNTIME_API");
    if (awsLambdaRuntimeApi == null) {
      System.out.println("error AWS_LAMBDA_RUNTIME_API is not available.");
      System.exit(1);
    }
    System.out.println(awsLambdaRuntimeApi);
    final HttpClient client = HttpClient.newHttpClient();
    System.out.println("client prepared.");
    while (true) {
      final URI uri = URI.create("http://" + awsLambdaRuntimeApi + "/2018-06-01/runtime/invocation/next");
      System.out.println("uri : " + uri);
      final HttpRequest getEvent = HttpRequest.newBuilder(uri).GET().build();
      try {
        final HttpResponse<String> response =
            client.send(getEvent, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        final String requestId =
            response.headers().firstValue("Lambda-Runtime-Aws-Request-Id").orElseThrow();
        final String body = response.body();
        System.out.println(body);
        final String payload = "{\"receive\":" + body + "}";
        final URI resultUrl =
            URI.create(
                "http://"
                    + awsLambdaRuntimeApi
                    + "/2018-06-01/invocation/"
                    + requestId
                    + "/response");
        final HttpRequest request =
            HttpRequest.newBuilder(resultUrl)
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();
        final HttpResponse<String> result =
            client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        System.out.println(result.statusCode());
        System.out.println(result.body());
      } catch (InterruptedException | IOException e) {
        e.printStackTrace();
      }
    }
  }
}
