import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

    }

    private static void config() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("config.yml"));
            ArrayList<String> strings=new ArrayList<>();
            for (String line = bufferedReader.readLine(); line != null;) {
                strings.add(line);
                line = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
