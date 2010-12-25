package context.apps.demos.roomlight;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class RoomPanel extends JPanel {

	private static final long serialVersionUID = -5527476385338784665L;

	public static final Image roomImg = new ImageIcon("demos/room-rules/img/room.png").getImage();
	
	public static final Image grassImg = new ImageIcon("demos/room-rules/img/grass.png").getImage();
	public static final Image grassNightImg = new ImageIcon("demos/room-rules/img/grass-night.png").getImage();
	
	public static final Image lampOnImg = new ImageIcon("demos/room-rules/img/lamp-on.png").getImage();
	public static final Image lampOffImg = new ImageIcon("demos/room-rules/img/lamp-off.png").getImage();

	public static final Image sunImg = new ImageIcon("demos/room-rules/img/sun.png").getImage();

	public final Map<String, Image> nameImgMap = new HashMap<String, Image>();
	{
		Image isabellaImg;
		Image emmaImg;
		Image jacobImg;
		Image ethanImg;
		
		nameImgMap.put("Isabella", isabellaImg = new ImageIcon("demos/room-rules/img/Isabella.png").getImage());
		nameImgMap.put("Emma", emmaImg = new ImageIcon("demos/room-rules/img/Emma.png").getImage());
		nameImgMap.put("Jacob", jacobImg = new ImageIcon("demos/room-rules/img/Jacob.png").getImage());
		nameImgMap.put("Ethan", ethanImg = new ImageIcon("demos/room-rules/img/Ethan.png").getImage());
		
		float scale = .75f;
		
		nameImgMap.put("Isabella_small", getScaled(isabellaImg, scale));
		nameImgMap.put("Emma_small", getScaled(emmaImg, scale));
		nameImgMap.put("Jacob_small", getScaled(jacobImg, scale));
		nameImgMap.put("Ethan_small", getScaled(ethanImg, scale));
	}
	
	public RoomPanel() {
		setPreferredSize(new Dimension(roomImg.getWidth(this), roomImg.getHeight(this)));
		setMinimumSize(new Dimension(roomImg.getWidth(this), roomImg.getHeight(this)));
		setLight((short)0); // start off
	}
	
	private Image getScaled(Image original, float scale) {
		return original.getScaledInstance(
				(int)(original.getWidth(this) * scale), 
				(int)(original.getHeight(this) * scale),
				Image.SCALE_SMOOTH);
	}

	private AlphaComposite acLamp;
	
	private AlphaComposite acSky;
	private Color COLOR_NIGHT = new Color(0,30,80);
	private Color COLOR_DAY = new Color(170,230,255);
	
	private int sunX = 425, sunY;
	private int sunY_brightest = -100;
	private int sunY_darkest = 330;

	private DefaultListModel insideListModel;
	private DefaultListModel outsideListModel;
	
	/**
	 * To set the light level of the ceiling lamp
	 * @param light between 0 and 1
	 */
	public void setLight(int light) {
		if (light > RoomModel.LIGHT_MAX) { light = RoomModel.LIGHT_MAX; }
		else if (light < 0) { light = 0; }
		acLamp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)light / RoomModel.LIGHT_MAX);
	}
	
	/**
	 * To set the brightness level of the sky, and sun height
	 * @param light between 0 and 1
	 */
	public void setBrightness(int brightness) {
		if (brightness > RoomModel.BRIGHTNESS_MAX) { brightness = RoomModel.BRIGHTNESS_MAX; }
		else if (brightness < 0) { brightness = 0; }
		float darkness = RoomModel.BRIGHTNESS_MAX - brightness;
		
		acSky = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)darkness / RoomModel.BRIGHTNESS_MAX);

		// sun height
		sunY = (int)((darkness/RoomModel.BRIGHTNESS_MAX) * (sunY_darkest - sunY_brightest) + sunY_brightest);
	}
	
	/**
	 * Links to list that maintains who is present in the room
	 * @param listModel
	 */
	public void setListModels(DefaultListModel insideListModel, DefaultListModel outsideListModel) {
		this.insideListModel = insideListModel;
		this.outsideListModel = outsideListModel;
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D)g;
		
		if (acSky == null) { return; } // not yet initialized fully
		
		int w = roomImg.getWidth(this), h = roomImg.getHeight(this);
		int lamp_w = lampOffImg.getWidth(this);
		
		g.setClip(5, 5, w-10, h-10);
		
		// sky
		g.setColor(COLOR_DAY);
		g.fillRect(10, 10, w-10, h-10);
		// night sky
		g2.setComposite(acSky);
		g.setColor(COLOR_NIGHT);
		g.fillRect(10, 10, w-10, h-10);		

		// sun
		g2.setComposite(AlphaComposite.SrcAtop); // reset
		g.drawImage(sunImg, sunX, sunY, this);
		
		// grass
		g.drawImage(grassImg, 360, 170, this);
//		g2.setComposite(acLamp);
//		g.drawImage(grassNightImg, 360, 170, this);
		
		g2.setComposite(AlphaComposite.SrcAtop); // reset
		
		/*
		 * Draw people outside
		 */
		if (outsideListModel != null) {
			for (int i = 0; i < outsideListModel.getSize(); i++) {
				String name = outsideListModel.getElementAt(i).toString();
//				Image personImg = nameImgMap.get(name);
				Image personImg = nameImgMap.get(name + "_small");
				if (personImg != null) {
					int y = 320;
					int x = 350 + (w-410) * (i+1)/(outsideListModel.getSize()+1);
					g.drawImage(personImg, x - personImg.getWidth(this)/2, y - personImg.getHeight(this), this);
				}
			}
		}

		// night sky
		g2.setComposite(acSky);
		g.setColor(COLOR_NIGHT);
		g.fillRect(10, 10, w-10, h-10);		
		
		g2.setComposite(AlphaComposite.SrcAtop); // reset
		
		// room
		g.drawImage(roomImg, 0, 0, this);
		
		// ceiling lamp
		g.drawImage(lampOffImg, (w - lamp_w)/2, 10, this);
		g2.setComposite(acLamp);
		g.drawImage(lampOnImg, (w - lamp_w)/2, 10, this);
		g2.setComposite(AlphaComposite.SrcAtop); // reset
		
		/*
		 * Draw people inside
		 */
		if (insideListModel != null) {
			for (int i = 0; i < insideListModel.getSize(); i++) {
				String name = insideListModel.getElementAt(i).toString();
				Image personImg = nameImgMap.get(name);
				if (personImg != null) {
					int y = 350;
					int x = w * (i+1)/(insideListModel.getSize()+1);
					g.drawImage(personImg, x - personImg.getWidth(this)/2, y - personImg.getHeight(this), this);
				}
			}
		}
	}

}
