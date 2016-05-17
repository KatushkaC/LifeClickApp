/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lifeclickapp;

import eu.lifeclick.backend.Request;
import eu.lifeclick.backend.RescuerManager;
import eu.lifeclick.backend.User;
import eu.lifeclick.backend.UserManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.sql.DataSource;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author xcambal
 */
public class UserTableModel extends AbstractTableModel {
    
    private List<User> users = new ArrayList<>();
    private UserManager manager;
    private RescuerManager rescuers;
    
    public void initUserManager(DataSource ds){
        manager = new UserManager(ds);
        rescuers = new RescuerManager(ds);
    }
    
    

    
    public void setAllUsers() {
        try {
            this.users = manager.getAll();
            fireTableDataChanged();
        } catch (Exception ex) {
            LifeClickApp.log.error("Error retrieving users.", ex);
        }
    }
    
    public void setUsersByNameOrEmail(String searchStr) {
        try {
            this.users = manager.getUserByNameOrEmail(searchStr);
            fireTableDataChanged();
        } catch (Exception ex) {
            //log
        }
    }

    public void addUser(User user){
        try {
            manager.create(user);
            this.users.add(user);
            int lastRow = users.size() - 1;
            fireTableRowsInserted(lastRow, lastRow);
        } catch (Exception ex){
            //log
        }
    }
    
    public void setUsersById(Long id){
        try {
            this.users = (List<User>) manager.get(id);
            fireTableDataChanged();
        } catch (Exception ex){
            //log
        }
    }
    
    public void setUsersByRequest(Request req){
        (new RescuersFinder(req)).perform();
        
        fireTableDataChanged();
    }
    
    public void deleteUser(User user){
        //....
    }
    
    public User getUserAt(int rowIndex){
        return users.get(rowIndex);
    }
    
    public void modifyUser(int rowIndex){
        //setValueAt()
        //manager.update( getUserAt(rowIndex); );
    }
    
    @Override
    public int getRowCount() {
        return users.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        User user = users.get(rowIndex);
        
        switch(columnIndex) {
            case 0 : return user.getFirstName();
            case 1 : return user.getLastName();
            case 2 : return user.getEmail();
            default : throw new IllegalArgumentException("Invalid columnIndex.");
        }
        
    }
    
    
    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0: return "First name";
            case 1: return "Last name";
            case 2: return "Email";
            default: throw new IllegalArgumentException("Invalid columnIndex.");
        }
    }
    
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 1:
            case 2:
                return String.class;
            default:
                throw new IllegalArgumentException("Invalid columnIndex.");
        }
    }
    
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        User user = users.get(rowIndex);
        switch (columnIndex) {
            case 0:
                user.setFirstName((String) aValue);
                break;
            case 1:
                user.setLastName((String) aValue);
                break;
            case 2:
                user.setEmail((String) aValue);
                break;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 1:
                return true;
            case 2:
                return false;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }


    
//SWING WORKERS:    
    
    
    
    
    
    
    private class RescuersFinder {
        private final Long reqId;


        public RescuersFinder(Request req) {
            this.reqId = req.getId();
        }

        private class RescuersByRequestSwingWorker extends SwingWorker<List<User>,Void> {
            @Override    
            protected List<User> doInBackground() throws Exception {
                List<User> internalUsers = UserTableModel.this.rescuers.get(reqId);
                return internalUsers;
            }

            @Override
            protected void done(){
                try {
                    UserTableModel.this.users = get();
                    fireTableDataChanged();
                } catch (InterruptedException | ExecutionException ex) {
                    LifeClickApp.log.error("Error retrieving users for particular request.", ex);
                }
            }
        }

        public void perform(){
            (new RescuersByRequestSwingWorker()).execute();
        }
    }

}