package ui.mainFrame;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import friends.Friend;

 

public class FinalTableModelFriendsList extends AbstractTableModel {


	private static final long serialVersionUID = 1L;
	
	private List<Friend> li = new ArrayList<Friend>();
    private String[] columnNames = {"", "Friends", "id"};

    public FinalTableModelFriendsList(List<Friend> list){
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
    	Friend f = li.get(rowIndex);
        switch (columnIndex) {
            case 0: 
                return f.isOnline();
            case 1:
            	return f.getUsername();
            case 2:
            	return f.getId();
            	
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
             case 2:
                 return Integer.class;

             }
             return null;
      }
 }