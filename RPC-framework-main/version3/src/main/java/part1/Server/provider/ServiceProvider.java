package part1.Server.provider;

import part1.Server.serviceRegister.ServiceRegister;
import part1.Server.serviceRegister.impl.ZKServiceRegister;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceProvider {

    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    private final String host;
    private final int port;
    private final ServiceRegister serviceRegister;

    public ServiceProvider(String host, int port) {
        this.host = host;
        this.port = port;
        this.serviceRegister = new ZKServiceRegister();
    }

    public void registerService(Object service) {
        Class<?>[] interfaces = service.getClass().getInterfaces();

        for (Class<?> clazz : interfaces) {
            String serviceName = clazz.getName();

            serviceMap.put(serviceName, service);

            serviceRegister.register(
                    serviceName,
                    new InetSocketAddress(host, port)
            );

            System.out.println("Register service: " + serviceName);
        }
    }

    public Object getService(String serviceName) {
        return serviceMap.get(serviceName);
    }
}