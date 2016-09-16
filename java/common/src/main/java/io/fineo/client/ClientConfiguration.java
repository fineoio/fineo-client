package io.fineo.client;

import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.proxy.ProxyServer;
import org.asynchttpclient.proxy.ProxyServerSelector;

import java.util.concurrent.ThreadFactory;

/**
 * Configure underlying connection properties. Generally the defaults will work fine - this is
 * for folks trying to wring every little bit of performance out of the platform.
 */
public class ClientConfiguration {

  private DefaultAsyncHttpClientConfig.Builder builder = new DefaultAsyncHttpClientConfig.Builder();
  // some defaults that seem be useful
  {
    builder.setConnectTimeout(60);
  }

  DefaultAsyncHttpClientConfig build(){
    return builder.build();
  }

  public ClientConfiguration setMaxConnections(int maxConnections) {
    builder.setMaxConnections(maxConnections);
    return this;
  }

  public ClientConfiguration setProxyServer(
    ProxyServer proxyServer) {
    builder.setProxyServer(proxyServer);
    return this;
  }

  public ClientConfiguration setProxyServerSelector(
    ProxyServerSelector proxyServerSelector) {
    builder.setProxyServerSelector(proxyServerSelector);
    return this;
  }

  public ClientConfiguration setReadTimeout(int readTimeout) {
    builder.setReadTimeout(readTimeout);
    return this;
  }

  public ClientConfiguration setKeepAlive(boolean keepAlive) {
    builder.setKeepAlive(keepAlive);
    return this;
  }

  public ClientConfiguration setSocketSendBufferSize(int soSndBuf) {
    builder.setSoSndBuf(soSndBuf);
    return this;
  }

  public ClientConfiguration setThreadFactory(ThreadFactory threadFactory) {
    builder.setThreadFactory(threadFactory);
    return this;
  }

  public ClientConfiguration setPooledConnectionIdleTimeout(
    int pooledConnectionIdleTimeout) {
    builder.setPooledConnectionIdleTimeout(pooledConnectionIdleTimeout);
    return this;
  }

  public ClientConfiguration setResponseBodyPartFactory(
    AsyncHttpClientConfig.ResponseBodyPartFactory responseBodyPartFactory) {
    builder.setResponseBodyPartFactory(responseBodyPartFactory);
    return this;
  }

  public ClientConfiguration setHandshakeTimeout(int handshakeTimeout) {
    builder.setHandshakeTimeout(handshakeTimeout);
    return this;
  }

  public ClientConfiguration setCompressionEnforced(boolean compressionEnforced) {
    builder.setCompressionEnforced(compressionEnforced);
    return this;
  }

  public ClientConfiguration setUseProxySelector(boolean useProxySelector) {
    builder.setUseProxySelector(useProxySelector);
    return this;
  }

  public ClientConfiguration setSocketReuseAddress(boolean soReuseAddress) {
    builder.setSoReuseAddress(soReuseAddress);
    return this;
  }

  public ClientConfiguration setUseProxyProperties(boolean useProxyProperties) {
    builder.setUseProxyProperties(useProxyProperties);
    return this;
  }

  public ClientConfiguration setHttpClientCodecMaxInitialLineLength(
    int httpClientCodecMaxInitialLineLength) {
    builder.setHttpClientCodecMaxInitialLineLength(httpClientCodecMaxInitialLineLength);
    return this;
  }

  public ClientConfiguration setHttpClientCodecMaxChunkSize(
    int httpClientCodecMaxChunkSize) {
    builder.setHttpClientCodecMaxChunkSize(httpClientCodecMaxChunkSize);
    return this;
  }

  public ClientConfiguration setMaxRedirects(int maxRedirects) {
    builder.setMaxRedirects(maxRedirects);
    return this;
  }

  public ClientConfiguration setTcpNoDelay(boolean tcpNoDelay) {
    builder.setTcpNoDelay(tcpNoDelay);
    return this;
  }

  public ClientConfiguration setConnectTimeout(int connectTimeout) {
    builder.setConnectTimeout(connectTimeout);
    return this;
  }

