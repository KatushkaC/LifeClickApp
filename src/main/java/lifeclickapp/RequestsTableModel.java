/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lifeclickapp;

import eu.lifeclick.backend.Request;
import eu.lifeclick.backend.RequestManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.sql.DataSource;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author xcambal
 */
public class RequestsTableModel extends AbstractTableModel {
    
    private List<Request> requests = new ArrayList<>();
    private RequestManager manager;
    
    private class AllRequestsSwingWorker extends SwingWorker<List<Request>,Void> {
        @Override    
        protected List<Request> doInBackground() throws Exception {
            List<Request> internalReqs = manager.getAll();
            return internalReqs;
        }
        
        @Override    
        protected void done() {
            try {
                requests = get();
                int lastRow = requests.size() - 1;
                fireTableRowsInserted(lastRow, lastRow);
                
            } catch (ExecutionException ex) {
                //jTextArea.append("Exception thrown in doInBackground(): " + ex.getCause() + "\n"); //radsi zalogovat...
            } catch (InterruptedException ex) {
                throw new AssertionError("Operation interrupted (this should never happen)",ex);
            }
        }
    }

    
    private class NewestRequestsSwingWorker extends SwingWorker<List<Request>,Void> {
        @Override    
        protected List<Request> doInBackground() throws Exception {
            List<Request> internalReqs = manager.getLast();
            return internalReqs;
        }
        
        @Override    
        protected void done() {
            try {
                requests = get();
                int lastRow = requests.size() - 1;
                fireTableRowsInserted(lastRow, lastRow);
                
            } catch (ExecutionException ex) {
                //jTextArea.append("Exception thrown in doInBackground(): " + ex.getCause() + "\n"); //radsi zalogovat...
            } catch (InterruptedException ex) {
                throw new AssertionError("Operation interrupted (this should never happen)",ex);
            }
        }
    }
    
    
    
    public void initRequestManager(DataSource ds){
        manager = new RequestManager(ds);
    }
    
    //public List<Request> getRequests() {
    //    return requests;
    //}

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }
    
    public void addRequest(Request request){
        try {
            manager.create(request);
            requests.add(request);

            int lastRow = requests.size() - 1;
            fireTableRowsInserted(lastRow, lastRow);
        } catch (Exception ex){
            //log
        }
    }
    
    public void setAllRequests(){
        //requests = manager.getAll();
        (new AllRequestsSwingWorker()).execute();
    }
    
    public void setRequestsByUser(String userName){
        RequestByUserSearcher searcher = new RequestByUserSearcher(manager, userName);
        searcher.perform();
        //requests = searcher.getReqs();
    }
    
    public void setNewestRequests(){
        (new NewestRequestsSwingWorker()).execute();
    }
     
    public void removeRequestAt(int rowIndex){
        Request toBeDeleted = requests.remove(rowIndex);
        (new RequestDeleter(manager, toBeDeleted)).perform();
        
        fireTableRowsDeleted(rowIndex, rowIndex);
    }
    
    public Request getRequestAt(int rowIndex){
        return requests.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return requests.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }
   
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Request request = requests.get(rowIndex);
        
        switch(columnIndex) {
            case 0 : return request.getTime();
            case 1 : return request.getLatitude();
            case 2 : return request.getLongitude();
            case 3 : return request.getUser().getName();// ++ " " ++ request.getUser().getLastName());
            case 4 : return request.getUser().getEmail();
            default : throw new IllegalArgumentException("Invalid columnIndex.");
        }
        
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Date";
            case 1:
                return "Latitude";
            case 2:
                return "Longitude";
            case 3:
                return "Author";
            case 4:
                return "Author's email";
            default:
                throw new IllegalArgumentException("Invalid columnIndex.");
        }
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Date.class;
            case 1:
            case 2:
                return Double.class;
            case 3:
            case 4:
                return String.class;
            default:
                throw new IllegalArgumentException("Invalid columnIndex.");
        }
    }
    
    
    //SWING WORKER SECTION:
    
    
    
    private class RequestByUserSearcher {
        private List<Request> intReqs = new ArrayList<>();
        private final RequestManager manager;
        private final String userName;
        private SwingWorker swingWorker;

        public RequestByUserSearcher(RequestManager manager, String userName) {
            this.manager = manager;
            this.userName = userName;
        }

        private class RequestsByUserSwingWorker extends SwingWorker<List<Request>,Void> {
            @Override    
            protected List<Request> doInBackground() throws Exception {
                List<Request> internalReqs = manager.getByUserName(userName);
                return internalReqs;
            }

            @Override    
            protected void done() {
                /*
                try {
        //k cemu je toto?!?            intReqs = get();
                    //je bezpecne vyvolat prekresleni tabulky
                    //getReqs();
                } catch (ExecutionException ex) {
                    //jTextArea.append("Exception thrown in doInBackground(): " + ex.getCause() + "\n"); //radsi zalogovat...
                } catch (InterruptedException ex) {
                    throw new AssertionError("Operation interrupted (this should never happen)",ex);
                }
                */
            }
        }
    
        public void perform(){
            swingWorker = new RequestsByUserSwingWorker();
            swingWorker.execute();
        }
    }

    private class RequestDeleter  {
        private final RequestManager manager;
        private final Request req;
        private SwingWorker swingWorker;

        public RequestDeleter(RequestManager manager, Request req) {
            this.manager = manager;
            this.req = req;
        }

        private class DeleteRequestSwingWorker extends SwingWorker<Integer,Void> {
            @Override
            protected Integer doInBackground() throws Exception {
                manager.remove(req.getId());
                return 0;
            }
        }

        public void perform(){
            swingWorker = new DeleteRequestSwingWorker();
            swingWorker.execute();
        }
    }
    
}