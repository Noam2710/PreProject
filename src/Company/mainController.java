package Company;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;


public class mainController extends Application implements Initializable {

    RecoEngine recoEngine;

    public static Stage st;
    //public static String BackgroundPath = "http://up419.siz.co.il/up3/vygmmzyjzyni.jpg";
    @FXML public AnchorPane bigAnchor;
    @FXML public ListView moviesList;
    @FXML TextField  genreText,movieText, movieText2;
    @FXML Button addMovieButton,addGenreButton, addMovieButton2;
    public static String genreString;
    public static String movieString;
    public static String movieString2;


    public static void main(String[] args) throws Exception {
        /*
        int chose = s.nextInt();
        RecoEngine re = new RecoEngine();
        re.InitUserMovies(chose);
        re.RecommendMoviesForUser(chose);
        */
        launch(args);

    }

    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
        primaryStage.setTitle("Recommendation System");
        primaryStage.setScene(new Scene(root, 1033, 700));
        st=primaryStage;
        st.show();



    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        addMovieButton2.setVisible(false);
        movieText2.setVisible(false);
        try {
            recoEngine = new RecoEngine();
            initGenres();
            } catch (Exception e) {
            e.printStackTrace();
        }
        //bigAnchor.setBackground();
    }

    private void initGenres() {
        moviesList.getItems().clear();
        moviesList.getItems().add("לבחירה על פי ז'אנר, עליך להקליד את מספר הז'אנר");
        List<String> list = new ArrayList<>(recoEngine.genres);
        for(int i = 1 ; i <= recoEngine.genres.size(); i++)
            moviesList.getItems().add(i +". "+list.get(i-1));
    }

    public void addMovie(ActionEvent actionEvent) {
        movieString = movieText.getText();
        recoEngine.InitUserMovies(1,movieString);

        if (recoEngine.optinal != null) {
            addMovieButton2.setVisible(true);
            movieText2.setVisible(true);
            moviesList.getItems().clear();
            List<List<String>> list = new ArrayList<List<String>>(recoEngine.optinal);
            //recoEngine.UpdateUserMovies(recoEngine.optinal);
            for (int i = 1; i <= recoEngine.optinal.size(); i++)
                moviesList.getItems().add(i + ". " + list.get(i - 1).get(1));
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Alert!");
            alert.setHeaderText("לא נמצאו תוצאות לסרט שחיפשת / חיפשת סרט שלא קיים");
            alert.showAndWait();
        }
    }

    public void chooseGenre(ActionEvent actionEvent) {
        List finalMovies = new ArrayList();
        if (genreText.getText().length() < 1 || !isNumeric(genreText.getText())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Alert!");
            alert.setHeaderText("לא בחרת מספר ז'אנר / בחרת ערך לא תקין");
            alert.showAndWait();
        }
        else
        {
            genreString = genreText.getText();
            recoEngine.InitUserMovies(2, movieString);
            finalMovies = recoEngine.RecommendMoviesForUser(2);
            moviesList.getItems().clear();
            moviesList.getItems().addAll(finalMovies);

        }
    }

    public static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public void resetSelection(ActionEvent actionEvent) {
        initGenres();
    }

    public void addMovie2(ActionEvent actionEvent) {

        List finalMovies = new ArrayList();
        movieString2 = movieText2.getText();

        if (isNumeric(movieString2))
        {
            if (Integer.parseInt(mainController.movieString2)<=recoEngine.optinal.size()) {
                movieText2.setVisible(false);
                addMovieButton2.setVisible(false);
                recoEngine.UpdateUserMovies(recoEngine.optinal);
                finalMovies = recoEngine.RecommendMoviesForUser(1);
                moviesList.getItems().clear();
                moviesList.getItems().addAll(finalMovies);
            }
            else
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Alert!");
                alert.setHeaderText("בחרת מספר הגדול ממספר האופציות לבחירה");
                alert.showAndWait();
            }
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Alert!");
            alert.setHeaderText(" הזנת ערך לא תקין");
            alert.showAndWait();
        }

    }
}
