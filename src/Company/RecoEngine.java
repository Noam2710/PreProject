package Company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RecoEngine {
    public static final String ANSI_RED = "\033[1;31m";;
    public static final String ANSI_RESET = "\u001B[0m";
    public static Scanner s = new Scanner(System.in);
    public static final String MOVIES_PATH = "C:\\Users\\noammo\\OneDrive - Mellanox\\Desktop\\PreProject\\Help\\ml-latest-small\\movies.csv";
    List<List<String>> MoviesPool;
    List<List<String>> UserMovies;

    public RecoEngine() throws Exception
    {
        MoviesPool = readCSVFile();
        UserMovies = new ArrayList<List<String>>();
    }

    public static void main(String[] args) throws Exception {
        PrintInfo("Welcome to our Movie's Recommendation engine, HAVE FUN");
        RecoEngine re = new RecoEngine();
        re.InitUserMovies();
    }

    private void InitUserMovies() {
        boolean KeepAsking = true;

        while(KeepAsking)
        {
            List<List<String>> OptionalMovies = GetOptionalMovies();
            this.UpdateUserMovies(OptionalMovies);
            KeepAsking = CheckIfWantMore();

        }
    }

    private String EnterMovie() {
        PrintInfo("Please Enter movie's title");
        String MovieToCheck = s.nextLine();
        while(MovieToCheck.length()==0)
            MovieToCheck = s.nextLine();
        PrintInfo("You have entered " + MovieToCheck);
        return MovieToCheck;
    }

    private boolean CheckIfWantMore() {
        PrintInfo("Do you want to Continue ?");
        PrintInfo("Enter 1 for yes , 0 otherwise");
        int chose = s.nextInt();

        if(chose==1)
            return true;
        else
            return false;
    }

    private void UpdateUserMovies(List<List<String>> optionalMovies) {
        PrintInfo(String.format("There are %s Movies - ",optionalMovies.size()));
        PrintListWithIndexes(optionalMovies);
        PrintInfo("Please enter the movie's index you want");
        int chose = s.nextInt();

        while(chose<1 || chose>optionalMovies.size()) {
            PrintInfo("Please enter a valid index");
            chose = s.nextInt();
        }

        PrintInfo(String.format("You have choosen the movie - %s ", optionalMovies.get(chose-1).get(1)));
        this.UserMovies.add(optionalMovies.get(chose-1));
        PrintInfo("You have entered so far - ");
        PrintListWithIndexes(UserMovies);

    }

    private void PrintListWithIndexes(List<List<String>> optionalMovies) {
        for(int i = 1 ; i <= optionalMovies.size(); i++)
            System.out.println(String.format("%s. %s",i,optionalMovies.get(i-1).get(1)));
    }

    private List<List<String>> GetOptionalMovies() {
        List<List<String>> OptinalMovies = new ArrayList<>();
        boolean HasMovies = false;

        while(!HasMovies) {
            String MovieToCheck = EnterMovie();
            for (List<String> movie : MoviesPool)
                if (movie.get(1).toLowerCase().contains(MovieToCheck.toLowerCase())) {
                    OptinalMovies.add(movie);
                    HasMovies = true;
                }
            if(!HasMovies)
                PrintInfo(String.format("There is no Movies with the title %s ",MovieToCheck ));
        }

        return OptinalMovies;
    }

    public static void PrintInfo(String text){

        System.out.println(ANSI_RED + text + ANSI_RESET);
    }

    private static List<List<String>> readCSVFile() throws IOException {

        String line = null;
        BufferedReader stream = null;
        List<List<String>> csvData = new ArrayList<List<String>>();

        try {
            stream = new BufferedReader(new FileReader(MOVIES_PATH));
            while ((line = stream.readLine()) != null) {
                String[] splitted = line.split(",");
                List<String> dataLine = new ArrayList<String>(splitted.length);
                for (String data : splitted) {
                    dataLine.add(data);
                    //System.out.println(data);
                }
                //System.out.println(dataLine);
                csvData.add(dataLine);
            }
        } finally {
            if (stream != null)
                stream.close();
        }

        return csvData;

    }
}
