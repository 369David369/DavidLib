package me.david.webapi.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import lombok.AllArgsConstructor;
import me.david.webapi.request.Request;
import me.david.webapi.response.CookieKey;
import me.david.webapi.response.Response;
import me.david.webapi.server.AbstractWebServer;

import java.util.Map;

import static me.david.webapi.server.netty.NettyUtils.*;

@ChannelHandler.Sharable
@AllArgsConstructor
public class WebServerHandler extends SimpleChannelInboundHandler {

    private NettyWebServer server;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest nettyRequest = (FullHttpRequest) msg;
            if (!nettyRequest.decoderResult().isSuccess()) {
                throw new AbstractWebServer.BadRequestException("Netty Decoder Failed");
            }
            Request request = new NettyRequest(server, nettyRequest, ctx);

            long start = System.currentTimeMillis();
            Response response = server.handleRequest(request);
            response.finish(request, server.getApplication());
            server.addTotalTime(System.currentTimeMillis() - start);

            ByteBuf content = Unpooled.buffer(response.getRawContent().available());
            content.writeBytes(response.getRawContent(), response.getRawContent().available());

            DefaultFullHttpResponse nettyResponse = new DefaultFullHttpResponse(
                    convertHttpVersion(response.getHttpVersion()),
                    HttpResponseStatus.valueOf(response.getResponseCode()),
                    content
            );
            for (Map.Entry<String, String> pair : response.getHeaders().entrySet()) {
                nettyResponse.headers().set(pair.getKey(), pair.getValue());
            }
            for (Map.Entry<CookieKey, String> cookie : response.getSetCookies().entrySet()) {
                nettyResponse.headers().add(HttpHeaderNames.SET_COOKIE, cookie.getKey().toHeaderString(cookie.getValue()));
            }
            ChannelFuture future = ctx.writeAndFlush(nettyResponse);
            if (!request.isKeepAlive()) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Response response = server.getErrorHandler().handleError(cause);
        response.finish(null, server.getApplication());
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(response.getRawContent(), response.getRawContent().available());
        DefaultFullHttpResponse nettyResponse = new DefaultFullHttpResponse(
                convertHttpVersion(response.getHttpVersion()),
                HttpResponseStatus.valueOf(response.getResponseCode()),
                byteBuf
        );
        for (Map.Entry<String, String> pair : response.getHeaders().entrySet()) {
            nettyResponse.headers().set(pair.getKey(), pair.getValue());
        }
        for (Map.Entry<CookieKey, String> cookie : response.getSetCookies().entrySet()) {
            nettyResponse.headers().add(HttpHeaderNames.SET_COOKIE, cookie.getKey().toHeaderString(cookie.getValue()));
        }
        ctx.writeAndFlush(nettyResponse).addListener(ChannelFutureListener.CLOSE);
    }
}
