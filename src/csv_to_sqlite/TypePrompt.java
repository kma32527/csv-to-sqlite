package csv_to_sqlite;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.*;

public class TypePrompt implements WindowListener{
	
	String[] cols;
	String[] colTypes = Variable.validTypes;
	String[] selectedFields;
	JComboBox<String>[] colBox;
	
	public TypePrompt(ArrayList<String> cols) {
		this.cols=new String[cols.size()];
		for(int i=0; i<cols.size(); i++)
			this.cols[i]=cols.get(i);
		selectedFields = new String[cols.size()];
		colBox = new JComboBox[cols.size()];
	}
	
	public TypePrompt(String[] cols) {
		this.cols=cols;
		selectedFields = new String[cols.length];
		colBox = new JComboBox[cols.length];
	}
	
	/**
	 * Launches a prompt allowing user to select the type of each column
	 */
	public String[] launchApp() {
		JFrame test = new JFrame("Column type selection");
		JDialog window=new JDialog(test, "Select column types");
		
		JPanel selectPanel = new JPanel();
		GridLayout grid = new GridLayout(cols.length + 1, 3);
		selectPanel.setLayout(grid);


		for(int i=0; i< colBox.length; i++)
			colBox[i] = new JComboBox<String>(colTypes);
		selectPanel.add(new JLabel("Index"));
		selectPanel.add(new JLabel("Name"));
		selectPanel.add(new JLabel("Type"));
		for(int i=0; i<colBox.length; i++) {
			String name=(cols[i].length() < 10) ? cols[i] : cols[i].substring(0, 10) + "...";
			selectPanel.add(new JLabel(Integer.toString(i+1)));			
			selectPanel.add(new JLabel(name));
			selectPanel.add(colBox[i]);
		}
		JScrollPane scroll=new JScrollPane(selectPanel);
		window.add(scroll);
		
		window.setModal(true);
		
		window.setAlwaysOnTop(true);
		window.setResizable(false);
		window.addWindowListener(this);
		test.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		window.pack();
		window.setSize(500, 400);
		window.setVisible(true);
		
		return selectedFields;
	}
	
	public static void main(String args[]) {
		ArrayList<String>cols=new ArrayList<String>();
		for(int i=0; i<20; i++) {
			cols.add("Var" + Integer.toString(i));
		}
		TypePrompt test=new TypePrompt(cols);
		test.launchApp();
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Retrieves the selected fields from the prompt on window close
	 */
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		for(int i=0; i<selectedFields.length; i++) {
			selectedFields[i]=colBox[i].getSelectedItem().toString();
		}
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		//System.exit(0);
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
}
