package Company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class RecoEngine {
    public static final String ANSI_RED = "\033[1;31m";;
    public static final String ANSI_RESET = "\u001B[0m";
    public static String MOVIES_PATH;
    public static String RATING_PATH;
    public static Scanner s = new Scanner(System.in);
    List<List<String>> MoviesPool,UserMovies,RecommendMovies,Ratings,TempForLaterCheck;
    Set<String> genres;
    HashMap<Object, Object> TopGenres;


    public RecoEngine() throws Exception {
        InitPaths();
        MoviesPool = readCSVFile(MOVIES_PATH);
        Ratings = readCSVFile(RATING_PATH);
        UserMovies = new ArrayList<List<String>>();
        RecommendMovies = new ArrayList<List<String>>();
        TempForLaterCheck = new ArrayList<List<String>>();
        genres = GetAllGenres();

    }

    private void InitPaths() {
        MOVIES_PATH = System.getProperty("user.dir") + "\\Help\\ml-latest-small\\movies.csv";
        RATING_PATH = System.getProperty("user.dir") + "\\Help\\ml-latest-small\\ratings.csv";
    }

    public static void main(String[] args) throws Exception {
        PrintInfo("Welcome to our Movie's Recommendation engine, HAVE FUN");
        PrintInfo("Please press the option you want - ");
        PrintInfo("1. Enter Movies");
        PrintInfo("2. Enter Genre");
        int chose = s.nextInt();
        RecoEngine re = new RecoEngine();
        re.InitUserMovies(chose);
        re.RecommendMoviesForUser(chose);
    }

    private void RecommendMoviesForUser(int chose) {
        PrintInfo("Searching for Similar movies for the user...");
        HashMap<String, Integer> CountGenresDic;

        if(chose==1) {
            //Getting TopGenres from movies, Otherwise, the user choose to enter the genre manually.
            CountGenresDic = CountGenres(UserMovies);
            TopGenres = MapsortByValuesInteger(CountGenresDic, 1);
        }

        HashMap<String,Integer> MoviesSharedGenres  = GetSharedGenresMovies(TopGenres);
        HashMap<String,Double> SharedWithRank = AddRankToMovies(MoviesSharedGenres);
        HashMap<String,Double> TopRank = MapsortByValuesDouble(SharedWithRank,10);
        HashMap<String,Double> IDtoTitle = IDtoTitle(TopRank);
        PrintHashMapWithIndexes(IDtoTitle);
    }

    private void PrintHashMapWithIndexes(HashMap<String, Double> iDtoTitle) {
        PrintInfo("We are recommending you the following 10 movies -");
        for (int i = 1 ; i<= iDtoTitle.entrySet().size() ; i++) {
            Object movie = iDtoTitle.keySet().toArray()[i-1];
            System.out.println(String.format("%s. %s",i,movie));
        }

        }

    private HashMap<String,Double> IDtoTitle(HashMap<String, Double> topRank) {
        HashMap<String,Double> BestMovies = new HashMap<>();

        for (Map.Entry<String, Double> IDMovie : topRank.entrySet()) {
            for(List<String> movie : TempForLaterCheck)
                if(movie.get(0).equals(IDMovie.getKey()))
                    BestMovies.put(movie.get(1),IDMovie.getValue());
        }
        return BestMovies;
    }

    private HashMap<String,Double> AddRankToMovies(HashMap<String, Integer> moviesSharedGenres) {

        HashMap<String,Double> Ranks = new HashMap<>();
        HashMap<Object,Object> TopmoviesSharedGenres = MapsortByValuesInteger(moviesSharedGenres,50);

        for (Map.Entry<Object, Object> SharedMovie : TopmoviesSharedGenres.entrySet())
        {
            for(List<String> movie : Ratings)
            {
                if(movie.get(1).equals(SharedMovie.getKey()))
                    Ranks.put((String) SharedMovie.getKey(),new Double(movie.get(2)));
            }
        }

        return Ranks;
    }

    private HashMap<String,Integer> GetSharedGenresMovies(HashMap<Object, Object> topGenres) {
        HashMap<String,Integer> SharedGenresMovies = new HashMap<>();
        List <String>ListOfTopGenre = new ArrayList(topGenres.keySet());

        for(List<String> movie : MoviesPool)
        {
            String[] SpecificMovieGenres = movie.get(2).split("\\|");
            int rankOfMovie = GetRank(SpecificMovieGenres,ListOfTopGenre);
            if(rankOfMovie>0) {
                SharedGenresMovies.put(movie.get(0), rankOfMovie);
                TempForLaterCheck.add(movie);
            }
        }

        return SharedGenresMovies;
    }

    private int GetRank(String[] SpecificMovieGenres, List<String> ListOfTopGenre) {
        int count = 0;

        for(String genre : SpecificMovieGenres)
            if(ListOfTopGenre.contains(genre))
                count++;

        return count;

    }

    private HashMap<String,Integer> CountGenres(List<List<String>> userMovies) {

        HashMap<String,Integer> Count = new HashMap<>();

        for(List<String> movie : userMovies){
            String[] genres = movie.get(2).split("\\|");
            Count = UpdateMap(Count,genres);
        }

        return Count;
    }

    private HashMap<String, Integer> UpdateMap(HashMap<String, Integer> count, String[] genres) {

        for(String genre : genres) {
            try {
                count.put(genre, count.get(genre) + 1);
            }
            catch (Exception e) {
                count.put(genre,1);
            }
        }

        return count;

    }

    private void InitUserMovies(int chose) {
        boolean KeepAsking = true;

        if(chose == 1) {
            while (KeepAsking) {
                List<List<String>> OptionalMovies = GetOptionalMovies();
                this.UpdateUserMovies(OptionalMovies);
                KeepAsking = CheckIfWantMore();

            }
        }
        else{
            PrintSetWithIndexes(genres);
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
        int chose;

        chose = s.nextInt();
        if (chose == 1)
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

    private void PrintSetWithIndexes(Set<String> genres) {
        List<String> list = new ArrayList<>(genres);

        for(int i = 1 ; i <= genres.size(); i++)
            System.out.println(String.format("%s. %s",i,list.get(i-1)));

        int chose = s.nextInt();
        while(chose<1 || chose>list.size());
        PrintInfo(String.format("You have choosen %s", list.get(chose-1)));
        TopGenres = new HashMap<>();
        TopGenres.put(list.get(chose-1),1);
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

    private static List<List<String>> readCSVFile(String PATH) throws IOException {

        String line = null;
        BufferedReader stream = null;
        List<List<String>> csvData = new ArrayList<List<String>>();

        try {
            stream = new BufferedReader(new FileReader(PATH));
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

    private HashMap<Object,Object> MapsortByValuesInteger(HashMap<String, Integer> map,int HowMany) {
        HashMap<Object,Object> TopGenres = new HashMap<>();
        Object[] a = map.entrySet().toArray();
        Arrays.sort(a, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Map.Entry<String, Integer>) o2).getValue()
                        .compareTo(((Map.Entry<String, Integer>) o1).getValue());
            }
        });

        for (int i = 0 ; i<Math.min(a.length,HowMany);i++) {
            Object e = a[i];
            TopGenres.put(((Map.Entry<String, Integer>) e).getKey(),((Map.Entry<String, Integer>) e).getValue());
        }

        return TopGenres;
    }

    private HashMap<String,Double> MapsortByValuesDouble(HashMap<String, Double> map,int HowMany) {
        HashMap<String,Double> TopGenres = new HashMap<>();

        Object[] a = map.entrySet().toArray();
        Arrays.sort(a, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Map.Entry<String, Double>) o2).getValue()
                        .compareTo(((Map.Entry<String, Double>) o1).getValue());
            }
        });

        for (int i = 0 ; i<Math.min(a.length,HowMany);i++) {
            Object e = a[i];
            TopGenres.put((String)((Map.Entry<String, Double>) e).getKey(),(Double)((Map.Entry<String, Double>) e).getValue());
        }

        return TopGenres;
    }

    private Set<String> GetAllGenres() {
        Set<String> genres = new HashSet<>();
        for(List<String> movie : MoviesPool)
            for(String genre : movie.get(2).split("\\|"))
                if(!genre.contains("listed"))
                    genres.add(genre);

        return genres;


    }
}

