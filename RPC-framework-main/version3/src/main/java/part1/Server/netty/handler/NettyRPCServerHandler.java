package part1.Server.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import part1.Server.provider.ServiceProvider;
import part1.common.Message.RpcRequest;
import part1.common.Message.RpcResponse;

import java.lang.reflect.Method;

public class NettyRPCServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final ServiceProvider serviceProvider;

    public NettyRPCServerHandler(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) {
        RpcResponse<?> response = invokeService(request);
        ctx.writeAndFlush(response);
    }

    private RpcResponse<?> invokeService(RpcRequest request) {
        try {
            Object service = serviceProvider.getService(request.getInterfaceName());

            if (service == null) {
                return RpcResponse.fail("Service not found: " + request.getInterfaceName());
            }

            Method method = service.getClass()
                    .getMethod(request.getMethodName(), request.getParamsType());

            Object result = method.invoke(service, request.getParams());

            return RpcResponse.success(result);

        } catch (Exception e) {
            return RpcResponse.fail("Invoke method failed: " + e.getMessage());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.writeAndFlush(RpcResponse.fail("Netty server error: " + cause.getMessage()));
        ctx.close();
    }
}