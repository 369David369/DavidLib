package me.david.webapi.server.undertow;

import io.undertow.Undertow;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HttpString;
import me.david.webapi.WebApplicationType;
import me.david.webapi.request.DefaultRequest;
import me.david.webapi.request.Method;
import me.david.webapi.response.CookieKey;
import me.david.webapi.response.Response;
import me.david.webapi.server.AbstractWebServer;
import me.david.webapi.server.WebServer;

import java.io.IOException;
import java.util.Map;

import static me.david.webapi.server.undertow.UndertowUtils.*;

public class UndertowWebServer extends AbstractWebServer implements WebServer {

    private Undertow server;

    public UndertowWebServer(WebApplicationType application) {
        super(application);
    }


    @Override
    public void listen(int port) {
        server = Undertow.builder()
                .addHttpListener(port, "localhost")
                .setHandler(ex -> {
                    ex.getRequestReceiver().receiveFullBytes((exchange, bytes) -> {
                        try {
                            DefaultRequest request = new DefaultRequest(
                                    this,
                                    exchange.getRequestPath(),
                                    exchange.getDestinationAddress().getHostString(),
                                    Method.create(exchange.getRequestMethod().toString()),
                                    isKeepAlive(exchange),
                                    bytes
                            );
                            request.setGet(exchange.getPathParameters());
                            for (Map.Entry<String, Cookie> cookie : exchange.getRequestCookies().entrySet()) {
                                request.getCookies().put(cookie.getKey(), cookie.getValue().getValue());
                            }

                            long start = System.currentTimeMillis();
                            Response response = handleRequest(request);
                            response.finish(request, application);
                            addTotalTime(System.currentTimeMillis() - start);

                            exchange.setStatusCode(response.getResponseCode());
                            for (Map.Entry<String, String> pair : response.getHeaders().entrySet()) {
                                exchange.getResponseHeaders().put(HttpString.tryFromString(pair.getKey()), pair.getValue());
                            }
                            for (Map.Entry<CookieKey, String> cookie : response.getSetCookies().entrySet()) {
                                exchange.getResponseHeaders().add(HttpString.tryFromString("set-cookie"), cookie.getKey().toHeaderString(cookie.getValue()));
                            }
                            send(exchange, response.getRawContent());
                        } catch (Throwable cause) {
                            Response response = handleError(cause);
                            response.finish(null, application);
                            exchange.setStatusCode(response.getResponseCode());
                            for (Map.Entry<String, String> pair : response.getHeaders().entrySet()) {
                                exchange.getResponseHeaders().put(HttpString.tryFromString(pair.getKey()), pair.getValue());
                            }
                            for (Map.Entry<CookieKey, String> cookie : response.getSetCookies().entrySet()) {
                                exchange.getResponseHeaders().add(HttpString.tryFromString("set-cookie"), cookie.getKey().toHeaderString(cookie.getValue()));
                            }
                            try {
                                send(exchange, response.getRawContent());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }).build();
        server.start();
    }

    @Override
    public void shutdown() {
        server.stop();
    }

    @Override
    public boolean isRunning() {
        return server != null && server.getWorker() != null && !server.getWorker().isTerminated() && !server.getWorker().isShutdown();
    }

}
