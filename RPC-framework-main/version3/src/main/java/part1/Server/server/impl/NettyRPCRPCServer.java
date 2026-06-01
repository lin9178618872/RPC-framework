package part1.Server.server.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import part1.Server.netty.nettyInitializer.NettyServerInitializer;
import part1.Server.provider.ServiceProvider;
import part1.Server.server.RpcServer;

public class NettyRPCRPCServer implements RpcServer {

    private final ServiceProvider serviceProvider;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyRPCRPCServer(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void start(int port) {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyServerInitializer(serviceProvider));

            ChannelFuture future = bootstrap.bind(port).sync();

            System.out.println("Netty RPC server started on port: " + port);

            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Netty server interrupted");
        } finally {
            stop();
        }
    }

    @Override
    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}