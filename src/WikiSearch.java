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


class WikipediaAPI{

    private static final String wikiApi_URL = "https://ru.wikipedia.org/w/api.php";
    private static final int timeout = 15;

    private static final Gson gson = new Gson();


    public String getArticleTitle(JsonObject article) {
        return article.has("title") ? article.get("title").getAsString() : "Без названия";
    }

    public String getArticleSnippet(JsonObject article) {
        String snippet = article.has("snippet") ? article.get("snippet").getAsString() : "";
        String cleanSnippet = snippet.replaceAll("<[^>]+>", "");
        if (cleanSnippet.length() > 120) {
            cleanSnippet = cleanSnippet.substring(0, 120) + "...";
        }
        return cleanSnippet.isEmpty() ? "Нет описания" : cleanSnippet;
    }

    public int getArticlePageId(JsonObject article) {
        return article.has("pageid") ? article.get("pageid").getAsInt() : -1;
    }

    public String getArticleUrl(JsonObject article) {
        int pageId = getArticlePageId(article);
        return "https://ru.wikipedia.org/w/index.php?curid=" + pageId;
    }

    public String fetchSearch(String search) {
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


    public JsonArray getSearchResults(String jsonResponse){
        try {
            // Парсим json в объект JsonObject для удобного доступа к полям
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

            if (!jsonObject.has("query")) {
                System.out.println("No query :(");
                return new JsonArray();
            }

            JsonObject query = jsonObject.getAsJsonObject("query");

            if (!query.has("search") || !query.get("search").isJsonArray()) {
                System.out.println("По вашему запросу ничего не найдено");
                return new JsonArray();
            }

            JsonArray searchResults = query.getAsJsonArray("search");

            if (searchResults.isEmpty()) {
                System.out.println("По вашему запросу ничего не найдено");
                return new JsonArray();
            }

            return searchResults;



        } catch (JsonSyntaxException e){
            System.out.println("Ошибка синтаксиса Json" + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("Ошибка структуры JSON: " + e.getMessage());
        } catch (NullPointerException e) {
            System.out.println("Ошибка: отсутствуют ожидаемые данные в JSON");
        } catch (RuntimeException e) {
            System.out.println("Ошибка при обработке результатов: " + e.getMessage());
        }
        return null;
    }

//    public JsonArray searchResults(String jsonResponse){
//        try {
//            // парсим JSON чтобы получить массив результатов
//            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
//            JsonArray searchResults = jsonObject.getAsJsonObject("query").getAsJsonArray("search");
//
//            //  список для хранения ID статей
//            java.util.List<Integer> pageIds = new java.util.ArrayList<>();
//
//            // собираем все pageid из результатов
//            for (JsonElement element : searchResults) {
//                JsonObject article = element.getAsJsonObject();
//                if (article.has("pageid")) {
//                    pageIds.add(article.get("pageid").getAsInt());
//                }
//            }
//
//            if (pageIds.isEmpty()) {
//                System.out.println("Не удалось получить ID статей");
//                return;
//            }
//
//            System.out.println("Выберите статью для открытия (0 - выход): ");
//            String input = scanner.nextLine().trim();
//
//            try {
//                int choice = Integer.parseInt(input);
//                if (choice > 0 && choice <= pageIds.size()) {
//                    int selectedPageId = pageIds.get(choice - 1);
//
//                    // формируем URL для открытия статьи
//                    String articleUrl = "https://ru.wikipedia.org/w/index.php?curid=" + selectedPageId;
//
////                openInBrowser(articleUrl);
//                } else if (choice != 0) {
//                    System.out.println("Неверный номер статьи. Введите число от 1 до " + pageIds.size());
//                }
//            } catch (NumberFormatException e) {
//                System.out.println("Пожалуйста, введите корректное число.");
//            }
//        }catch (JsonSyntaxException e) {
//            System.out.println("Ошибка синтаксиса JSON при выборе статьи: " + e.getMessage());
//        } catch (IllegalStateException e) {
//            System.out.println("Ошибка структуры JSON при выборе статьи: " + e.getMessage());
//        } catch (NullPointerException e) {
//            System.out.println("Ошибка: отсутствуют данные для выбора статьи");
//        }
//    }
}



class Browser{
    public void openInBrowser(String url) {
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


public class WikiSearch {

    private final Browser browser;
    private final WikipediaAPI wikipediaAPI;


    public WikiSearch(){
        this.wikipediaAPI = new WikipediaAPI();
        this.browser = new Browser();
    }

    public static void main(String[] s){
        WikiSearch app = new WikiSearch();
        app.run();
    }

    public void run(){
//        WikiSearch app = new WikiSearch();
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

//                String jsonResponse = wikipediaAPI.fetchSearch(search);
//                if (jsonResponse != null){
//                    parseAndResult(jsonResponse);
//                    articleSelection(jsonResponse, scanner);
//                }

                String jsonResponse = wikipediaAPI.fetchSearch(search);
                if (jsonResponse != null) {
                    JsonArray searchResults = wikipediaAPI.getSearchResults(jsonResponse);
                    if (searchResults.size() > 0) {
                        displayResults(searchResults);
                        articleSelection(searchResults, scanner);
                    }
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

    private void displayResults(JsonArray searchResults) {
        System.out.println("\nНайдено статей: " + searchResults.size());

        for (int i = 0; i < searchResults.size(); i++) {
            JsonObject article = searchResults.get(i).getAsJsonObject();
            String title = wikipediaAPI.getArticleTitle(article);
            String snippet = wikipediaAPI.getArticleSnippet(article);
            int pageId = wikipediaAPI.getArticlePageId(article);

            System.out.println((i + 1) + ". " + title);
            System.out.println("   " + snippet);
            System.out.println("   ID: " + pageId + "\n");
        }
    }





    private void articleSelection(JsonArray searchResults, Scanner scanner){
        try {
//            // парсим JSON чтобы получить массив результатов
//            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
//            JsonArray searchResults = jsonObject.getAsJsonObject("query").getAsJsonArray("search");
//
//            //  список для хранения ID статей
//            java.util.List<Integer> pageIds = new java.util.ArrayList<>();
//
//            // собираем все pageid из результатов
//            for (JsonElement element : searchResults) {
//                JsonObject article = element.getAsJsonObject();
//                if (article.has("pageid")) {
//                    pageIds.add(article.get("pageid").getAsInt());
//                }
//            }
//
//            if (pageIds.isEmpty()) {
//                System.out.println("Не удалось получить ID статей");
//                return;
//            }

            System.out.println("Выберите статью для открытия (0 - выход): ");
            String input = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(input);
                if (choice > 0 && choice <= searchResults.size()) {
                    JsonObject selectedArticle = searchResults.get(choice - 1).getAsJsonObject();
                    String articleUrl = wikipediaAPI.getArticleUrl(selectedArticle);
                    browser.openInBrowser(articleUrl);
//                    int selectedPageId = searchResults.get(choice - 1);

//                    // формируем URL для открытия статьи
//                    String articleUrl = "https://ru.wikipedia.org/w/index.php?curid=" + selectedPageId;
//
////                openInBrowser(articleUrl);

                } else if (choice != 0) {
                    System.out.println("Неверный номер статьи. Введите число от 1 до " + searchResults.size());
                }
            } catch (NumberFormatException e) {
                System.out.println("Пожалуйста, введите корректное число.");
            }
        }catch (RuntimeException e) {
            System.out.println("\"Ошибка при выборе статьи: " + e.getMessage());
        }

    }
}


