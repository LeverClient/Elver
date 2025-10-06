package net.syl;

import com.lcv.Main;
import com.lcv.commands.hypixel.Bedwars;
import com.lcv.util.HypixelPlayerData;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class Window extends JFrame implements KeyListener {
	public static Window frame;

	public static JLabel label;

	public static BufferedImage image;

	public static ImageIcon imageIcon;

	public static ImageIcon resizeImageIcon(ImageIcon icon, int resizedWidth, int resizedHeight) {
		Image img = icon.getImage();
		Image resizedImage = img.getScaledInstance(resizedWidth, resizedHeight, Image.SCALE_SMOOTH);
		return new ImageIcon(resizedImage);
	}

	public static void onResize() {
		GraphicsConfiguration graphics = frame.getGraphicsConfiguration();
		GraphicsDevice device = graphics.getDevice();
		DisplayMode display = device.getDisplayMode();
		int width = display.getWidth();
		int height = display.getHeight();

		if (image == null) return;

		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		int imageX;
		int imageY;
		double imgRatio = (double) imageWidth / imageHeight;
		double labelRatio = (double) width / height;
		double scaleRatio;

		if (labelRatio < imgRatio) {
			scaleRatio = (double) imageWidth / width;
		} else {
			scaleRatio = (double) imageHeight / height;
		}

		int scaledWidth = (int) (imageWidth/scaleRatio/2);
		int scaledHeight = (int) (imageHeight/scaleRatio/2);

		imageIcon = resizeImageIcon(new ImageIcon(image), scaledWidth, scaledHeight);
		label.setIcon(imageIcon);
		label.setSize(scaledWidth, scaledHeight);

		imageX = graphics.getBounds().x + (width/2 - scaledWidth/2);
		imageY = graphics.getBounds().y + (height/2 - scaledHeight/2) - scaledHeight/3;

		frame.setSize(scaledWidth, scaledHeight);
		frame.setLocation(imageX, imageY);
	}

	public static void initialize() throws IOException {
		frame = new Window();
		label = new JLabel();

		// set up main window frame
		frame.add(label);
		frame.setSize(1536, 1152);
		frame.setTitle("Nya! :3");
		frame.setVisible(true);

		// i hate j frame
		frame.setLayout(null);

		// input handling?
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addKeyListener(frame);
	}

	public static void setImage(BufferedImage img) {
		image = img;
		imageIcon = new ImageIcon(img);

		onResize();
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e == null || (e.getKeyCode() == KeyEvent.VK_ENTER)) {
			// reead debug file
			String name = System.getenv("debugfile");
			byte[] debugFileBytes = new byte[0];
			try (InputStream stream = Main.class.getResourceAsStream("/" + name)) {
				assert stream != null;
				debugFileBytes = stream.readAllBytes();
			} catch (IOException ex) {}

			JSONObject hypixelJson = new JSONObject(new String(debugFileBytes));

//			HypixelPlayerData hypixelData = new HypixelPlayerData(hypixelJson);
//
//			// yay display image
//			BufferedImage statsImage;
//			try {
//				statsImage = new Bedwars().generateStatsImage(hypixelData);
//			} catch (IllegalArgumentException ex) {
//				throw new RuntimeException(ex);
//			}
//
//
//			Window.setImage(statsImage);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}
}
