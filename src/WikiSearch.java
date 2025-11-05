import java.awt.*; // класс для работы с desktop
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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;


public class WikiSearch {

    private static final String wikiApi_URL = "https://ru.wikipedia.org/w/api.php";
    private static final int timeout = 15;

    private static final Gson gson = new Gson();


    public static void main(String[] s){
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        //String search = "";
        while(true) {
            try {
                System.out.print("Для завершения работы программы ввелите 0 ");
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

                String jsonResponse = fetchSearch(search);
                if (jsonResponse != null){
                    parseAndResult(jsonResponse);
                    articleSelection(jsonResponse, scanner);
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
        scanner.close();
        System.out.println("The end of the prog");

    }

    private static String fetchSearch(String search) {
        try{ // кодируем запрос для дальнейшей передачи в url
            String searchURL = URLEncoder.encode(search, StandardCharsets.UTF_8);
            String url = wikiApi_URL +
                    "?action=query" +
                    "&list=search" +
                    "&utf8=1" +
                    "&format=json" +
                    "&srsearch=" + searchURL +
                    "&srlimit=10"; //ограничение по кол результатов 10
            // создаем http клиент с таймаутом

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(timeout))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(timeout))
                    .header("User-Agent", "WikiSearchApp/1.0 (Student Project)") //идентификация "себя"
                    .GET()
                    .build();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(url))
//                    .timeout(Duration.ofSeconds(timeout))
//                    .GET()
//                    .build();
            //отправка запроса , получение ответа
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

    private static void parseAndResult(String jsonResponse){
        try {
            // Парсим json в объект JsonObject для удобного доступа к полям
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

            if (!jsonObject.has("query")) {
                System.out.println("No query :(");
                return;
            }

            JsonObject query = jsonObject.getAsJsonObject("query");

            if (!query.has("search") || !query.get("search").isJsonArray()) {
                System.out.println("По вашему запросу ничего не найдено");
                return;
            }

            JsonArray searchResults = query.getAsJsonArray("search");

            if (searchResults.isEmpty()) {
                System.out.println("По вашему запросу ничего не найдено");
                return;
            }

            System.out.println("\nНайдено статей: " + searchResults.size());

            for (int i = 0; i < searchResults.size(); i++){
                JsonObject article = searchResults.get(i).getAsJsonObject();

                String title = article.has("title") ? article.get("title").getAsString() : "Без названия";
                String snippet = article.has("snippet") ? article.get("snippet").getAsString() : "";
                int pageId = article.has("pageid") ? article.get("pageid").getAsInt() : -1;

                // очищаем сниппет от HTML тегов для отображения
                String cleanSnippet = snippet.replaceAll("<[^>]+>", "");

                // обрезка текста
                if (cleanSnippet.length() > 120) {
                    cleanSnippet = cleanSnippet.substring(0, 120) + "...";
                }
                System.out.println((i + 1) + title);
                System.out.println((cleanSnippet.isEmpty() ? "Нет описания" : cleanSnippet));
                System.out.println("ID: " + pageId + "\n");
            }



        } catch (JsonSyntaxException e){
            System.out.println("Ошибка синтаксиса Json" + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("Ошибка структуры JSON: " + e.getMessage());
        } catch (NullPointerException e) {
            System.out.println("Ошибка: отсутствуют ожидаемые данные в JSON");
        } catch (RuntimeException e) {
            System.out.println("Ошибка при обработке результатов: " + e.getMessage());
        }
    }

    private static void articleSelection(String jsonResponse, Scanner scanner){
        try {
            // парсим JSON чтобы получить массив результатов
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
            JsonArray searchResults = jsonObject.getAsJsonObject("query").getAsJsonArray("search");

            //  список для хранения ID статей
            java.util.List<Integer> pageIds = new java.util.ArrayList<>();

            // собираем все pageid из результатов
            for (JsonElement element : searchResults) {
                JsonObject article = element.getAsJsonObject();
                if (article.has("pageid")) {
                    pageIds.add(article.get("pageid").getAsInt());
                }
            }

            if (pageIds.isEmpty()) {
                System.out.println("Не удалось получить ID статей");
                return;
            }

            System.out.println("choice smth(0 - skip)");
            String input = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(input);
                if (choice > 0 && choice <= pageIds.size()) {
                    int selectedPageId = pageIds.get(choice - 1);

                    // формируем URL для открытия статьи
                    String articleUrl = "https://ru.wikipedia.org/w/index.php?curid=" + selectedPageId;

                openInBrowser(articleUrl);
                } else if (choice != 0) {
                    System.out.println("Неверный номер статьи. Введите число от 1 до " + pageIds.size());
                }
            } catch (NumberFormatException e) {
                System.out.println("Пожалуйста, введите корректное число.");
            }
        }catch (JsonSyntaxException e) {
            System.out.println("Ошибка синтаксиса JSON при выборе статьи: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("Ошибка структуры JSON при выборе статьи: " + e.getMessage());
        } catch (NullPointerException e) {
            System.out.println("Ошибка: отсутствуют данные для выбора статьи");
        }
    }
}

class Browser{
    private static void openInBrowser(String url) {
        try {
             if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();

                // поддерживает ли действие поисковик
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    System.out.println("Открытие статьи.");
                    desktop.browse(new URI(url));
                    System.out.println("Статья открыта в браузере.");
                    return;
                }
            }

            // если автоматическое открытие не поддерживается -  показываем ссылку
            System.out.println("Не удалось открыть браузер.");
            System.out.println("Ссылка для открытия: " + url);

        } catch (URISyntaxException e) {
            System.out.println("Неверный формат URL: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Ошибка ввода/вывода при открытии браузера: " + e.getMessage());
        }
    }
}



