package context.arch.intelligibility.presenters;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JToggleButton;

/**
 * Utility class for GUI to maintain a library of icons.
 * @author Brian Y. Lim
 *
 */
public class ContextIcons {

	public static final Map<String, Icon> icons = new HashMap<String, Icon>();

	public static ImageIcon NOT = new ImageIcon("resources/icons/not.png");
	public static ImageIcon INFO = new ImageIcon("resources/icons/information.png");

	static {
		icons.put("Not", NOT);
		icons.put("Info", INFO);
	}
	
	public static Icon EMPTY_ICON = new Icon() {		
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			// empty			
		}		
		@Override public int getIconWidth() { return 32; }		
		@Override public int getIconHeight() { return 32; }
	}; 
	
	/**
	 * Button that only shows its icon.
	 * It does not have a background or a border.
	 * @author Brian Y. Lim
	 *
	 */
	public static class ToggleButton extends JToggleButton {
		
		private static final long serialVersionUID = 7066876318306164749L;

		public ToggleButton(ImageIcon icon) {
			this(icon, null);
		}

		public ToggleButton(ImageIcon icon, ActionListener listener) {
			super(icon);
			addActionListener(listener);
			setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
			setOpaque(false);
			setBorder(null);
		}
		
		@Override
		public void paintComponent(Graphics g) {
			// don't paint border or background
			
			Image img = ((ImageIcon)getIcon()).getImage();
			g.drawImage(img, 0, 0, this);
		}
		
	}
	
	public static void set(String name, Icon icon) {
		icons.put(name, icon);
	}
	
	public static Icon get(String name) {
		Icon icon = icons.get(name);
		return icon != null ? icon : EMPTY_ICON;
	}
	
	/**
	 * Convenience method that looks for icon for value first, 
	 * then if that is not found, finds the icon for the context,
	 * otherwise, returns the empty icon.
	 * @param context
	 * @param value
	 * @return
	 */
	public static Icon get(String context, String value) {
		Icon icon = icons.get(value);
		icon = icon != null ? icon : icons.get(context);
		return icon != null ? icon : EMPTY_ICON;
	}
	
	/**
	 * Applies a Not symbol over the original icon
	 * @param icon
	 * @return
	 */
	public static Icon not(final Icon icon) {
		Icon notIcon = new Icon() {
			
			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				// paint original first
				icon.paintIcon(c, g, x, y);
				
				// paint Not symbol
				NOT.paintIcon(c, g, x, y);
			}
			
			@Override public int getIconWidth() {
				return icon.getIconWidth();
			}			
			@Override public int getIconHeight() {
				return icon.getIconHeight();
			}
		};
		
		return notIcon;
	}
	
	/**
	 * Use this for any combo box that presents values and would be made prettier with icons.
	 * @author Brian Y. Lim
	 *
	 */
	public static class IconListCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = -2175816740784492047L;
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Icon icon = ContextIcons.get((String)value);
			JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			label.setIcon(icon);
			return label;
		}
	}

}
