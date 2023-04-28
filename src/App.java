import java.awt.*;
import javax.swing.*;

//Class for creating and initializing the window
public class App {
    public static void main(String[] args) throws Exception {
        JFrame f = new JFrame();

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setResizable(false);
        MyPanel panel = new MyPanel();
        f.add(panel);
        f.pack();
        f.setVisible(true);

        while (true)
        {
            panel.repaint();
            Thread.sleep(10);
        }
    }
}

class MyPanel extends JPanel {

    int WINDOW_WIDTH = 500; //The width and height of the window
    int WINDOW_HEIGHT = 500;
    int GRID_WIDTH = 100; //The width and height of the grid in cells
    int GRID_HEIGHT = 100;
    boolean SHOW_VALUES = true; //If there should be a grid showing the value of each of the grid cells
    double ISO_LEVEL = 0.5; //A value for the linear interpolation algoritm
    double zVal = 0; //The z value for the noise, which will be incremented to change the noise
    double[][] grid = new double[GRID_WIDTH][GRID_HEIGHT]; //The 2d array for the values of each of the cells
    NoiseGenerator noise = new NoiseGenerator(); //A new NoiseGenerator for generating noise
    double rez = WINDOW_WIDTH/GRID_WIDTH; // The width and height of each cell
    boolean interpolation = true; //Whether it interpolates between the nodes

    public MyPanel() {
        setBorder(BorderFactory.createLineBorder(Color.black));
        initializeGrid();
        generateNoise();
    }

    //Initialize the grid by setting all of the values to 0
    private void initializeGrid() {
        for (int x = 0; x < GRID_WIDTH; x++)
        {
            for (int y = 0; y < GRID_HEIGHT; y++)
            {
                grid[x][y] = 0;
            }
        }
    }

    //Use the NoiseGenerator to generate a value for each cell based on their position and the z value
    private void generateNoise() {
        for (int x = 0; x < GRID_WIDTH; x++)
        {
            for (int y = 0; y < GRID_HEIGHT; y++)
            {
                grid[x][y] = (noise.noise(x, y, zVal) + 1)/2;
            }
        }
    }

    //This is a function that graws a square for each grid cell to show their value
    private void drawGrid(Graphics g) {
        for (int x = 0; x < GRID_WIDTH; x++)
        {
            for (int y = 0; y < GRID_HEIGHT; y++)
            {
              g.setColor(new Color(clamp((float)grid[x][y], 0f, 1f), clamp((float)grid[x][y], 0f, 1f), clamp((float)grid[x][y], 0f, 1f))); //Setting the color to a grayscale based on the value of the cell
              g.fillRect(x * (WINDOW_WIDTH/GRID_WIDTH), y * (WINDOW_HEIGHT/GRID_HEIGHT), WINDOW_WIDTH/GRID_WIDTH, WINDOW_HEIGHT/GRID_HEIGHT);
            }
        }
    }

