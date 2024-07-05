package com.platformatory;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class RestServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final StateQueryClient queryClient;

    public RestServerHandler(StateQueryClient queryClient) {
        this.queryClient = queryClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (request.getMethod() == HttpMethod.GET) {
            String uri = request.getUri();
            if (uri.startsWith("/query/")) {
                String key = uri.substring("/query/".length());
                String responseContent = queryClient.queryState(key);
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(responseContent.getBytes()));
                response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
