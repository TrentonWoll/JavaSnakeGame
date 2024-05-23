import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import java.io.File;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.sound.sampled.*;
import java.io.IOException;

public class GamePanel extends JPanel implements ActionListener {
	//constants for screen dimensions and game settings
	static final int SCREEN_WIDTH = 800;
	static final int SCREEN_HEIGHT = 800;
	static final int UNIT_SIZE = 40;
	static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE;
	static final int DELAY = 100;
	//snake properties
	final int x[] = new int[UNIT_SIZE];
	final int y[] = new int[GAME_UNITS];
	int body = 3;
	int foodEaten;
	int foodX;
	int foodY;
	char direction = 'R';
	boolean running = false;
	//game timer
	Timer timer;
	// Random number generator
	Random random;
	// custom font
	private Font customFont;
	// restart button
	private JButton restartButton;
	//high score
	int highScore = 0;
	// background music
	private BackgroundMusic backgroundMusic;
	// grid colors
	Color[] gridColors = {Color.red, Color.blue, Color.green, Color.orange, Color.pink, Color.cyan, Color.yellow, Color.magenta, Color.black};
	// Color change frequency
	int colorChangeFrequency = 1; 
	//counter for food coloring
	int foodCounter = 0;
	// current grid color
	private Color currentGridColor = Color.black;
	//pause state
	boolean isPaused = false;
	//check if it's the first food
	private boolean isFirstFood = true;
	// pause message
	String pausedMessage = "Paused";
	