  public ClientConfiguration setSocketLinger(int soLinger) {
    builder.setSoLinger(soLinger);
    return this;
  }

  public ClientConfiguration setUseNativeTransport(boolean useNativeTransport) {
    builder.setUseNativeTransport(useNativeTransport);
    return this;
  }

  public ClientConfiguration setAcceptAnyCertificate(
    boolean acceptAnyCertificate) {
    builder.setAcceptAnyCertificate(acceptAnyCertificate);
    return this;
  }

  public ClientConfiguration setChunkedFileChunkSize(int chunkedFileChunkSize) {
    builder.setChunkedFileChunkSize(chunkedFileChunkSize);
    return this;
  }

  public ClientConfiguration setSslSessionTimeout(
    Integer sslSessionTimeout) {
    builder.setSslSessionTimeout(sslSessionTimeout);
    return this;
  }

  public ClientConfiguration setThreadPoolName(String threadPoolName) {
    builder.setThreadPoolName(threadPoolName);
    return this;
  }

  public ClientConfiguration setKeepEncodingHeader(boolean keepEncodingHeader) {
    builder.setKeepEncodingHeader(keepEncodingHeader);
    return this;
  }

  public ClientConfiguration setMaxConnectionsPerHost(int maxConnectionsPerHost) {
    builder.setMaxConnectionsPerHost(maxConnectionsPerHost);
    return this;
  }

  public ClientConfiguration setHttpClientCodecMaxHeaderSize(
    int httpClientCodecMaxHeaderSize) {
    builder.setHttpClientCodecMaxHeaderSize(httpClientCodecMaxHeaderSize);
    return this;
  }

  public ClientConfiguration setValidateResponseHeaders(
    boolean validateResponseHeaders) {
    builder.setValidateResponseHeaders(validateResponseHeaders);
    return this;
  }

  public ClientConfiguration setUseOpenSsl(boolean useOpenSsl) {
    builder.setUseOpenSsl(useOpenSsl);
    return this;
  }

  public ClientConfiguration setMaxRequestRetry(int maxRequestRetry) {
    builder.setMaxRequestRetry(maxRequestRetry);
    return this;
  }

  public ClientConfiguration setConnectionTtl(int connectionTtl) {
    builder.setConnectionTtl(connectionTtl);
    return this;
  }

  public ClientConfiguration setDisableZeroCopy(boolean disableZeroCopy) {
    builder.setDisableZeroCopy(disableZeroCopy);
    return this;
  }

  public ClientConfiguration setRequestTimeout(int requestTimeout) {
    builder.setRequestTimeout(requestTimeout);
    return this;
  }

  public ClientConfiguration setShutdownQuietPeriod(int shutdownQuietPeriod) {
    builder.setShutdownQuietPeriod(shutdownQuietPeriod);
    return this;
  }

  public ClientConfiguration setUserAgent(String userAgent) {
    builder.setUserAgent(userAgent);
    return this;
  }

  public ClientConfiguration setShutdownTimeout(int shutdownTimeout) {
    builder.setShutdownTimeout(shutdownTimeout);
    return this;
  }

  public ClientConfiguration setUsePooledMemory(boolean usePooledMemory) {
    builder.setUsePooledMemory(usePooledMemory);
    return this;
  }

  public ClientConfiguration setWebSocketMaxBufferSize(
    int webSocketMaxBufferSize) {
    builder.setWebSocketMaxBufferSize(webSocketMaxBufferSize);
    return this;
  }

  public ClientConfiguration setSslSessionCacheSize(
    Integer sslSessionCacheSize) {
    builder.setSslSessionCacheSize(sslSessionCacheSize);
    return this;
  }

  public ClientConfiguration setSocketReceiveBufferSize(int soRcvBuf) {
    builder.setSoRcvBuf(soRcvBuf);
    return this;
  }

  public ClientConfiguration setWebSocketMaxFrameSize(int webSocketMaxFrameSize) {
    builder.setWebSocketMaxFrameSize(webSocketMaxFrameSize);
    return this;
  }
}
