package Company;

import javafx.scene.control.Alert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class RecoEngine {
    public static final String ANSI_RED = "\033[1;31m";;
    public static final String ANSI_RESET = "\u001B[0m";
    public static String MOVIES_PATH;
    public static String RATING_PATH;
    public static String StringOfUser;
    public static Scanner s = new Scanner(System.in);
    List<List<String>> UserMovies,RecommendMovies,TempForLaterCheck;
    HashMap<String,List<String>> MoviesPool,Ratings;
    Set<String> genres;
    HashMap<Object, Object> TopGenres;
    List<List<String>> optinal;


    public RecoEngine() throws Exception {
        InitPaths();
        MoviesPool = readCSVFile(MOVIES_PATH,"Movies");
        Ratings = readCSVFile(RATING_PATH,"Ratings");
        UserMovies = new ArrayList<List<String>>();
        RecommendMovies = new ArrayList<List<String>>();
        TempForLaterCheck = new ArrayList<List<String>>();
        genres = GetAllGenres();

    }

    public void InitPaths() {
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
        re.InitUserMovies(chose, "");
        re.RecommendMoviesForUser(chose);
    }

        public List RecommendMoviesForUser(int chose) {
        List finalMovies = new ArrayList();
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
            finalMovies = PrintHashMapWithIndexes(IDtoTitle);
            return finalMovies;
        }

        public List PrintHashMapWithIndexes(HashMap<String, Double> iDtoTitle) {
            PrintInfo("We are recommending you the following 10 movies -");
            List arrayList = new ArrayList();
            for (int i = 1 ; i<= iDtoTitle.entrySet().size() ; i++) {
                Object movie = iDtoTitle.keySet().toArray()[i-1];
                System.out.println(String.format("%s. %s",i,movie));
                arrayList.add(String.format("%s. %s",i,movie));
            }
            return arrayList;

            }

        public HashMap<String,Double> IDtoTitle(HashMap<String, Double> topRank) {
            HashMap<String,Double> BestMovies = new HashMap<>();

            for (Map.Entry<String, Double> IDMovie : topRank.entrySet()) {
                for(List<String> movie : TempForLaterCheck)
                    if(movie.get(0).equals(IDMovie.getKey()))
                        BestMovies.put(movie.get(1),IDMovie.getValue());
            }
            return BestMovies;
        }

        public HashMap<String,Double> AddRankToMovies(HashMap<String, Integer> moviesSharedGenres) {

            HashMap<String,Double> Ranks = new HashMap<>();
            HashMap<Object,Object> TopmoviesSharedGenres = MapsortByValuesInteger(moviesSharedGenres,200);
            int count = 0;
            for (Map.Entry<Object, Object> SharedMovie : TopmoviesSharedGenres.entrySet())
              for(Map.Entry<String, List<String>> movie : Ratings.entrySet())
                {
                        if (movie.getKey().equals(SharedMovie.getKey()))
                            Ranks.put((String) SharedMovie.getKey(), Double.parseDouble(movie.getValue().get(0)));

                        if (count<3)
                            if (MoviesPool.get(movie.getKey()).get(1).contains(StringOfUser))
                                if (!InList(MoviesPool.get(movie.getKey()).get(1))) {
                                    Ranks.put((String) movie.getKey(), Double.parseDouble("5"));
                                    count++;
                                }
                 }



            return Ranks;
        }

        public HashMap<String,Integer> GetSharedGenresMovies(HashMap<Object, Object> topGenres) {
            HashMap<String,Integer> SharedGenresMovies = new HashMap<>();
            List <String>ListOfTopGenre = new ArrayList(topGenres.keySet());

            for (Map.Entry<String, List<String>> movie : MoviesPool.entrySet())
            {
                String[] SpecificMovieGenres = movie.getValue().get(2).split("\\|");
                int rankOfMovie = GetRank(SpecificMovieGenres,ListOfTopGenre);
                if(rankOfMovie>0) {
                    SharedGenresMovies.put(movie.getValue().get(0), rankOfMovie);
                    TempForLaterCheck.add(movie.getValue());
                }
            }

            return SharedGenresMovies;
        }

        public boolean InList(String toCheck)
        {
            boolean flag = false;

            for ( int i = 0 ; i<UserMovies.size();i++)
                if ((toCheck.equals(UserMovies.get(i).get(1))))
                    flag = true;

            return flag;

        }
        public int GetRank(String[] SpecificMovieGenres, List<String> ListOfTopGenre) {
            int count = 0;

            for(String genre : SpecificMovieGenres)
                if(ListOfTopGenre.contains(genre))
                    count++;

            return count;

        }

        public HashMap<String,Integer> CountGenres(List<List<String>> userMovies) {

            HashMap<String,Integer> Count = new HashMap<>();

            for(List<String> movie : userMovies){
                String[] genres = movie.get(2).split("\\|");
                Count = UpdateMap(Count,genres);
            }

            return Count;
        }

        public HashMap<String, Integer> UpdateMap(HashMap<String, Integer> count, String[] genres) {

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

        public void InitUserMovies(int chose, String movieString) {
            boolean KeepAsking = true;
            StringOfUser = movieString;


            if(chose == 1) {
                while (KeepAsking) {
                    List<List<String>> OptionalMovies = GetOptionalMovies();
                    optinal = OptionalMovies;
                    //this.UpdateUserMovies(OptionalMovies);
                    KeepAsking = false;
                    //KeepAsking = CheckIfWantMore();
                }
            }
            else{
                PrintSetWithIndexes(genres);
            }
        }

        public String EnterMovie() {
            PrintInfo("Please Enter movie's title");
            String MovieToCheck = s.nextLine();
            while(MovieToCheck.length()==0)
                MovieToCheck = s.nextLine();
            PrintInfo("You have entered " + MovieToCheck);
            return MovieToCheck;
        }

        public boolean CheckIfWantMore() {
            PrintInfo("Do you want to Continue ?");
            PrintInfo("Enter 1 for yes , 0 otherwise");
            int chose;

            chose = s.nextInt();
            if (chose == 1)
                return true;
            else
                return false;

        }

        public void UpdateUserMovies(List<List<String>> optionalMovies) {
            //PrintInfo(String.format("There are %s Movies - ",optionalMovies.size()));
            //PrintListWithIndexes(optionalMovies);
            //rintInfo("Please enter the movie's index you want");
            String check = mainController.movieString2;

                int chose = Integer.parseInt(mainController.movieString2);
                //PrintInfo(String.format("You have choosen the movie - %s ", optionalMovies.get(chose-1).get(1)));
                if (chose > optinal.size()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Alert!");
                    alert.setHeaderText("בחרת מספר שלא מופיע / הזנת ערך לא תקין");
                    alert.showAndWait();
                } else
                    this.UserMovies.add(optionalMovies.get(chose - 1));
            }

            //PrintInfo("You have entered so far - ");
            //PrintListWithIndexes(UserMovies);



        public void PrintListWithIndexes(List<List<String>> optionalMovies) {
            for(int i = 1 ; i <= optionalMovies.size(); i++)
                System.out.println(String.format("%s. %s",i,optionalMovies.get(i-1).get(1)));
        }

        public void PrintSetWithIndexes(Set<String> genres) {
            List<String> list = new ArrayList<>(genres);
            /*
            for(int i = 1 ; i <= genres.size(); i++)
                System.out.println(String.format("%s. %s",i,list.get(i-1)));
            */
            int chose = Integer.parseInt(mainController.genreString);
            while(chose<1 || chose>list.size());
            PrintInfo(String.format("You have choosen %s", list.get(chose-1)));
            TopGenres = new HashMap<>();
            TopGenres.put(list.get(chose-1),1);
        }

        public List<List<String>> GetOptionalMovies() {
            List<List<String>> OptinalMovies = new ArrayList<>();
            boolean HasMovies = false;

            while(!HasMovies) {
                String MovieToCheck = mainController.movieString;
                for (Map.Entry<String, List<String>> Movie : MoviesPool.entrySet())
                    if (Movie.getValue().get(1).toLowerCase().contains(MovieToCheck.toLowerCase())) {
                        OptinalMovies.add(Movie.getValue());
                        HasMovies = true;
                    }
                if(!HasMovies)
                    return null;
            }

            return OptinalMovies;
        }

        public static void PrintInfo(String text){

            System.out.println(ANSI_RED + text + ANSI_RESET);
        }

    public static HashMap<String,List<String>> readCSVFile(String PATH,String What) throws IOException {
        String line = null;
        BufferedReader stream = null;
        HashMap<String,List<String>> csvData = new HashMap<>();
        List<String> OldList;

        try {
            stream = new BufferedReader(new FileReader(PATH));
            while ((line = stream.readLine()) != null) {
                String[] splitted = line.split(",");
                List<String> dataLine = new ArrayList<String>(splitted.length);
                for (String data : splitted) {
                    dataLine.add(data);
                }
                if(What.equals("Movies"))
                    csvData.put(dataLine.get(0),dataLine);
                else {
                    if(csvData.containsKey(dataLine.get(1))) {
                        OldList = csvData.get(dataLine.get(1));
                        Double sum = Double.parseDouble(OldList.get(0)) * Double.parseDouble(OldList.get(1));
                        Double avg = (sum + Double.parseDouble(dataLine.get(2))) / (Double.parseDouble(OldList.get(1)) + 1);
                        String avgString = Double.toString(avg);
                        String newCount = Double.toString(Double.parseDouble(OldList.get(1)) + 1);
                        csvData.put(dataLine.get(1), new ArrayList<>(Arrays.asList(avgString, newCount)));
                    }
                    else
                        csvData.put(dataLine.get(1),new ArrayList<>(Arrays.asList(dataLine.get(2),"1")));
                 }
            }
        } finally {
            if (stream != null)
                stream.close();
        }
        return csvData;

    }

    public HashMap<Object,Object> MapsortByValuesInteger(HashMap<String, Integer> map,int HowMany) {
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

    public HashMap<String,Double> MapsortByValuesDouble(HashMap<String, Double> map,int HowMany) {
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

    public Set<String> GetAllGenres() {
        Set<String> genres = new HashSet<>();
        for(Map.Entry<String, List<String>> movie : MoviesPool.entrySet())
            for(String genre : movie.getValue().get(2).split("\\|"))
                if(!genre.contains("listed"))
                    genres.add(genre);

        return genres;


    }


}

