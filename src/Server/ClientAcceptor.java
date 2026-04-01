package Server;
import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ExecutorService;

public class ClientAcceptor implements Runnable {
    final private ServerSocketChannel listenChannel;
    final private ExecutorService es;

    public ClientAcceptor(ServerSocketChannel listenChannel, ExecutorService es) {
        this.listenChannel = listenChannel;
        this.es = es;
    }

    public void run() {
        while (true) {
            try {
                // wait to accept a new client
                SocketChannel serveChannel = listenChannel.accept();
                // submit a separate task to handle the new client
                es.submit(new ClientHandler(serveChannel));
            } catch (AsynchronousCloseException e) {

                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

