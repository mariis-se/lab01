import java.util.Scanner;
import java.io.IOError;
import java.util.IllegalFormatException;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;


public class WikiSearch {

    private static final String wikiApi_URL = "https://ru.wikipedia.org/w/api.php";
    private static final int timeout = 15;


    public static void main(String[] s){
        Scanner scanner = new Scanner(System.in);
        String search = "";

        try {
            System.out.print("Введите поисковой запрос: ");
            search = scanner.nextLine();

            //trim - убираем пробелы
            if (search.trim().isEmpty()) {
                System.out.println("Ошибка. Пустой запрос.");
                return;
            }

            System.out.println("Считано: " + search);
        } catch (IOError e) {
            System.out.println("Ошибка ввода(вывода)!");
        } catch (IllegalFormatException e) {
            System.out.println("Ошибка формата ввода!");
        }
//        finally {
//            if (scanner != null) {
//                scanner.close();
//            }
//        }

    }





}