/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shawndfox.moviesdb;

import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Shawn D. Fox
 */
public class TableModelTests
{
   
   String testConnection = "jdbc:derby:Test;create=true";
   String selectAll = "select * from names";
   public TableModelTests()
   {
   }
   
   public void testConnectionFailures() throws SQLException {
      SQLException e = assertThrows(SQLException.class, () -> new ResultSetTableModel("invalid"));
      
      ResultSetTableModel tableModel = new ResultSetTableModel(testConnection);
      tableModel.disconnectFromDatabase();
      
      IllegalStateException illegalState = assertThrows(IllegalStateException.class, () -> tableModel.setQuery(selectAll));
      assertEquals(illegalState.getMessage(), "Not Connected to Database");
      illegalState = assertThrows(IllegalStateException.class, () -> tableModel.getValueAt(0, 0));
      assertEquals(illegalState.getMessage(), "Not Connected to Database");
      illegalState = assertThrows(IllegalStateException.class, () -> tableModel.getRowCount());
      assertEquals(illegalState.getMessage(), "Not Connected to Database");
      illegalState = assertThrows(IllegalStateException.class, () -> tableModel.getColumnCount());
      assertEquals(illegalState.getMessage(), "Not Connected to Database");
      illegalState = assertThrows(IllegalStateException.class, () -> tableModel.getColumnClass(0));
      assertEquals(illegalState.getMessage(), "Not Connected to Database");
      
      String name = tableModel.getColumnName(0);
      assertTrue(name.isEmpty());
   }
}