	GamePanel() {
	    // Initialize a random number generator
	    random = new Random();

	    // Set the preferred size and background color of the panel
	    this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
	    this.setBackground(Color.black);

	    // Allow the panel to receive key events
	    this.setFocusable(true);
	    this.addKeyListener(new MyKeyAdapter());

	    // Initialize and play background music
	    backgroundMusic = new BackgroundMusic("C:\\Users\\trent\\OneDrive\\Desktop\\Fonts\\610068__bloodpixelhero__game-music-loop-7.wav");
	    backgroundMusic.play();

	    try {
	        // Load and register a custom font
	        customFont = Font.createFont(Font.TRUETYPE_FONT, new File("C:\\Users\\trent\\OneDrive\\Desktop\\Fonts\\PublicPixel-z84yD.ttf"));
	        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	        ge.registerFont(customFont);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    // Initialize the restart button but hide it initially
	    restartButton = new JButton("Restart");
	    restartButton.setVisible(false); // Hide the button initially

	    // Calculate the X and Y coordinates for the center of the screen
	    int buttonX = (SCREEN_WIDTH - restartButton.getPreferredSize().width) / 2;
	    int buttonY = (SCREEN_HEIGHT - restartButton.getPreferredSize().height) / 2;

	    // Set the position and size of the restart button
	    restartButton.setBounds(buttonX, buttonY, restartButton.getPreferredSize().width, restartButton.getPreferredSize().height);

	    // Add the restart button to the panel
	    this.add(restartButton);

	    // Add an action listener to handle button clicks
	    restartButton.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            restartGame();
	        }
	    });

	    // Start the game
	    startGame();
	}


    public void startGame() {
    	//check if game is not running
        if (!running) {
        	//start snakes length at 3
            body = 3;
            //start food count at 0
            foodEaten = 0;
            //start direction moving to the right
            direction = 'R';
            //set the game as running so it starts
            running = true;
            
         // Initialize the snake's position in the middle of the screen
            int initialX = SCREEN_WIDTH / 2;
            int initialY = SCREEN_HEIGHT / 2;
            for (int i = 0; i < body; i++) {
                x[i] = initialX - i * UNIT_SIZE;
                y[i] = initialY;
            }
            
            // Start the game in a paused state
            isPaused = true;
        }
        
        //generates the first food
        Food();
        //check for game timer and create on if new and start the timer
        if (timer == null) {
            timer = new Timer(DELAY, this);
            timer.start();
        }
    }
    
    //setting up the audio for when you eat food
    public void playEatFoodSound() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("C:\\Users\\trent\\OneDrive\\Desktop\\Fonts\\649726__duskbreaker__8bit-coin-collection-2.wav"));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
    
    //setting up the audio when you die
    public void playDeathSound() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("C:\\Users\\trent\\OneDrive\\Desktop\\Fonts\\350925__cabled_mess__hurt_c_08.wav"));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
    
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);

        if (running) {
            //yellow font for score
            g.setColor(Color.yellow);
            g.setFont(customFont.deriveFont(Font.BOLD, 30)); 
            FontMetrics metrics2 = getFontMetrics(g.getFont());
            g.drawString("Score: " + foodEaten, (SCREEN_WIDTH - metrics2.stringWidth("Score: " + foodEaten)) / 2, g.getFont().getSize());

            // orange font on top of yellow to give 3d effect
            g.setColor(Color.orange);
            g.setFont(customFont.deriveFont(30f)); 
            FontMetrics metrics1 = getFontMetrics(g.getFont());
            g.drawString("Score: " + foodEaten, (SCREEN_WIDTH - metrics1.stringWidth("Score: " + foodEaten)) / 2, g.getFont().getSize());
        } else {
            endGame(g);
        }

        if (isPaused) {
            // show puased on the screen when game is paused, uses same yellow orange overlap
            g.setColor(Color.yellow);
            g.setFont(customFont.deriveFont(Font.BOLD, 55));
            FontMetrics metrics3 = getFontMetrics(g.getFont());
            g.drawString(pausedMessage, (SCREEN_WIDTH - metrics3.stringWidth(pausedMessage)) / 2, SCREEN_HEIGHT / 2);
            g.setColor(Color.orange);
            g.setFont(customFont.deriveFont(Font.BOLD, 54));
            FontMetrics metrics4 = getFontMetrics(g.getFont());
            g.drawString(pausedMessage, (SCREEN_WIDTH - metrics3.stringWidth(pausedMessage)) / 2, SCREEN_HEIGHT / 2);
        }
    }

    public void draw(Graphics g) {
        if (running) {
            // Change the grid color
            g.setColor(currentGridColor);
            for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
                g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
                g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
            }

            //draw the food as red
            g.setColor(Color.red);
            g.fillOval(foodX, foodY, UNIT_SIZE, UNIT_SIZE);
            
            for (int i = 0; i < body; i++) {
                if (i == 0) {
                    // set head color
                    g.setColor(new Color(0, 100, 0));
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                } else {
                    // set body color
                    g.setColor(new Color(57, 255, 20));
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }
            }
            //default color for anything drawn is white
            g.setColor(Color.white);
        } else {
        	//display end game screen game is not running
            endGame(g);
        }
    }

    public void Food() {
        if (isFirstFood) {
            // position the first food in front of the snake
        	foodX = x[0] + 3 * UNIT_SIZE;
            foodY = y[0];
            isFirstFood = false;
        } else {
            // spawn food randomly
            foodX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
            foodY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
        }
    }

    public void restartGame() {
        // Reset game variables
        body = 3;
        foodEaten = 0;
        direction = 'R';
        running = true;

        // hide the restart button again
        restartButton.setVisible(false);

        //restart the game timer
        if (timer != null) {
            timer.stop();
            timer = new Timer(DELAY, this);
            timer.start();
        }

        // respawn the snake in the middle of the screen
        int initialX = SCREEN_WIDTH / 2;
        int initialY = SCREEN_HEIGHT / 2;
        for (int i = 0; i < body; i++) {
            x[i] = initialX - i * UNIT_SIZE;
            y[i] = initialY;
        }

        // start a new game by generating food and redrawing the game
        Food();
        repaint();
    }

    public void checkFood() {
    	//checks if the snakes had and food are in the same position
        if ((x[0] == foodX) && (y[0] == foodY)) {
            body++;
            foodEaten++;
            // check the score and see if it is higher than the high score 
            if (foodEaten > highScore) {
                highScore = foodEaten;
            }
            //play food eating sound effect
            playEatFoodSound();
            //increment food counter
            foodCounter++; 
            //checks if the grid color should be changed
            if (foodCounter % colorChangeFrequency == 0) {
                // change the color of the grid
                currentGridColor = gridColors[foodCounter / colorChangeFrequency % gridColors.length];
            }
            //spawn another food
            Food();
        }
    }


    public void movement() {
    	//move the body of the snake to the previous x and y coordinates starting from the head
        for (int i = body; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }
        //update the location of the head based on the direction
        switch (direction) {
            case 'U'://up
                y[0] = y[0] - UNIT_SIZE;
                break;
            case 'D'://down
                y[0] = y[0] + UNIT_SIZE;
                break;
            case 'L'://left
                x[0] = x[0] - UNIT_SIZE;
                break;
            case 'R'://right
                x[0] = x[0] + UNIT_SIZE;
                break;
        }
    }

	
    public void collision() {
        for (int i = body; i > 0; i--) {
        	//check if the head collides with its body
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
             // play the death sound effect
                playDeathSound(); 
            }
        }
        //check if the head collides with any of the walls
        if (y[0] < 0 || x[0] > SCREEN_WIDTH || y[0] > SCREEN_HEIGHT || x[0] < 0) {
            running = false;
         // play the death sound effect
            playDeathSound(); 
        }
        //if the game is not running stop the time and show the restart button
        if (!running) {
            timer.stop();
            restartButton.setVisible(true); 
        }
    }

    public void endGame(Graphics g) {
    	//set color to green to display the score
        g.setColor(Color.green);
        g.setFont(customFont.deriveFont(Font.BOLD, 30));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Score: " + foodEaten, (SCREEN_WIDTH - metrics1.stringWidth("Score: " + foodEaten)) / 2, SCREEN_HEIGHT - 100);
        //set color to blue to display high score
        g.setColor(Color.blue);
        g.setFont(customFont.deriveFont(Font.BOLD, 35));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("High Score: " + highScore, (SCREEN_WIDTH - metrics2.stringWidth("High Score: " + highScore)) / 2, SCREEN_HEIGHT - 40);
        //set game over to red and display it big in the middle
        g.setColor(Color.red);
        g.setFont(customFont.deriveFont(Font.BOLD, 55));
        FontMetrics metrics3 = getFontMetrics(g.getFont());
        g.drawString("Game Over!", (SCREEN_WIDTH - metrics3.stringWidth("Game Over!")) / 2, SCREEN_HEIGHT / 2);

        //show the restart button when the game ends
        restartButton.setVisible(true);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
    	//check if game is running and not paused
        if (running && !isPaused) {
        	//execute snake movement
            movement();
            //check for food eaten and for collisions
            checkFood();
            collision();
        }
        //repaint to update graphics
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
        	//create and initialize  a new direction variable
            char newDirection = direction;
            //if space is pressed pause the game
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                isPaused = !isPaused; 
                //if game is not paused and then set keypad arrows and wasd to control the direction
            }
            
            else if (!isPaused) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        if (direction != 'R') {
                            newDirection = 'L';
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        if (direction != 'L') {
                            newDirection = 'R';
                        }
                        break;
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        if (direction != 'D') {
                            newDirection = 'U';
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        if (direction != 'U') {
                            newDirection = 'D';
                        }
                        break;
                }
            }
            // update the direction with newDirection
            direction = newDirection;
        }
    }
    
    
    
    public class BackgroundMusic {
        private Clip clip;

        public BackgroundMusic(String musicFilePath) {
            try {//try to load and open the audio file
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("C:\\Users\\trent\\OneDrive\\Desktop\\Fonts\\610068__bloodpixelhero__game-music-loop-7.wav"));
                clip = AudioSystem.getClip();
                clip.open(audioInputStream);
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        public void play() {
        	//start playing the music in a loop
            if (clip != null) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        }

        public void stop() {
        	//stop and close the music
            if (clip != null) {
                clip.stop();
                clip.close();
            }
        }
    }

    
    }