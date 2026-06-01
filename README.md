# Custom RPC Framework

> Resume Project: Custom RPC Framework | Java, Netty, Serialization  
> Project Period: Feb. 2026 - Mar. 2026

## Project Overview

This project is a lightweight RPC framework implemented in Java. Its goal is to allow clients to call remote services in the same way they call local methods. The framework is built around remote invocation, network communication, service registration and discovery, load balancing, and custom serialization protocols. It uses Netty for high-performance network communication, ZooKeeper/Curator for service instance management, and a custom encoder/decoder protocol to transmit request and response objects across the network.

The project uses `UserService` as a sample service. The server registers the service implementation, while the client generates a proxy object through dynamic proxy. The client can then remotely call methods such as `createUser`, `getUserById`, `updateUserEmail`, and `deactivateUser`.

## Tech Stack

| Module | Technology |
| --- | --- |
| Language | Java 8 |
| Network Communication | Netty NIO, Java Socket |
| Service Registration and Discovery | ZooKeeper, Apache Curator |
| Serialization | Java Object Serialization, Fastjson |
| Dynamic Proxy | JDK Dynamic Proxy |
| Concurrency Utilities | ConcurrentHashMap, CopyOnWriteArrayList, AtomicInteger |
| Build Tool | Maven |

## Core Features

### 1. Remote Invocation Based on Dynamic Proxy

The client uses `ClientProxy` to generate a JDK dynamic proxy object for the service interface. When a method on the proxy object is called, the proxy intercepts the invocation, packages the interface name, method name, parameters, and parameter types into an `RpcRequest`, and sends the request to the remote service through the RPC client.

Invocation flow:

1. The client obtains a proxy object through `clientProxy.getProxy(UserService.class)`.
2. When `proxy.getUserById(id)` is called, the invocation enters `InvocationHandler.invoke`.
3. The proxy layer constructs an `RpcRequest` containing `interfaceName`, `methodName`, `params`, and `paramsType`.
4. `NettyRpcClient` discovers an available service address based on the interface name.
5. The request is encoded by Netty and sent to the server.
6. The server invokes the actual service implementation through reflection.
7. The server returns an `RpcResponse`, and the client extracts the result and returns it to the business code.

This design hides the underlying network communication details. The business layer only depends on interfaces and does not need to handle Socket connections, Netty channels, serialization, or service address selection.

### 2. Service Registration and Discovery

When the server starts, it registers local service instances through `ServiceProvider`. Each service implementation is stored in the local `serviceMap` by its interface name, and `ZKServiceRegister` registers the service address to ZooKeeper.

ZooKeeper node structure:

```text
/MyRPC
  /part1.common.service.UserService
    /127.0.0.1:9999
```

Design highlights:

- The service interface node is a persistent node, representing a service type.
- The service instance address is an ephemeral node, which is automatically removed when the server disconnects.
- The client uses `ZKServiceCenter` to query service instance lists.
- The client maintains a local `ServiceCache` to reduce repeated ZooKeeper queries.
- `WatchZK` uses CuratorCache to listen for node changes and update the local cache when service instances are added or removed.

This design allows service providers to join or leave dynamically. Clients can detect service instance changes, which provides the foundation for load balancing and fault tolerance.

### 3. Load Balancing

After discovering multiple service instances, the client selects a target address through the `LoadBalance` interface. The project implements three load balancing strategies:

| Strategy | Class | Description |
| --- | --- | --- |
| Round Robin | `RoundLoadBalance` | Uses `AtomicInteger` to maintain the access index for each service and distribute requests sequentially |
| Random | `RandomLoadBalance` | Uses `ThreadLocalRandom` to randomly select one address from the instance list |
| Consistent Hashing | `ConsistencyHashBalance` | Builds a hash ring with virtual nodes to reduce request migration when nodes are added or removed |

The default strategy is round robin. Round robin is simple and distributes requests evenly when service instances have similar capacity. Consistent hashing is more suitable for cache-oriented services or scenarios where requests should stay close to the same node.

