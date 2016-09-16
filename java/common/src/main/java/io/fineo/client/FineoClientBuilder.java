package io.fineo.client;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.util.IOUtils;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.asynchttpclient.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;

/**
 * Build a fineo client
 */
public class FineoClientBuilder {

  private AWSCredentialsProvider credentials;
  private String apiKey;
  private String endpoint;
  private String stage = "/prod";
  private ClientConfiguration conf = new ClientConfiguration();

  public FineoClientBuilder withApiKey(String apiKey) {
    this.apiKey = apiKey;
    return this;
  }

  public FineoClientBuilder withCredentials(AWSCredentialsProvider credentials) {
    this.credentials = credentials;
    return this;
  }

  public FineoClientBuilder withEndpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  FineoClientBuilder withStage(String stage) {
    this.stage = stage;
    return this;
  }

  FineoClientBuilder witConfiguration(ClientConfiguration conf) {
    this.conf = conf;
    return this;
  }

  public <T> T build(Class<T> apiClass) {
    if (apiClass == null) {
      throw new IllegalArgumentException("Missing API class");
    }
    if (endpoint == null) {
      throw new IllegalArgumentException("Missing endpoint information");
    }
    ApiClientHandler handler = getHandler(endpoint);
    return build(apiClass, handler);
  }

  static <T> T build(Class<T> apiClass, ApiClientHandler handler) {
    Object proxy = Proxy.newProxyInstance(apiClass.getClassLoader(),
      new Class<?>[]{
        apiClass
      }, handler);
    return apiClass.cast(proxy);
  }

  private ApiClientHandler getHandler(String endpoint) {
    try {
      ApiAwsClient client = new ApiAwsClient(new URL(endpoint), this.stage, conf);
      client.setApiKey(apiKey);
      client.setCredentials(credentials);
      return new ApiClientHandler(client);
    } catch (URISyntaxException | MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private static class Request {
    private Multimap<String, String> parameters = ArrayListMultimap.create();
    private Map<String, String> headers = new HashMap<>();
    private HttpMethodName method;
    private Object content;
    String path;
  }

  static class ApiClientHandler implements InvocationHandler {

    private ObjectMapper mapper = new ObjectMapper();
    private ApiAwsClient client;

    public ApiClientHandler(ApiAwsClient client) {
      this.client = client;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (method.getName().equals("close")) {
        this.client.close();
        return null;
      }

      FineoClientBuilder.Request httpRequest = buildRequest(method, args);
      Future<Response> response;
      switch (httpRequest.method) {
        case PUT:
          response = client.put(httpRequest.path, mapper.writeValueAsBytes(httpRequest.content));
          break;
        case POST:
          response = client.post(httpRequest.path, mapper.writeValueAsBytes(httpRequest.content));
          break;
        case DELETE:
          response = client.delete(httpRequest.path, mapper.writeValueAsBytes(httpRequest.content));
          break;
        case PATCH:
          response = client.patch(httpRequest.path, mapper.writeValueAsBytes(httpRequest.content));
          break;
        case GET:
          Map<String, List<String>> map = httpRequest.parameters.values().stream().collect
            (Collectors.toMap(name -> name, name -> newArrayList(httpRequest.parameters.get(name)
            )));
          response = client.get(httpRequest.path, map);
          break;
        default:
          throw new UnsupportedOperationException(
            "Don't support http method: " + httpRequest.method);
      }

      Type t = method.getReturnType();
      CompletableFuture<Object> future = handleResponse(response, method);
      if (t.getTypeName().equals(CompletableFuture.class.getName())) {
        return future;
      }
      try {
        return future.join();
      } catch (CompletionException e) {
        Throwable cause = e.getCause();
        cause.setStackTrace(new Exception().getStackTrace());
        // throw the actual case back out
        throw cause;
      }
    }

    /**
     * Build a {@link Request} object for the given method.
     *
     * @param method method that annotated with {@link Op}
     * @param args   arguments of the method
     * @return a {@link Request} object
     */
    Request buildRequest(Method method, Object[] args) {
      Request request = new Request();
      Op op = method.getAnnotation(Op.class);
      if (op == null) {
        throw new IllegalArgumentException("Method isn't annotated with Op");
      }
      request.path = op.path();

      Object content = null;
      Annotation[][] annotations = method.getParameterAnnotations();
      int length = annotations.length;
      for (int i = 0; i < length; i++) {
        // content body
        if (annotations[i].length == 0) {
          if (content != null) {
            throw new IllegalStateException("Can't have more than one Body");
          }
          content = args[i];
          continue;
        }

        for (Annotation annotation : annotations[i]) {
          if (annotation instanceof Parameter) {
            processParameter(request, (Parameter) annotation, args[i]);
            break;
          }
        }
      }

      assert !request.path.contains("{") && !request.path.contains("}") :
        "Request path " + request.path + " still contains a template field - they should be "
        + "completely consumed by parameters or an exception thrown from the onNull Strategy";
      request.content = content;
      request.method = HttpMethodName.fromValue(op.method());
      return request;
    }

    CompletableFuture<Object> handleResponse(Future<Response> future, Method method)
      throws Throwable {
      return CompletableFuture.supplyAsync(() -> {
        Response response;
        try {
          response = future.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e);
        }
        int code = response.getStatusCode();
        InputStream content = response.getResponseBodyAsStream();
        try {
          // successful request if code is 2xx
          if (code >= 200 && code < 300) {
            Type t = method.getReturnType();
            JavaType type = mapper.getTypeFactory().constructType(t);
            if (t != void.class && content != null) {
              try (Reader reader = new InputStreamReader(content, StringUtils.UTF8)) {
                return mapper.readValue(reader, type);
              }
            } else {
              // discard response
              if (content != null) {
                content.close();
              }
              return null;
            }
          } else {
            String error = content == null ? "" : IOUtils.toString(content);
            FineoApiClientException e = new FineoApiClientException(error);
            e.setMethod(method.getName());
            e.setStatusCode(response.getStatusCode());
            String requestId = response.getHeaders().get("x-amzn-RequestId");
            if (requestId != null) {
              e.setRequestId(requestId);
            }
            throw e;
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    }

    /**
     * Process an argument annotated with {@link Parameter}.
     *
     * @param request request to be set
     * @param p       annotation
     * @param arg     argument
     */
    void processParameter(Request request, Parameter p, Object arg) {
      String name = p.name();

      switch (p.type()) {
        case HEADER:
          request.headers.put(name, String.valueOf(arg));
          break;
        case PATH:
          String template = format("\\{%s\\}", p.name());
          if (arg == null) {
            arg = p.nullStrategy().onNull.apply(p);
          }
          request.path = request.path.replaceFirst(template, arg.toString());
          break;
        case QUERY:
          if (Map.class.isAssignableFrom(arg.getClass())) {
            Map<String, Object> map = (Map<String, Object>) arg;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
              request.parameters.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
          } else if (Collection.class.isAssignableFrom(arg.getClass())) {
            request.parameters
              .putAll(name, ((Collection<?>) arg).stream().map(Object::toString).collect
                (Collectors.toList()));
          } else {
            request.parameters.put(name, String.valueOf(arg));
          }
      }
    }
  }
}
