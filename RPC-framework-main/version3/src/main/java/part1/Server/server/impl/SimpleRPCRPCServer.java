package part1.Server.server.impl;

import part1.Server.provider.ServiceProvider;
import part1.Server.server.RpcServer;
import part1.Server.server.work.WorkThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleRPCRPCServer implements RpcServer {

    private final ServiceProvider serviceProvider;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;

    public SimpleRPCRPCServer(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
        this.threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Simple RPC server started on port: " + port);

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                threadPool.execute(new WorkThread(socket, serviceProvider));
            }
        } catch (IOException e) {
            if (serverSocket == null || !serverSocket.isClosed()) {
                System.out.println("Simple RPC server start failed: " + e.getMessage());
            }
        } finally {
            stop();
        }
    }

    @Override
    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Close server socket failed: " + e.getMessage());
        }
        threadPool.shutdown();
    }
}
