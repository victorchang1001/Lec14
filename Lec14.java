import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.TextUI;
import javax.swing.text.JTextComponent;
import java.awt.Image;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;

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
//		int difficulty = Integer.parseInt(args[0]);
//		System.out.println("Enter desired difficulty/難易度 (low)0~5(high):");
		new Lec14();
	}

	public class MyJPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener, KeyListener{
		Timer timer, timer_game;
		Image image;
		int my_x, my_y;
		int my_life = 5;
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

		//text here
		JTextArea explain_before;
		JTextArea explain_after;
		JTextArea game_over;
		JTextArea score;
		JTextArea score_final;

		int game_stage = 0;
		int play_num = 0;

		Image star, pig, rock, fire, heart, arrow;
		int star_count[] = {0, 0, 0};
		int star_x[] = new int[3];

		int pig_life = 3;
		int pig_x = 800;
		int pig_velo = 5;
		int pig_size = 200;

		int fire_x = 100;
		int fire_y = 50;
		int fire_flag = 0;

		int rock_x = 800;
		int rock_width = 60;
		int rock_height = 40;
		int rock_count[] = {0, 0, 0};
		int rock_velo = 10;

		int arrow_x = 800;
		int arrow_y = 260;
		int arrow_velo = 0;

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

			//加一堆圖片區
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
			ImageIcon arrow_icon = new ImageIcon("arrow.png");
			arrow = arrow_icon.getImage();

			//小遊戲說明
			explain_before = new JTextArea("Drag and launch the bird and hit the pig to start the game!");
			explain_before.setBounds(100, 100, 600, 300);
			add(explain_before);

			//主遊戲說明
			explain_after = new JTextArea("Press SPACE to jump.\n" +
					"Press F to shoot fire.\n" +
					"Avoid the rock and shoot the pig to win!\n" +
					"Press ENTER to start the game!\n" +
					"You have three tries. Eat as many stars as you can!");
			explain_after.setBounds(100, 100, 600, 300);
			add(explain_after);
			explain_after.setVisible(false);

			//遊戲中間說明
			game_over = new JTextArea("Game Over. Press ENTER to continue.");
			game_over.setBounds(100, 100, 600, 300);
			add(game_over);
			game_over.setVisible(false);

			//中間報分數
			score = new JTextArea("Score");
			score.setBounds(100, 100, 600, 300);
			add(score);
			score.setVisible(false);

			//最終分數(sorted)
			score_final = new JTextArea("Score final");
			score_final.setBounds(100, 100, 600, 300);
			add(score_final);
			score_final.setVisible(false);

			//暫停按鈕
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

			//重新丟鳥鳥按鈕
			restart_button = new JButton("RESTART");
			restart_button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (pause_flag == 0 && (game_stage == 0)) {
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

		}

		private Boolean checkHitPig(){
			if (
					my_x > 750 &&//pig_x = 750
					my_x < (750 + 50) &&
					my_y > 400 &&//pig_y = 400
					my_y < (400 + 50)
			){
				return true;
			}
			return false;
		}

//		private Boolean checkEatStar(){
//			//吃星星，+1pt
//			return true;
//		}

		private Boolean checkHitRock(){
			//撞到石頭
//			if (my_x > rock_x && my_x < (rock_x + rock_width) && my_y > (400 - rock_height) && my_y < 400){
			if(rock_x < 90 && (my_y + 50 > 400 - rock_height)){
				System.out.println("HIT ROCK!!!!");
				return true;
			}
			return false;
		}

		private void moveRock(){
			rock_velo = 10 + rock_count[play_num]/5;
			rock_x -= rock_velo;
			//依照石頭數變快
			if (rock_x + rock_width < 0) {
				rock_x = 800;
				rock_count[play_num] += 1;
			}
		}

		private void movePig(){
			if(pig_life > 0){
				pig_x -= pig_velo;
				if (pig_x+pig_size < 0) {
					pig_x = 800;
				}
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

		private void moveFire(){
			if(fire_flag == 1){
				fire_x += 15;
				if(fire_x > 800){
					fire_flag = 0;
				}
			}
		}

		private Boolean fireHitPig(){
			if((fire_x + 90 > pig_x) && (fire_y > 400-pig_size)){
				return true;
			}
			return false;
		}

//		private void moveArrow(){
//			//generate random speed for arrow
////			arrow_velo = ThreadLocalRandom.current().nextInt(20, 50);
//			arrow_velo = 20;
//			arrow_x -= arrow_velo;
//			if (arrow_x+50 < 0) {
//				arrow_x = 800;
//			}
//		}

//		private Boolean checkHitArrow(){
//			return false;
//		}


		
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
				g.drawImage(image, my_x, my_y, 50, 50, this);
				//draw rock (moving)
				g.drawImage(rock, rock_x, (400-rock_height), rock_width, rock_height, this);//60x40 rock
				//draw pig (moving)
				if(pig_life > 0){
					g.drawImage(pig, pig_x, (400-pig_size), pig_size, pig_size, this);
				}
				//draw fire
				if(fire_flag == 1){
					g.drawImage(fire, fire_x, fire_y, 75, 40, this);
				}
				//draw arrow(moving)
//				g.drawImage(arrow, arrow_x, arrow_y, 50, 25, this);

			}
		}
		
		public void actionPerformed(ActionEvent e){
			Dimension d;
			d=getSize();

			//開始前畫面
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
					System.out.println("HIT PIG!");

					timer.stop();
					my_x = 50;
					my_y = 350;
//					t = 0.0;
					game_stage = 1;

					restart_button.setVisible(false);
					pause_button.setVisible(false);
					explain_before.setVisible(false);
					explain_after.setVisible(true);
				}
				repaint();
			}

			//主遊戲區
			if((game_stage == 1) && e.getSource() == timer_game){

				moveRock();
				gravityBird();
				if (pig_life > 0){
					movePig();
				}else{
					pig_x = 800;
				}

				moveFire();
				repaint();

				if(checkHitRock()){
					rock_x = 800;
					my_life -= 1;
					if (my_life == 0){
						score.setText("Rocks avoided: " + rock_count[play_num]);
						if(play_num == 2){

							System.out.println("===== Game Over =====");
							score_final.setText("Gameover.");
							score.setVisible(true);
//							System.exit(0);
						}
						timer_game.stop();
						game_over.setVisible(true);
						score.setVisible(true);

						my_life = 5;//重新開始時的生命
						play_num += 1;

					}
				}
				if(fireHitPig()&&fire_flag == 1){
					pig_life -= 1;
					fire_flag = 0;
					System.out.println(pig_life);
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

						break;
					}
					break;

				case KeyEvent.VK_SPACE:
					if(my_y == 350){
						my_y -= 100;
					}
					break;

				case KeyEvent.VK_UP:
					my_y -= 100;
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
	}
}
