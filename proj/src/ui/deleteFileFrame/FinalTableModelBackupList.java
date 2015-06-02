package ui.deleteFileFrame;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class FinalTableModelBackupList extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	private List<String> li = new ArrayList<String>();
	private String[] columnNames = {"", "Files"};

	public FinalTableModelBackupList(List<String> files){
		this.li = files;
	}

	@Override
	public String getColumnName(int columnIndex){
		return columnNames[columnIndex];
	}

	@Override     
	public int getRowCount() {
		return li.size();
	}

	@Override        
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		String f = li.get(rowIndex);
		switch (columnIndex) {
		case 0: 
			return true;
		case 1:
			return f;
		}
		return null;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex){
		switch (columnIndex){
		case 0:
			return Boolean.class;
		case 1:
			return String.class;
		}
		return null;
	}
}