### 4. Custom Communication Protocol and Serialization

The project defines the RPC message encoding and decoding process through `MyEncoder` and `MyDecoder`. The protocol header is 8 bytes long:

```text
+-------------+----------------+------------+-----------+
| messageType | serializerType | bodyLength | body      |
| 2 bytes     | 2 bytes        | 4 bytes    | N bytes   |
+-------------+----------------+------------+-----------+
```

Field description:

- `messageType`: Identifies whether the message is a request or a response, corresponding to `RpcRequest` and `RpcResponse`.
- `serializerType`: Identifies the serialization method, such as Java native serialization or JSON serialization.
- `bodyLength`: Represents the byte length of the message body and helps solve TCP packet sticking and half-packet issues.
- `body`: The serialized request or response content.

The serialization layer is abstracted through the `Serializer` interface. The current implementation supports:

- `ObjectSerializer`: Based on Java native object serialization, easy to integrate.
- `JsonSerializer`: Based on Fastjson, with type restoration for request parameters and response data.

This protocol design makes the framework extensible. Future versions can integrate higher-performance or cross-language serialization methods such as Kryo, Hessian, or Protobuf.

## System Call Flow

```text
Client
  -> ClientProxy intercepts interface method calls
  -> Builds RpcRequest
  -> ZKServiceCenter discovers service address
  -> LoadBalance selects a service instance
  -> Netty Channel sends the request
  -> MyEncoder encodes the request
  -> NettyRPCServerHandler receives the request
  -> ServiceProvider finds the local service implementation
  -> Invokes the target method through reflection
  -> Returns RpcResponse
  -> Client receives and returns the result
```

## Project Structure

```text
version3
├── pom.xml
└── src/main/java/part1
    ├── Client
    │   ├── proxy                 # Client-side dynamic proxy
    │   ├── rpcClient             # RPC client, including Netty and Socket implementations
    │   ├── serviceCenter         # Service discovery, cache, and load balancing
    │   └── netty                 # Client-side Netty Handler and Initializer
    ├── Server
    │   ├── provider              # Local service registration and lookup
    │   ├── serviceRegister       # ZooKeeper service registration
    │   ├── server                # RPC server abstraction and implementation
    │   └── netty                 # Server-side Netty Handler and Initializer
    └── common
        ├── Message               # RpcRequest, RpcResponse, MessageType
        ├── serializer            # Custom encoder/decoder and serialization implementation
        ├── service               # Sample service interface and implementation
        └── pojo                  # Sample business object
```

## How to Run

### 1. Start ZooKeeper

The project connects to the local ZooKeeper instance by default:

```text
127.0.0.1:2181
```

Make sure ZooKeeper is running before starting the server.

### 2. Start the Server

Run:

```text
part1.Server.TestServer
```

The server creates `UserServiceImpl`, registers the service to `ServiceProvider` and ZooKeeper, and then starts the Netty RPC server on port `9999`.

### 3. Start the Client

Run:

```text
part1.Client.TestClient
```

The client creates `ClientProxy`, generates a `UserService` proxy object, initiates remote calls through the proxy, and prints the returned results.


## Project Highlights

- Implemented transparent remote invocation based on dynamic proxy, reducing integration complexity for business code.
- Used Netty NIO instead of traditional blocking Socket to improve concurrent connection handling.
- Used ZooKeeper ephemeral nodes to detect service instance shutdown automatically.
- Introduced client-side service cache and Watch listeners to reduce ZooKeeper query pressure.
- Abstracted the `LoadBalance` interface and supported multiple load balancing strategies.
- Designed a custom message protocol to define message boundaries and avoid TCP packet sticking and half-packet issues.
- Abstracted the `Serializer` interface to improve serialization extensibility.
- Used `ConcurrentHashMap`, `CopyOnWriteArrayList`, and `AtomicInteger` to improve thread safety in concurrent scenarios.
