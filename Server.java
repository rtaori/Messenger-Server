import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class Server {

	public static final int PORT_NUMBER = 6789;
	public static final String NAME = "SERVER - ";

	private ServerSocket server;
	private ServerGUI graphics;
	private ArrayList<ServerThread> connections;
	private StringBuilder messages;

	public Server() {
		graphics = new ServerGUI(e -> this.sendMessage(
				NAME + e.getActionCommand(), 0),
				e -> this.sendMessage(e.getSource(), 0),
				e -> this.downloadHistory(e.getID()));
		graphics.makeVisible();

		connections = new ArrayList<ServerThread>();

		try {
			server = new ServerSocket(PORT_NUMBER);
		} catch (final IOException e) {
			e.printStackTrace();
		}

		messages = new StringBuilder();
	}

	public void run() {
		while(true)
			try {
				final Socket client = server.accept();
				final ServerThread connect = new ServerThread(client,
						e -> this.sendMessage(e.getActionCommand(), e.getID()),
						e -> this.sendMessage(e.getSource(), e.getID()),
						e -> this.killClient(e.getSource(), e.getID()));
				connect.start();
				connections.add(connect);
				graphics.setTypable(true);
			} catch (final IOException e) {
				e.printStackTrace();
			}
	}

	private void downloadHistory(final int compression) {

		switch (compression) {
		case 0:

			break;
		case 1:
			break;
		default:
			break;
		}
	}

	private void sendMessage(final String message, final int id) {
		final int index = message.indexOf("-");
		if (index < 0 || message.substring(index).length() == 2)
			return;
		graphics.showMessage(message);
		messages.append(message + "\n");
		if (id == 0)
			graphics.resetText();
		for (final ServerThread serv : connections)
			try {
				serv.sendMessage(message);
			} catch (final IOException e) {
				e.printStackTrace();
			}
	}

	private void sendMessage(final Object message, final int id) {
		for (final ServerThread serv : connections)
			if (serv.getID() != id)
				try {
					serv.sendMessage(message);
				} catch (final IOException e) {
					e.printStackTrace();
				}
	}

	private void killClient(final Object clnt, final int id) {
		final ServerThread client = (ServerThread) clnt;
		for (final Iterator<ServerThread> iterator = connections.iterator(); iterator
				.hasNext();)
			if(iterator.next() == client)
				iterator.remove();
	}

}
