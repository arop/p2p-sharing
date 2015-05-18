package ui.mainFrame;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import friends.FriendCircle;

 

public class FinalTableModelCirclesList extends AbstractTableModel {


	private static final long serialVersionUID = 1L;
	
	private List<FriendCircle> li = new ArrayList<FriendCircle>();
    private String[] columnNames = {"Circles"};

    public FinalTableModelCirclesList(List<FriendCircle> list){
         this.li = list;
  
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
        //return 2;
    	return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
    	FriendCircle f = li.get(rowIndex);
        switch (columnIndex) {
           	case 0:
            	return f.getName();
           }
           return null;
   }

   @Override
   public Class<?> getColumnClass(int columnIndex){
          switch (columnIndex){
             case 0:
               return String.class;

             }
             return null;
      }
 }