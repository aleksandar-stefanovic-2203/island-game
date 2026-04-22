package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class Game extends Frame {
		
	private static int numberOfCells = 30;
	private static String mapPath = "Map.txt";
	private Panel mapPanel = new Panel(new GridLayout(30, 30));
	private Map map = new Map(numberOfCells, numberOfCells);
	private Button restartButton = new Button("Restart game");
	private Label numberOfAttemptsLabel = new Label("Number of attempts: 3");
	private Label message = new Label();
	private Label timeLabel = new Label();
	
	private int numberOfWins = 0;
	private int numberOfLosses = 0;
	private ArrayList<Integer> times = new ArrayList<>();
	
	private Timer timer = new Timer(timeLabel);
	
	private Menu menu;
	
	private int numberOfAttempts;
	
	public Game(Menu menu) {
		this.menu = menu;
		loadStatistics();
		
		generateMap();
		
		setLocation(500, 50);
		setResizable(false);
		setTitle("Game");
		
		fillWindow();
		pack();
		
		addListeners();		
		
		setVisible(true);
		
		timer.start();
		timer.activate();
	}
	
	private void loadStatistics() {
		File file = new File("PlayerStatistics.txt");
		if(!file.exists()) {
			try {
				file.createNewFile();
				FileWriter fw = new FileWriter(file);
				fw.write("0,0");
				fw.close();
			} catch (IOException e) {
				System.out.println("Error with opening the file.");
			}
			
		}
		
		try {
			FileReader fr = new FileReader(file);
			StringBuilder sb = new StringBuilder();
			int ch;
			while((ch = fr.read()) != -1) sb.append((char)ch);
			fr.close();
			String content = sb.toString();
			String[] elements = content.split(",");
			this.numberOfWins = Integer.parseInt(elements[0]);
			this.numberOfLosses = Integer.parseInt(elements[1]);
			
			for(int i = 2; i < elements.length; i++) {
				times.add(Integer.parseInt(elements[i]));
			}
			
		} catch (IOException e) {
			System.out.println("Error with reading from the file.");
		}
		
	}

	@SuppressWarnings("deprecation")
	private void generateMap() {
		Island.resetNextID();
		this.numberOfAttempts = 3;
		numberOfAttemptsLabel.setText("Number of attempts: " + numberOfAttempts);
		message.setText("");
		URL url;
		HttpURLConnection con;
		try {	
			/*
			 * This part was used while the challenge was still active.
			 * Since the link has expired, loading a map is done from a file Map.txt
			 */			
//			url = new URL("https://jobfair.nordeus.com/jf24-fullstack-challenge/test");
//			con = (HttpURLConnection) url.openConnection();
//			con.setRequestMethod("GET");
//			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(mapPath))));
			String inputLine;
			String words[] = new String[numberOfCells];
			int i = 0;
			while ((inputLine = in.readLine()) != null && i < numberOfCells) {
				words = inputLine.split(" ", numberOfCells);
				for(int j = 0; j < numberOfCells; j++) {
					int height = Integer.parseInt(words[j]);
					if(height == 0) map.cells[i][j] = new Water();
					else map.cells[i][j] = new Land(height);
				}
				
				i++;
			}
			
			in.close();
			
			findIslands();
						
		} catch (Exception e) { System.out.println(e.getMessage()); }	
	}
	
	private void addListeners() {
		restartButton.addActionListener((ae) -> {
			restartButton.setEnabled(false);
			Cell[][] oldCells = new Cell[numberOfCells][numberOfCells];
			for(int i = 0; i < numberOfCells; i++)
				for(int j = 0; j < numberOfCells; j++) {
					oldCells[i][j] = map.cells[i][j];
					mapPanel.remove(map.cells[i][j]);					
				}
			map.deleteMap();
			generateMap();
			timer.reset();
			timer.activate();
			addCellListeners();
			for(int i = 0; i < numberOfCells; i++)
				for(int j = 0; j < numberOfCells; j++) {
					map.cells[i][j].setBounds(oldCells[i][j].getBounds());
					mapPanel.add(map.cells[i][j]);
				}
		});
		
		addCellListeners();
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if(timer != null) timer.interrupt();
				map.deleteMap();
				
				saveStatistics();
				
				dispose();
				menu.setVisible(true);
			}

			private void saveStatistics() {
				File file = new File("PlayerStatistics.txt");
				if(!file.exists()) {
					try {
						file.createNewFile();						
					} catch (IOException e) {
						System.out.println("Error with opening the file.");
					}
					
				}
				
				String content = numberOfWins + "," + numberOfLosses;
				for(Integer i: times) {
					content += "," + i;
				}
				try {
					FileWriter fw = new FileWriter(file);
					fw.write(content);
					fw.close();
				} catch (IOException e) {
					System.out.println("Error when writing into a file.");					
				}
				
			}
		});
	}

	private void addCellListeners() {
		for(int i = 0; i < numberOfCells; i++)
			for(int j = 0; j < numberOfCells; j++) {
				final int ii = i;
				final int jj = j;
				map.cells[i][j].addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {						
						if(map.cells[ii][jj] instanceof Water) message.setText("Click on an island!");
						else {
							numberOfAttempts--;
							int id = ((Land) map.cells[ii][jj]).getIslandID();
							if(((Land) map.cells[ii][jj]).getIslandID() == Map.getHighestIslandID()) {
								mark(id, Color.GREEN);
								message.setText("You won!");																					
								deactivateMap();
								
								numberOfWins++;
								times.add(timer.getTimeInSeconds());
								
								restartButton.setEnabled(true);
							} else {
								 if(numberOfAttempts == 0) {
									 mark(id, Color.RED);
									 message.setText("Game over!");
									 
									 numberOfLosses++;
									 
									 numberOfAttemptsLabel.setText("Number of attempts: 0");
									 deactivateMap();
									 restartButton.setEnabled(true);
								 }
								 else {
									 mark(id, Color.RED);
									 message.setText("You are wrong, try again!");
									 numberOfAttemptsLabel.setText("Number of attempts: " + numberOfAttempts);
								 }
							}
							map.refresh();
						}						
					}

					private void mark(int id, Color color) {
						for(int a = 0; a < numberOfCells; a++)
							for(int b = 0; b < numberOfCells; b++)
								if(map.cells[a][b] instanceof Land && ((Land) map.cells[a][b]).getIslandID() == id) map.cells[a][b].mark(color);
					}
				});
			}
	}

	protected void deactivateMap() {
		timer.pause();
		for(int i = 0; i < numberOfCells; i++)
			for(int j = 0; j < numberOfCells; j++)
				map.cells[i][j].setEnabled(false);
	}

	private void findIslands() {
		for(int i = 0; i < numberOfCells; i++)
			for(int j = 0; j < numberOfCells; j++) {
				if(map.cells[i][j] instanceof Land && ((Land) map.cells[i][j]).getIslandID() == -1) {
					Island island = new Island(map);
					map.addIsland(island);
					island.addLand(i, j);
					island.calculateAverageHeight();
					Map.setMaximumAverageHeight(island);
				}
			}
	}

	private void fillWindow() {
		Cell[][] cells = map.cells;
		for(int i = 0; i < cells.length; i++)
			for(int j = 0; j < cells[i].length; j++){
				mapPanel.add(cells[i][j]);
			}
		this.add(mapPanel, BorderLayout.CENTER);
		Panel controlPanel = new Panel(new GridLayout(0, 1));
		restartButton.setEnabled(false);
		controlPanel.add(numberOfAttemptsLabel);
		controlPanel.add(message);
		controlPanel.add(timeLabel);
		controlPanel.add(restartButton);
		this.add(controlPanel, BorderLayout.EAST);
	}	

}
