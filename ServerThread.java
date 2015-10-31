import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ServerThread extends Thread {

	private static int globalID = 1;

	private ObjectOutputStream output;
	private ObjectInputStream input;
	private Socket connection;
	private ActionListener textUpdate;
	private ActionListener attachUpdate;
	private ActionListener killThread;
	private int id;

	public ServerThread(final Socket connection,
			final ActionListener textUpdate, final ActionListener attachUpdate,
			final ActionListener killThread) {
		this.connection = connection;
		this.textUpdate = textUpdate;
		this.attachUpdate = attachUpdate;
		this.killThread = killThread;
		id = globalID++;
	}

	@Override
	public void run() {
		try {
			this.setupStreams();
			this.readIn();
		} catch (final EOFException e) {
			this.readString("Client Left.");
			killThread.actionPerformed(new ActionEvent(this, id, ""));
		} catch (final IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			this.closeStreams();
		}
	}

	private void setupStreams() throws IOException {
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
	}

	private void readIn() throws IOException, ClassNotFoundException {
		String message = "";
		do {
			final Object in = input.readObject();
			if (in instanceof String)
				message = this.readString(in);
			else if (in instanceof ImageIcon)
				this.readImage(in);
			else if (in instanceof byte[])
				this.readAudio(in);
			message = message.substring(message.length() - 3);
		} while (!message.equals("END"));
		this.readString("Client Left.");
		killThread.actionPerformed(new ActionEvent(this, id, ""));
	}

	private String readString(final Object in) {
		final String message = (String) in;
		textUpdate.actionPerformed(new ActionEvent(this, id, message));
		return message;
	}

	private void readImage(final Object in) {
		attachUpdate.actionPerformed(new ActionEvent(in, id, null));
		final JLabel img = new JLabel((ImageIcon) in);
		final JFrame frame = new JFrame();
		frame.add(img);
		frame.pack();
		frame.setVisible(true);
	}

	private void readAudio(final Object in) {
		final byte[] audioData = (byte[]) in;
		final InputStream byteArray = new ByteArrayInputStream(audioData);
		final AudioFormat format = new AudioFormat(44100.0F, 16, 2, true, false);

		try {
			final Clip sound = AudioSystem.getClip();
			sound.open(new AudioInputStream(byteArray, format, audioData.length
					/ format.getFrameSize()));
			sound.start();
		} catch (final LineUnavailableException | IOException e1) {
			e1.printStackTrace();
		}
	}

	public void sendMessage(final Object message) throws IOException {
		try {
			output.writeObject(message);
			output.flush();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void closeStreams() {
		try {
			output.close();
			input.close();
			connection.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public int getID() {
		return id;
	}

}
