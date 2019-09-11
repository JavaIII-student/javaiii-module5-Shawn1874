package com.shawndfox.moviesdb;

// Fig. 24.25: ResultSetTableModel.java
// A TableModel that supplies ResultSet data to a JTable.
import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.swing.table.AbstractTableModel;

/**
 * Implementation of AbstractTableModel which serves as a data model for the JTable
 * control used within the application.  Adapted from example code produced originally
 * by Deitel & Associates, Inc. and * Pearson Education.
 * 
 * @author Deitel & Associates, Inc. and * Pearson Education
 * @author Shawn D. Fox
 */
public class ResultSetTableModel extends AbstractTableModel
{

   private final Connection connection;
   private final Statement statement;
   private ResultSet resultSet;
   private ResultSetMetaData metaData;
   private int numberOfRows;

   // keep track of database connection status 
   private boolean connectedToDatabase = false;

   /**
    * Constructor initializes resultSet and obtains its metadata object
    * 
    * @param url connection string for the database
    * @throws SQLException 
    */
   public ResultSetTableModel(String url) throws SQLException
   {
      // connect to database
      connection = DriverManager.getConnection(url);

      // create Statement to query database                             
      statement = connection.createStatement(
              ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

      // update database connection status
      connectedToDatabase = true;
   }

   /**
    * Get class that represents column type
    * @param column 0 based index of the column
    * @return
    * @throws IllegalStateException 
    */
   @Override
   public Class getColumnClass(int column) throws IllegalStateException
   {
      // ensure database connection is available                      
      if (!connectedToDatabase) {
         throw new IllegalStateException("Not Connected to Database");
      }

      // determine Java class of column
      try {
         String className = metaData.getColumnClassName(column + 1);

         // return Class object that represents className
         return Class.forName(className);
      }
      catch (Exception exception) {
         exception.printStackTrace();
      }

      return Object.class; // if problems occur above, assume type Object
   }

   /**
    * Gets the number of columns within the table that are currently displayed
    * 
    * @return number of columns in ResultSet
    * @throws IllegalStateException 
    */
   @Override
   public int getColumnCount() throws IllegalStateException
   {
      // ensure database connection is available
      if (!connectedToDatabase) {
         throw new IllegalStateException("Not Connected to Database");
      }

      // determine number of columns
      try {
         return metaData.getColumnCount();
      }
      catch (SQLException sqlException) {
         sqlException.printStackTrace();
      }

      return 0; // if problems occur above, return 0 for number of columns
   }

   /**
    * Gets the name of the column from the ResultSet of the last query executed
    * successfully.
    * 
    * @param column 0 based index of the column
    * @return the name to display as the column header
    */
   @Override
   public String getColumnName(int column) throws IllegalStateException
   {
      String name = "";
      
      // ensure database connection is available
      if (connectedToDatabase) {
         // determine column name
         try {
            name = metaData.getColumnName(column + 1);
         }
         catch (SQLException sqlException) {
            sqlException.printStackTrace();
         }
      }

      return name; // if problems, return empty string for column name
   }

   /**
    * Gets the number of rows within the table that are currently displayed
    * 
    * @return number of rows in ResultSet
    * @throws IllegalStateException 
    */
   @Override
   public int getRowCount() throws IllegalStateException
   {
      // ensure database connection is available
      if (!connectedToDatabase) {
         throw new IllegalStateException("Not Connected to Database");
      }

      return numberOfRows;
   }

   /**
    * obtain value in particular row and column
    * @param row 0 based index of the row
    * @param column 0 based index of the column
    * @return the object at the intersection of the specified location
    * @throws IllegalStateException if not connected to the database
    */
   @Override
   public Object getValueAt(int row, int column)
           throws IllegalStateException
   {

      // ensure database connection is available
      if (!connectedToDatabase) {
         throw new IllegalStateException("Not Connected to Database");
      }

      // obtain a value at specified ResultSet row and column
      try {
         resultSet.absolute(row + 1);
         return resultSet.getObject(column + 1);
      }
      catch (SQLException sqlException) {
         sqlException.printStackTrace();
      }

      return ""; // if problems, return empty string object
   }

   /**
    * Executes the specified query, and refresh the table if connected to the database.
    * 
    * @param query The query to execute
    * @throws SQLException if the query execution fails
    * @throws IllegalStateException if not connected to a database
    */
   public final void setQuery(String query)
           throws SQLException, IllegalStateException
   {
      // ensure database connection is available
      if (!connectedToDatabase) {
         throw new IllegalStateException("Not Connected to Database");
      }

      // specify query and execute it
      resultSet = statement.executeQuery(query);

      // obtain metadata for ResultSet
      metaData = resultSet.getMetaData();

      // determine number of rows in ResultSet
      resultSet.last(); // move to last row
      numberOfRows = resultSet.getRow(); // get row number      

      fireTableStructureChanged(); // notify JTable that model has changed
   }

   /**
    * Close all resources which includes disconnecting from the database and 
    * setting the state of the object to disconnected.
    * 
    */             
   public void disconnectFromDatabase()
   {
      if (connectedToDatabase) {
         // close Statement and Connection            
         try {
            if(resultSet != null) {
               resultSet.close();
            }
            statement.close();
            connection.close();
         }
         catch (SQLException sqlException) {
            sqlException.printStackTrace();
         }
         finally { // update database connection status
            connectedToDatabase = false;
         }
      }
   }
}

/**
 * ************************************************************************
 * (C) Copyright 1992-2018 by Deitel & Associates, Inc. and * Pearson Education,
 * Inc. All Rights Reserved. * * DISCLAIMER: The authors and publisher of this
 * book have used their * best efforts in preparing the book. These efforts
 * include the * development, research, and testing of the theories and programs
 * * to determine their effectiveness. The authors and publisher make * no
 * warranty of any kind, expressed or implied, with regard to these * programs
 * or to the documentation contained in these books. The authors * and publisher
 * shall not be liable in any event for incidental or * consequential damages in
 * connection with, or arising out of, the * furnishing, performance, or use of
 * these programs. *
 ************************************************************************
 */
