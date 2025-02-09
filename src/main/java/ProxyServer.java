import java.io.*;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

class ProxyServer {
    private static final int PORT = 8080;
    private static final int MAX_CLIENTS = 400;
    private static final int MAX_CACHE_SIZE = 200 * (1 << 20); // 200MB
    private static final int MAX_ELEMENT_SIZE = 10 * (1 << 20); // 10MB
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
    private static final LRUCache<String, byte[]> cache = new LRUCache<>(MAX_CACHE_SIZE);
    private static final ReentrantLock cacheLock = new ReentrantLock();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Proxy Server running on port: " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            System.out.println("Error : " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (clientSocket; InputStream clientIn = clientSocket.getInputStream();
             OutputStream clientOut = clientSocket.getOutputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientIn));
            String requestLine = reader.readLine();
            if (requestLine == null || !requestLine.startsWith("GET")) {
                sendError(clientOut, 400, "Bad Request");
                return;
            }

            String[] parts = requestLine.split("-");
            if (parts.length < 2) {
                sendError(clientOut, 400, "Invalid Request");
                return;
            }

            String fullUrl = parts[1];
            if (!fullUrl.startsWith("http")) {
                fullUrl = "http://" + fullUrl;
            }

            if (fullUrl.startsWith("https")) {
                sendError(clientOut, 403, "HTTPS Not Supported (Use HTTP)");
                return;
            }

            byte[] response;
            if (cache.containsKey(fullUrl)) {
                System.out.println("Cache hit: Serving from cache -> " + fullUrl);
                response = cache.get(fullUrl);
            } else {
                System.out.println("Cache miss: Fetching from server -> " + fullUrl);
                response = fetchFromServer(fullUrl);
                cache.put(fullUrl, response);
            }
            for (String s : cache.keySet()) {
                System.out.println("Link : " + s);
            }
            clientOut.write(response);

        } catch (IOException e) {
            System.out.println("Error : " + e.getMessage());
        }
    }

    private static byte[] fetchFromServer(String url) {
        try {
            URL realServer = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) realServer.openConnection();
            conn.setRequestMethod("GET");

            try (InputStream serverIn = conn.getInputStream(); ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                byte[] data = new byte[4096];
                int bytesRead;
                while ((bytesRead = serverIn.read(data)) != -1) {
                    buffer.write(data, 0, bytesRead);
                }
                return buffer.toByteArray();
            }
        } catch (IOException e) {
            System.out.println("Error : " + e.getMessage());
            return "HTTP/1.1 500 Internal Server Error\r\n\r\n".getBytes();
        }
    }

    private static void sendError(OutputStream out, int statusCode, String message) throws IOException {
        String errorResponse = "HTTP/1.1 " + statusCode + " " + message + "\r\n\r\n";
        out.write(errorResponse.getBytes());
    }
}
