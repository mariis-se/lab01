import java.io.IOException;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.Scanner;
import java.io.IOError;
import java.util.IllegalFormatException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;


public class WikiSearch {

    private static final String wikiApi_URL = "https://ru.wikipedia.org/w/api.php";
    private static final int timeout = 15;


    public static void main(String[] s){
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        //String search = "";
        while(true) {
            try {
                System.out.print("Введите поисковой запрос: ");
                String search = scanner.nextLine();

                if (search.equalsIgnoreCase("0")){
                    break;
                }

                //trim - убираем пробелы
                if (search.trim().isEmpty()) {
                    System.out.println("Ошибка. Пустой запрос.");
                    continue;
                }

                System.out.println("Searching.......");

                String jsonAnswer = fetchSearch(search);
//                if (jsonAnswer != null){
//
//                }


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
        scanner.close();
        System.out.println("The end of the prog");

    }

    private static String fetchSearch(String search) {
        try{ // кодируем запрос для дальнейшей передачи в url
            String searchURL = URLEncoder.encode(search, StandardCharsets.UTF_8);
            String url = wikiApi_URL +  "?action=query&list=search&utf8=&format=json&srsearch=" + searchURL + "&srlimit=10";
            // создаем http клиент с таймаутом

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(timeout))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(timeout))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.out.println("Ошибка HTTP: " + response.statusCode());
            }

        } catch (UnsupportedEncodingException e){
            System.out.println("Ошибка кодирования запроса " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Ошибка сети " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Запрос прерван " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Неверный URL" + e.getMessage());
        }
        return null;

    }




}