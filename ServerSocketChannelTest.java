
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class ServerSocketChannelTest {

	public static void main(String[] args) throws IOException {
		// try {
		final ServerSocketChannel channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		ServerSocket socket = channel.socket();
		socket.bind(new InetSocketAddress(8080));
		socket.setReuseAddress(true);
		final Selector sel = Selector.open();
		channel.register(sel, SelectionKey.OP_ACCEPT);
		while (true) {
			int count = 0;
			if ((count = sel.select(2000)) == 0) {
				System.out.print(".");
				continue;
			}
			System.out.println("");
			System.out.println("all key size: " + sel.keys().size());
			for (SelectionKey key : sel.keys()) {
				if ((key.interestOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
					System.out.println("OP_ACCEPT");
				}else if ((key.interestOps() & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT) {
					System.out.println("OP_CONNECT");
				}else if ((key.interestOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
					System.out.println("OP_READ");
				}else if ((key.interestOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
					System.out.println("OP_WRITE");
				}
			}
			System.out.println("select size: " + count);
			for (SelectionKey key : sel.selectedKeys()) {
				if ((key.interestOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
					System.out.println("OP_ACCEPT");
				}else if ((key.interestOps() & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT) {
					System.out.println("OP_CONNECT");
				}else if ((key.interestOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
					System.out.println("OP_READ");
				}else if ((key.interestOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
					System.out.println("OP_WRITE");
				}
			}
			Iterator<SelectionKey> iter = sel.selectedKeys().iterator();
			while (iter.hasNext()) {
				SelectionKey key = iter.next();
				if (key.isAcceptable()) {
					SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
					socketChannel.configureBlocking(false);
					socketChannel.register(sel, SelectionKey.OP_READ);
				}
				if (key.isReadable()) {
					SocketChannel socketChannel = (SocketChannel) key.channel();
					ByteBuffer buffer = ByteBuffer.allocate(2048);
					socketChannel.read(buffer);
					key.interestOps(SelectionKey.OP_WRITE);
					String str = new String(buffer.array());
					System.out.println(str);
				}
				if (key.isValid() && key.isWritable()) {
					SocketChannel socketChannel = (SocketChannel) key.channel();
					ByteBuffer buffer = ByteBuffer.allocate(2048);
					StringBuilder resp = new StringBuilder();
					resp.append("HTTP/1.1 200 OK\r\n");
					resp.append("Content-Type: text/html;charset=utf-8\r\n");
					resp.append("Connection: keep-alive\r\n");
					resp.append("Pragma: no-cache\r\n");
					resp.append("Content-Length: 90\r\n");
					resp.append("\r\n");
					resp.append("<html><head><title>this is a test</title></head><body>hello, my first server</body></html>");
					buffer.put(resp.toString().getBytes());
					buffer.flip();
					socketChannel.write(buffer);
					if (!buffer.hasRemaining()) {
						key.interestOps(SelectionKey.OP_READ);
						socketChannel.socket().setKeepAlive(true);
					}
				}
				iter.remove();
			}
		}
	}
}
