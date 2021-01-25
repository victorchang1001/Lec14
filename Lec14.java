import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.TextUI;
import javax.swing.text.JTextComponent;
import java.awt.Image;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Lec14 extends JFrame{
	
	// # Lec14
	// Javaプログラミング
	// Changed something: Added JButtons
	// Functionalities: Instructers, sort, random.
	
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
		Timer timer, timer_game;
		Image image;
		int my_x, my_y;
		int my_life = 3;
		int mouse_x, mouse_y;
		int start_x, start_y;
		int init_x=100, init_y=375;
		double t=0.0, v=100.0;
		double v_x, v_y;
		int my_width, my_height;
		int grab_flag=0;

		JButton pause_button;
		int pause_flag = 0;
		JButton restart_button;
		JButton skip_button;

		//text used in game
		JTextArea explain_before;
		JTextArea explain_after;
		JTextArea game_over;
		JTextArea score;
		JTextArea score_final;

		int game_stage = 0;
		int play_num = 0;
		Boolean gameover_flag = false;

		Image star, pig, rock, fire, heart;

		int star_x = 800;
		int star_y = 260;
		int star_size = 50; //50x50star
		int star_velo = 15;

//		int pig_life = 3;
//		int pig_x = 800;
//		int pig_velo = 5;
//		int pig_size = 200;

		int fire_x = 100;
		int fire_y = 50;
		int fire_flag = 0;

		int rock_x = 800;
		int rock_width = 60;
		int rock_height = 40;
		int rock_count[] = {0, 0, 0};
		int rock_velo = 10;

		public MyJPanel(){
			setBackground(Color.white);
			addMouseListener(this);
			addMouseMotionListener(this);
			setFocusable(true);
			addKeyListener(this);

			ImageIcon icon = new ImageIcon("bird.jpg");
			image = icon.getImage();
			my_width = image.getWidth(this);
			my_height = image.getHeight(this);
			my_x = init_x;
			my_y = init_y;

			//import images
			ImageIcon star_icon = new ImageIcon("star.png");
			star = star_icon.getImage();
			ImageIcon pig_icon = new ImageIcon("pig.jpg");
			pig = pig_icon.getImage();
			ImageIcon rock_icon = new ImageIcon("rock.png");
			rock = rock_icon.getImage();
			ImageIcon fire_icon = new ImageIcon("fire.png");
			fire = fire_icon.getImage();
			ImageIcon heart_icon = new ImageIcon("heart.jpg");
			heart = heart_icon.getImage();

			//before game instructions
			explain_before = new JTextArea("Launch the bird to hit the pig and enter the main game!\n" +
					"Change something: (1) pause button\n" +
					"Change something: (2) restart button\n" +
					"Change something: (3) skip button\n" +
					"Change something: (4) main game following\n" +
					"Functionalities: instructers" +
					"Functionalities related to Lec5: sort final score\n" +
					"Click anywhere or drag the bird to start the game");
			explain_before.setBounds(100, 100, 600, 300);
			add(explain_before);

			//main game instructions
			explain_after = new JTextArea("Press SPACE to jump.\n" +
					"(Press UP/DOWN to move upwards/downwards too.)\n" +
					"Press F to fire.\n" +
					"Avoid as many rocks as you can.\n" +
					"Shoot (F) at the star to heal 1 heart.\n" +
					"Remember to heal up!" +
					"You have three tries, 3 hp each game.\n" +
					"--------Press ENTER to start the game!--------");
			explain_after.setBounds(100, 100, 600, 300);
			add(explain_after);
			explain_after.setVisible(false);

			//say game over
			game_over = new JTextArea("Game Over. Press ENTER to continue.");
			game_over.setBounds(100, 100, 600, 300);
			add(game_over);
			game_over.setVisible(false);

			//show current game score
			score = new JTextArea("Score");
			score.setBounds(100, 100, 600, 300);
			add(score);
			score.setVisible(false);

			//show final score
			score_final = new JTextArea("Score final");
			score_final.setBounds(100, 100, 600, 300);
			add(score_final);
			score_final.setVisible(false);

			//pause button
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

			//restart button
			restart_button = new JButton("RESTART");
			restart_button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (game_stage == 0) {
						if (pause_flag == 1) {
							//PAUSE condition, restart
							pause_flag = 0;
							pause_button.setText("PAUSE");
						}
						my_x = init_x;
						my_y = init_y;
						t = 0.0;
						grab_flag = 0;
						timer.stop();
						repaint();
					}

				}
			});
			add(restart_button);

			skip_button = new JButton("SKIP");
			skip_button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					timer = new Timer(100, this);
					timer.start();
					timer.stop();
					my_x = 50;
					my_y = 350;

					game_stage = 1;

					restart_button.setVisible(false);
					pause_button.setVisible(false);
					skip_button.setVisible(false);
					explain_before.setVisible(false);
					explain_after.setVisible(true);
					repaint();
				}
			});
			add(skip_button);

		}

		private Boolean checkHitPig(){
			return my_x > 750 &&//pig_x = 750
					my_x < (750 + 50) &&
					my_y > 400 &&//pig_y = 400
					my_y < (400 + 50);
		}

		private Boolean checkHitRock(){
			//hit rock?
			Rectangle my_rect = new Rectangle(my_x, my_y, 50, 50);//50x50bird
			Rectangle rock_rect = new Rectangle(rock_x, (400-rock_height), rock_width, rock_height);
			return my_rect.intersects(rock_rect);
//			return rock_x < 90 && (my_y + 50 > 400 - rock_height);
		}

		private void moveRock(){
			rock_velo = 10 + (int)(rock_count[play_num]/1.5);
			rock_x -= rock_velo;
			//speed up according to #rock avoided
			if (rock_x + rock_width < 0) {
				rock_x = 800;
				rock_count[play_num] += 1;
			}
		}

		private void moveStar(){
			Random r = new Random();
			int rand = r.ints(0, 4).findFirst().getAsInt();

			star_x -= star_velo*rand;
			if (star_x+star_size < 0) {
				star_x = 1200;
			}
		}

		private void gravityBird(){
			if(my_y < 350){
				my_y += 2.5;
				if(my_y > 350){
					my_y = 350;
				}
				if(my_y < 250){
					my_y = 250;
				}
			}
		}

		private void activateFire(){
			if(fire_flag == 0){
				fire_x = 100;
				fire_y = my_y;
				fire_flag = 1;
			}
		}

		private void resetFire(){
			if(fire_flag == 1){
				fire_x = 100;
				fire_y = 500;
				fire_flag = 0;
			}
		}



		private void moveFire(){
			if(fire_flag == 1){
				fire_x += 30;
				if(fire_x > 800){
					fire_flag = 0;
				}
			}
		}

		private Boolean checkHitStar(){
			Rectangle fire_rect = new Rectangle(fire_x, fire_y, 60, 30);//60x30fire
			Rectangle star_rect = new Rectangle(star_x, star_y, star_size, star_size);//
			return fire_rect.intersects(star_rect);
		}

		public void paintComponent(Graphics g){
			super.paintComponent(g);//reset graphics
			if(game_stage == 0) {
				g.drawImage(image, my_x, my_y, this);
				g.drawImage(pig, 750, 400, 50, 50, this);
				g.setColor(Color.black);
				g.fillRect(95 + my_width / 2, 400, 10, 100);
				if (grab_flag == 1) {
					g.drawLine(95 + my_width / 2, 400, mouse_x, mouse_y);
				}
			}

			if(game_stage == 1){
				//draw ground
				g.fillRect(0, 400, 800, 100);
				//draw heart (life)
				for(int i = 0; i < my_life; i++){
					g.drawImage(heart, 10+35*i, 430,30,20, this);
				}
				//draw bird
				g.drawImage(image, my_x, my_y, 50, 50, this);//50x50bird
				//draw rock (moving)
				g.drawImage(rock, rock_x, (400-rock_height), rock_width, rock_height, this);//60x40 rock
				//draw star (moving)
				g.drawImage(star, star_x, star_y, star_size, star_size, this);

				//draw fire
				if(fire_flag == 1){
					g.drawImage(fire, fire_x, fire_y, 60, 30, this);//fire60x30
				}
			}
		}
		
		public void actionPerformed(ActionEvent e){
			Dimension d;
			d=getSize();

			//before game, template
			if(pause_flag == 0 && (game_stage == 0)) {
				t += 0.2;
//-------------------------------------------------------------------
				my_x = (int) (v * v_x * t + start_x);
				my_y = (int) (9.8 * t * t / 2 - v * v_y * t + start_y);
//-------------------------------------------------------------------
				if ((my_x < 0) || (my_x > d.width) || (my_y > d.height) || (my_y < 0)) {
					timer.stop();
					my_x = init_x;
					my_y = init_y;
					t = 0.0;
				}
				grab_flag = 0;
				if (checkHitPig()){
					timer.stop();
					my_x = 50;
					my_y = 350;

					game_stage = 1;

					restart_button.setVisible(false);
					pause_button.setVisible(false);
					skip_button.setVisible(false);
					explain_before.setVisible(false);
					explain_after.setVisible(true);
				}
				repaint();
			}

			//main game
			if((game_stage == 1) && e.getSource() == timer_game){

				moveRock();
				gravityBird();
				moveStar();
				moveFire();
				repaint();

				if(checkHitRock()){
					rock_x = 800;
					my_life -= 1;
					if (my_life == 0){
						if(play_num == 2){
							gameover_flag = true;
							score.setText("\nGame over." +
									"\nGame 1: " + rock_count[0] +" rocks!"+
									"\nGame 2: " + rock_count[1] +" rocks!"+
									"\nGame 3: " + rock_count[2]+" rocks!");
							score.setVisible(true);

							//Lec5: sort algorithm
							//Sort scores of three games and show them in order
							Quick quick = new Quick();
							quick.sort(rock_count);
							score_final.setText("\nPress ENTER to end game." +
									"\nBEST score: " + rock_count[2] +
									"\nSecond best score: " + rock_count[1] +
									"\nWorst score: " + rock_count[0]);
							score_final.setVisible(true);

							System.out.println("===== Game Over =====");
							timer_game.stop();
//							Thread t1 = new MyThread();
//							t1.start();


						}

						score.setText("Rocks avoided: " + rock_count[play_num]);
						timer_game.stop();
						game_over.setVisible(true);
						score.setVisible(true);

						my_life = 3;//restart life
						play_num += 1;

					}
				}
				if(checkHitStar()){
					resetFire();
					star_x = 800;
					if(my_life < 3){
						my_life += 1;
					}
				}

				repaint();
			}
		}

		public void mouseClicked(MouseEvent me)
		{
		}
		
		public void mousePressed(MouseEvent me)
		{
			if(game_stage == 0) {
				explain_before.setVisible(false);
				mouse_x = me.getX();
				mouse_y = me.getY();
				if ((pause_flag == 0) && (grab_flag == 0) && (my_x < mouse_x) && (mouse_x < my_x + my_width) && (my_y < mouse_y) && (mouse_y < my_y + my_height)) {
					grab_flag = 1;
					start_x = mouse_x;
					start_y = mouse_y;
				}
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
			char keychar = me.getKeyChar();
			switch (keycode) {
				case KeyEvent.VK_ENTER:

					if(game_stage == 1){
						timer_game = new Timer(25, this);
						timer_game.start();
						explain_after.setVisible(false);
						game_over.setVisible(false);
						score.setVisible(false);

						if(gameover_flag){
							System.exit(0);
						}
						break;

					}
					break;

				case KeyEvent.VK_SPACE:
					if(my_y == 350){
						my_y -= 100;
					}
					break;

				case KeyEvent.VK_UP:
					if(my_y < 300){
						my_y -= 20;
					}
					break;

				case KeyEvent.VK_DOWN:
					if(my_y < 350) {
						my_y += 20;
						if (my_y > 350){
							my_y = 350;
						}
					}
					break;

				default:
					switch (keychar){
						case 'f':
							activateFire();
							break;
					}
					break;
//				case KeyEvent.VK_X:
//					activateMyMissile();
//					break;
			}
		}

		public void keyReleased(KeyEvent e){
		}
		public void keyTyped(KeyEvent e){
		}

		class Quick{//used for quick sort (Lec. 5)
			public int compareCounter;
			public int swapCounter;
			public int swapIndex;
			public int pivot;

			public void swap(int[] A, int i, int j){
				this.swapCounter++;
				int tmp = A[i];
				A[i] = A[j];
				A[j] = tmp;
			}
			public void compareAndSwap(int[] A, int i){
				this.compareCounter++;
				if (A[i] <= this.pivot)
					swap(A, i, this.swapIndex++);
			}

			public void sort(int[] A){
				sort(A, 0, A.length - 1);
			}

			public void sort(int[] A, int left, int right) {
				this.compareCounter=0;
				this.swapCounter=0;

				if (right <= left)  return;

				int pivotIndex = (left + right) / 2;
				this.pivot = A[pivotIndex];
				swap(A, pivotIndex, right);
				this.swapIndex = left;
				for(int i = left; i < right; ++i){
					compareAndSwap(A, i);
				}
				swap(A, swapIndex, right);

				sort(A, left, swapIndex - 1);
				sort(A, swapIndex + 1, right);

			}
		}

		class MyThread extends Thread {
			public void run(){
				try {
					sleep(10000);
					System.exit(0);
				} catch(InterruptedException e){
				}
			}
		}

	}
}
