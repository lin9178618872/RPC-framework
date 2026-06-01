package part1.Server.server.work;

import part1.Server.provider.ServiceProvider;
import part1.common.Message.RpcRequest;
import part1.common.Message.RpcResponse;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;

public class WorkThread implements Runnable {

    private final Socket socket;
    private final ServiceProvider serviceProvider;

    public WorkThread(Socket socket, ServiceProvider serviceProvider) {
        this.socket = socket;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void run() {
        try (
                ObjectInputStream input =
                        new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream output =
                        new ObjectOutputStream(socket.getOutputStream())
        ) {
            RpcRequest request = (RpcRequest) input.readObject();

            RpcResponse<?> response = invokeService(request);

            output.writeObject(response);
            output.flush();

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Handle socket request failed: " + e.getMessage());
        }
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
            return RpcResponse.fail("Socket invoke failed: " + e.getMessage());
        }
    }
}