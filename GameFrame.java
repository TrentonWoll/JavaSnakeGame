import javax.swing.JFrame;

public class GameFrame extends JFrame{

	GameFrame(){
		
		//add game panel to the JFrame
		this.add(new GamePanel());
		this.setTitle("Snake");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//stops the user from resizing the JFrame
		this.setResizable(false);
		//fits everything we add into the JFrame
		this.pack();
		//makes the JFrame visible
		this.setVisible(true);
		//put the JFrame in the center of the screen
		this.setLocationRelativeTo(null);
		
	}
}