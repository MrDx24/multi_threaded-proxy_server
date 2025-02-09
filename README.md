# MultiThreaded Proxy Server

A multithreaded HTTP Proxy Server built using Java. 
It handles multiple client requests, caches responses, and forwards HTTP requests to the actual server.

## 🚀 Features

* **HTTP Support**: Handles HTTP requests (GET method)
* **Multi-threading**: Built with ExecutorService for concurrent request handling
* **LRU Caching**: Implements Least Recently Used caching mechanism for improved performance
* **Request Logging**: Maintains logs of cache hits and misses
* **High Concurrency**: Efficiently handles up to 400 concurrent client connections

**Note**: HTTPS requests are not currently supported.

## 📌 How It Works

The proxy server operates as follows:

1. Listens for incoming connections on port 8080
2. Receives HTTP requests from clients (e.g., http://localhost:8080-http://example.com)
3. Checks cache for requested content:
    * If cached: Serves response from cache (cache hit)
    * If not cached: Fetches from origin server and stores in cache (cache miss)
4. Returns response to client

## 🛠️ Installation & Usage

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/MrDx24/multi_threaded-proxy_server.git
cd ProxyServer
```

### 2️⃣ Compile & Run

```bash
javac ProxyServer.java
java ProxyServer
```

### 3️⃣ Test Using curl

```bash
curl -x http://localhost:8080 http://example.com
```

## 📷 Example Input & Output

### Client Request:
```
GET http://localhost:8080-http://example.com
```

### Proxy Response:
```
Cache miss: Fetching from server -> http://example.com
Cache hit: Serving from cache -> http://example.com
```

## 📌 Future Enhancements

* Implement HTTPS support via tunneling (CONNECT method)
* Add Rate Limiting functionality to prevent abuse
* Optimize cache replacement strategy