    //The function that generates the lines based on the grid cell values and the marching squares algorithm
    public void generateMarching(Graphics g)
    {
    	//looping through all of the rows and columns
    	for (int i = 0; i < GRID_WIDTH-1; i++)
    	{
    		for (int j = 0; j < GRID_HEIGHT-1; j++)
    		{
    			//getting the actual x and y coordinate value on the window
    			int x = (int)(i * rez);
    			int y = (int)(j * rez);

    			//creating a vector for the position
    			vector pos = new vector(x, y);
          
          vector a, b, c, d;
//        A----a----B
//        |         |
//        d         b
//        |         |
//        D----c----C
          if (interpolation)
          {
            //here, we are calculating the mid points of the cell that we are calculating for, and we are also using a linear interpolation algorithm to make the lines look smoother, and rounded out
    			  a = VertexInterp(ISO_LEVEL, pos, new vector(x + rez, y), (double)grid[i][j], (double)grid[i + 1][j]);
    		    b = VertexInterp(ISO_LEVEL, new vector(x + rez, y), new vector(x + rez, y + rez), (double)grid[i + 1][j], (double)grid[i + 1][j + 1]);
    		    c = VertexInterp(ISO_LEVEL, new vector(x, y + rez), new vector(x + rez, y + rez), (double)grid[i][j + 1], (double)grid[i + 1][j + 1]);
    		    d = VertexInterp(ISO_LEVEL, pos, new vector(x, y + rez), (double)grid[i][j], (double)grid[i][j + 1]);
          } else {
            a = new vector(pos.x + rez/2, pos.y);
            b = new vector(pos.x + rez, pos.y + rez/2);
            c = new vector(pos.x + rez/2, pos.y + rez);
            d = new vector(pos.x, pos.y + rez/2);
          }
    			

    		    //getting the state in which the cell is in
    			int state = getState((int)Math.round(grid[i][j]), (int)Math.round(grid[i + 1][j]), (int)Math.round(grid[i + 1][j + 1]), (int)Math.round(grid[i][j + 1]));

    			//using a switch statement to draw the lines based on which state the cell is in
    			switch (state) {
    		      case 1:  
    		        drawLine(c, d, g);
    		        break;
    		      case 2:  
    		        drawLine(b, c, g);
    		        break;
    		      case 3:  
    		        drawLine(b, d, g);
    		        break;
    		      case 4:  
    		        drawLine(a, b, g);
    		        break;
    		      case 5:  
    		        drawLine(a, d, g);
    		        drawLine(b, c, g);
    		        break;
    		      case 6:  
    		        drawLine(a, c, g);
    		        break;
    		      case 7:  
    		        drawLine(a, d, g);
    		        break;
    		      case 8:  
    		        drawLine(a, d, g);
    		        break;
    		      case 9:  
    		        drawLine(a, c, g);
    		        break;
    		      case 10: 
    		        drawLine(a, b, g);
    		        drawLine(c, d, g);
    		        break;
    		      case 11: 
    		        drawLine(a, b, g);
    		        break;
    		      case 12: 
    		        drawLine(b, d, g);
    		        break;
    		      case 13: 
    		        drawLine(b, c, g);
    		        break;
    		      case 14: 
    		        drawLine(c, d, g);
    		        break;
    		      case 15:
    		    	break;
    		  }
    		}
    	}
    }
    
    //a method to draw lines easier
    public void drawLine(vector v1, vector v2, Graphics g)
    {
    	g.setColor(new Color(0, 0, 0));
    	g.drawLine((int)v1.x, (int)v1.y, (int)v2.x, (int)v2.y);
    }
    
    //the method that is used to convert the state from binary into base ten
    public int getState(int a, int b, int c, int d) {
    	  return a * 8 + b * 4  + c * 2 + d * 1;
    }
    

    //Function to clamp a value between a min and a max
    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    //the method where we calculate the place where we should draw the line, based on the two values on either side
    public vector VertexInterp(double isolevel, vector p1, vector p2, double valp1, double valp2) {
    	   double mu;
    	   vector p = new vector(0, 0);

    	   if (Math.abs(isolevel-valp1) < 0.00001)
    	      return(p1);
    	   if (Math.abs(isolevel-valp2) < 0.00001)
    	      return(p2);
    	   if (Math.abs(valp1-valp2) < 0.00001)
    	      return(p1);
    	   mu = (isolevel - valp1) / (valp2 - valp1);
    	   p.x = (float) (p1.x + mu * (p2.x - p1.x));
    	   p.y = (float) (p1.y + mu * (p2.y - p1.y));
    	   //p.z = p1.z + mu * (p2.z - p1.z);

    	   return(p);
    }

    public Dimension getPreferredSize() {
        return new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    //Function where we actually paint the window
    public void paintComponent(Graphics g) {
        super.paintComponent(g); //Clear the screen
        zVal+=0.1; //Change the z value so that the noise changes each frame
        generateNoise();

        if (SHOW_VALUES)
        {
            drawGrid(g);
        }

        generateMarching(g);
    }

}

//A class to store the x and y coordinates of a vector
class vector {
    public double x = 0;
    public double y = 0;

    public vector(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
}