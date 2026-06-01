package part1.Server;

import part1.Server.provider.ServiceProvider;
import part1.Server.server.RpcServer;
import part1.Server.server.impl.NettyRPCRPCServer;
import part1.common.service.Impl.UserServiceImpl;
import part1.common.service.UserService;

public class TestServer {

    public static void main(String[] args) {

        String host = "127.0.0.1";
        int port = 9999;

        UserService userService = new UserServiceImpl();

        ServiceProvider serviceProvider = new ServiceProvider(host, port);
        serviceProvider.registerService(userService);

        RpcServer rpcServer = new NettyRPCRPCServer(serviceProvider);
        rpcServer.start(port);
    }
}