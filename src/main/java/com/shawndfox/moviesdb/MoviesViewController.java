package com.shawndfox.moviesdb;

import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import static javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * This is the view controller for a dialog application that shows a Movies table
 * and allows the addition of movies to a database and a filtering capability using
 * SELECT SQL queries.
 * 
 * @author Shawn D. Fox
 */
public class MoviesViewController implements Initializable
{
   private static final String DATABASE_URL = "jdbc:derby:Movies;create=true";
   private static final String DEFAULT_QUERY = "SELECT * FROM MOVIES";
   private final String nameOfMoviesTable = "MOVIES";
   private final String getMovies = "Select * from MOVIES";

   private Connection connection;
   private PreparedStatement insertMovie;
   private ResultSetTableModel tableModel;
   private TableRowSorter<TableModel> sorter;

   @FXML
   private Label label;
      
   @FXML
   private BorderPane borderPane;
    
   @FXML
   private TextField descrTextField;

   @FXML
   private TextField ratingTextField;

   @FXML
   private TextField titleTextField;

    @FXML
    private TextArea queryTextArea;

    /**
     * Submits the query to the JTable model, and resets the query text to the 
     * default if the query is successful.
     * 
     * @param event 
     */
   @FXML
   void submitQueryButtonPressed(ActionEvent event)
   {
      //perform a new query
      try {
         tableModel.setQuery(queryTextArea.getText());
         queryTextArea.setText(DEFAULT_QUERY);
      } 
      catch (SQLException sqlException) {
         displayAlert(Alert.AlertType.ERROR, "Database Error", 
            sqlException.getMessage());
      } 
   }

   /**
    * The event handler for the Add Movie Button.  Validates the user inputs
    * and executes an insertion if all inputs are acceptable.
    *
    * @param event
    */
   @FXML
   void addMovie(ActionEvent event)
   {
      String ratingText = ratingTextField.getText();
      String titleText = titleTextField.getText();
      String descriptionText = descrTextField.getText();
      
      if(ratingText.isEmpty() || titleText.isEmpty() || descriptionText.isEmpty()) {
         displayAlert(Alert.AlertType.ERROR, 
                      "Invalid Entry!",
                      "All fields must be filled out and the rating must be a value from 1 - 10");
         return;
      }

      try {
         int rating = Integer.parseInt(ratingText);
         
         if(rating < 1 || rating > 10) {
            throw new NumberFormatException();
         }
         
         insertMovie.setString(1, titleText);
         insertMovie.setString(2, ratingText);
         insertMovie.setString(3, descriptionText);

         insertMovie.execute();
         ratingTextField.clear();
         titleTextField.clear();
         descrTextField.clear();
         
         tableModel.setQuery(DEFAULT_QUERY);
      }
      catch(NumberFormatException e) {
         displayAlert(Alert.AlertType.ERROR, 
                      "Invalid Entry", 
                      "Enter a rating from 1 to 10");
      }
      catch (SQLException sqlException) {
         displayAlert(Alert.AlertType.ERROR, 
                      "Database Error",
                      sqlException.getMessage());
      }
   }

   /**
    * Initialize the controller.  Create the database and the movies table if necessary.
    * Create a model for a JTable and add the table to the border pane.
    * 
    * @param url
    * @param rb 
    */
   @Override
   public void initialize(URL url, ResourceBundle rb)
   {
      try {
         connection = DriverManager.getConnection(DATABASE_URL);

         if (!moviesTableExists(connection)) {
            Statement stmt = connection.createStatement();
            System.out.println("Creating Table Movies");
            String primaryKey = "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)";
            String cmd = String.format(
                    "CREATE TABLE %s (%s, name VARCHAR(255), rating INT, description VARCHAR(255), CONSTRAINT primary_key PRIMARY KEY (id))",
                    nameOfMoviesTable,
                    primaryKey);
            stmt.execute(cmd);
         }

         // Prepare a statement for future use
         String preparedStatement = String.format("INSERT INTO %s (name, rating, description) VALUES (?, ?, ?)", nameOfMoviesTable);
         insertMovie = connection.prepareStatement(preparedStatement);
      }
      catch (SQLException sqlException) {
         displayAlert(Alert.AlertType.ERROR, 
                      "Database Error",
                      sqlException.getMessage());
         
         // No point continuing if the database can't be initialized.
         System.exit(1);
      }
      
      // create ResultSetTableModel and display database table
      try {
         // create TableModel for results of DEFAULT_QUERY
         tableModel = new ResultSetTableModel(DATABASE_URL);
         tableModel.setQuery(DEFAULT_QUERY);

         // create JTable based on the tableModel    
         JTable resultTable = new JTable(tableModel);

         // set up row sorting for JTable
         sorter = new TableRowSorter<>(tableModel);
         resultTable.setRowSorter(sorter);

         // configure SwingNode to display JTable, then add to borderPane
         SwingNode swingNode = new SwingNode();
         swingNode.setContent(new JScrollPane(resultTable));
         borderPane.setCenter(swingNode);
      }
      catch (SQLException sqlException) {
         displayAlert(Alert.AlertType.ERROR, "Database Error",
                      sqlException.getMessage());
         tableModel.disconnectFromDatabase(); // close connection  
         System.exit(1); // terminate application
      }
   }

   /**
    * Determines whether or not a table already exists within the database.
    *
    * @param conn the database connection which must have already been opened
    * @return true if the table exists, and false otherwise
    * @throws SQLException
    */
   private boolean moviesTableExists(Connection conn) throws SQLException
   {
      DatabaseMetaData dbMetaData = conn.getMetaData();
      ResultSet rs = dbMetaData.getTables(null, null, nameOfMoviesTable, null);
      boolean value = rs.next();
      return value;
   }

   /**
    * Display a dialog box with an alert message.
    * 
    * @param type a value from the javafx.scene.control.Alert.AlertType enumeration
    * @param title the title of the alert to display
    * @param message a detailed message to display
    */
   private void displayAlert(Alert.AlertType type, String title, String message) {
      Alert alert = new Alert(type);
      alert.setTitle(title);
      alert.setContentText(message);
      alert.showAndWait();
   }
}
