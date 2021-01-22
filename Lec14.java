import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.TextUI;
import javax.swing.text.JTextComponent;
import java.awt.Image;
import java.util.concurrent.TimeUnit;

public class Lec14 extends JFrame{
	
	public Lec14(){
		setSize(800,500);
		setTitle("1W19CF03: HAngry Birds...");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		MyJPanel myJPanel= new MyJPanel();
		Container c = getContentPane();
		c.add(myJPanel);
		setVisible(true);
		setResizable(false);
	}
	
	public static void main(String[] args){
		new Lec14();
	}
	
	public class MyJPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener, KeyListener{
		Timer timer;
		Image image, star;
		int my_x, my_y;
		int mouse_x, mouse_y;
		int start_x, start_y;
		int init_x=100, init_y=375;
		double t=0.0, v=100.0;
		double v_x, v_y;
		int my_width, my_height;
		int grab_flag=0;

		int star_x = 750;

		JButton pause_button;
		int pause_flag = 0;

		JSlider velo_slider;
		JLabel velo;


		public MyJPanel(){
			setBackground(Color.white);
			addMouseListener(this);
			addMouseMotionListener(this);
			
			ImageIcon icon = new ImageIcon("bird.jpg");
			image = icon.getImage();
			my_width = image.getWidth(this);
			my_height = image.getHeight(this);
			my_x = init_x;
			my_y = init_y;

			ImageIcon star_icon = new ImageIcon("star.png");
			star = star_icon.getImage();

			pause_button = new JButton("PAUSE");
			pause_button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Graphics g = getGraphics();
					g.setColor(Color.black);
					if (pause_flag == 0) {
						//PAUSE
						pause_flag = 1;
						pause_button.setText("RESUME");
					} else if (pause_flag == 1) {
						//PLAY
						pause_flag = 0;
						pause_button.setText("PAUSE");
					}
				}
			});
			add(pause_button);
			
//			velo_slider = new JSlider(0, 200, 100);
//			velo = new JLabel("Velocity");
//			add(velo);
//			add(velo_slider);
		}
		
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			g.drawImage(image, my_x, my_y, this);
			g.drawImage(star, star_x, 87, 50, 50, this);
			g.setColor(Color.black);
			g.fillRect(95 + my_width / 2, 400, 10, 100);
			if (grab_flag == 1) {
				g.drawLine(95 + my_width / 2, 400, mouse_x, mouse_y);
			}

		}
		
		public void actionPerformed(ActionEvent e){
			Dimension d;
			d=getSize();

			if(pause_flag == 0 ) {
				t += 0.2;
//				v = velo_slider.getValue();
//-------------------------------------------------------------------
				my_x = (int) (v * v_x * t + start_x);
				my_y = (int) (9.8 * t * t / 2 - v * v_y * t + start_y);
				star_x = (int)(750 - v * v_x * t);
//-------------------------------------------------------------------
				if ((my_x < 0) || (my_x > d.width) || (my_y > d.height) || (my_y < 0)) {
					timer.stop();
					my_x = init_x;
					my_y = init_y;
					t = 0.0;
				}
				grab_flag = 0;
				repaint();
			}
		}

		public void mouseClicked(MouseEvent me)
		{
		}
		
		public void mousePressed(MouseEvent me)
		{
			mouse_x = me.getX();
			mouse_y = me.getY();
			if((pause_flag == 0)&&(grab_flag==0)&&(my_x<mouse_x)&&(mouse_x<my_x+my_width)&&(my_y<mouse_y)&&(mouse_y<my_y+my_height)){
				grab_flag = 1;
				start_x = mouse_x;
				start_y = mouse_y;
			}
		}
		
		public void mouseReleased(MouseEvent me)
		{
			if(grab_flag==1){
				timer = new Timer(100, this);
				timer.start();
//-------------------------------------------------------------------
				v_x = (double)(start_x-mouse_x)/100;
				v_y = -(double)(start_y-mouse_y)/100;
//-------------------------------------------------------------------
				start_x = my_x;
				start_y = my_y;
			}
		}
		
		public void mouseExited(MouseEvent me)
		{
		}
		
		public void mouseEntered(MouseEvent me)
		{
		}
		
		public void mouseMoved(MouseEvent me)
		{
		}
		
		public void mouseDragged(MouseEvent me)
		{
			if(grab_flag==1&&(pause_flag == 0)){
				mouse_x = me.getX();
				mouse_y = me.getY();
				my_x = init_x - (start_x-mouse_x);
				my_y = init_y - (start_y-mouse_y);
				repaint();
			}
		}
		public void keyPressed(KeyEvent me) {
			int keycode = me.getKeyCode();
//			char keychar = me.getKeyChar();
			switch (keycode) {
//				case KeyEvent.VK_ENTER:
//					break;

				case KeyEvent.VK_UP:
					my_y += 20;
					break;

				case KeyEvent.VK_DOWN:
					my_y -= 20;
					break;

//				default:
//					switch (keychar){
//						case 'x':
//
//							break;
//					}
//					break;
//				case KeyEvent.VK_X:
//					activateMyMissile();
//					break;
			}
		}

		public void keyReleased(KeyEvent e){
		}
		public void keyTyped(KeyEvent e){
		}
	}
}
