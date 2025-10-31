# 使用 Spring AI 打造企業 RAG 知識庫【14】- 結構化資料轉換器

## Spring很注重一致性

![https://ithelp.ithome.com.tw/upload/images/20240814/20161290CYmKc1rZYz.png](https://ithelp.ithome.com.tw/upload/images/20240814/20161290CYmKc1rZYz.png)

先說個題外話，Spring AI 1.1 在結構化輸出方面有了重大改進！不僅保留了原有的 `StructuredOutputConverter`，還在 ChatClient 中新增了更簡潔的 `entity()` 方法，讓開發者有更多選擇。

## Converter 就是將 JSON 轉為 Java 的結構

對程式而言，要看懂 ChatGPT 回應的文字並不容易，所以 OpenAI 也提供了許多方式將回應轉為不同型態，JSON、XML 或是 Markdown 的語法，最近 OpenAI 還更新了新的功能，透過強制約束，讓 OpenAI 的輸出可以與定義的 JSON 格式完全一致，有了這功能後進行外部系統整合才能讓失誤率降到最低

Spring AI 1.1 在這方面有了顯著的改進，現在提供了兩種主要方式來處理結構化輸出：

1. **傳統的 StructuredOutputConverter** - 需要手動處理格式描述
2. **新的 ChatClient entity() 方法** - 自動處理類型轉換

![https://ithelp.ithome.com.tw/upload/images/20240814/201612906iIB5Aaeg3.jpg](https://ithelp.ithome.com.tw/upload/images/20240814/201612906iIB5Aaeg3.jpg)

## 三個實作類別的用途

Spring AI 目前提供的轉換器有三個 `BeanOutputConverter`、`MapOutputConverter` 和 `ListOutputConverter`（抽象類別就不看了）

![https://ithelp.ithome.com.tw/upload/images/20240814/20161290XuAnAxIQer.jpg](https://ithelp.ithome.com.tw/upload/images/20240814/20161290XuAnAxIQer.jpg)

`BeanOutputConverter`：AI 產生的格式化資料主要以 JSON 為主，這個轉換器就是將 JSON 轉為 Java 程式需要的 Bean，背後用到的就是 Spring MVC 最常用到的 ObjectMapper

`MapOutputConverter`：這個轉換器是將資料使用 Map 的方式轉出，對未知的格式最常使用的處理方式

`ListOutputConverter`：這個轉換器顧名思義就是將結果轉為 List，不過這裡主要以字串的 List 為主，例如請 AI 提供最受歡迎的五種冰淇淋口味，若是結構化的複數資料則還是使用 `BeanOutputConverter` 進行轉換，只是將 Bean 的類別改為 `ParameterizedTypeReference`

## 程式碼實作

### 方法一：使用 ChatClient entity() 方法（推薦）

Spring AI 1.1 新增的 `entity()` 方法是最簡潔的方式，它會自動處理類型轉換：

```java
@RestController
public class MovieController {
    
    private final ChatClient chatClient;
    
    public MovieController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
    
    // 單一演員的電影作品
    @GetMapping("/actor-films")
    public ActorsFilms getActorFilms(@RequestParam String actor) {
        return chatClient.prompt()
            .user("Generate the filmography of 5 movies for {actor}", 
                  Map.of("actor", actor))
            .call()
            .entity(ActorsFilms.class);
    }
    
    // 多位演員的電影作品
    @GetMapping("/multiple-actors")
    public List<ActorsFilms> getMultipleActors() {
        return chatClient.prompt()
            .user("Generate the filmography of 5 movies for Tom Hanks and Bill Murray.")
            .call()
            .entity(new ParameterizedTypeReference<List<ActorsFilms>>() {});
    }
}

record ActorsFilms(String actor, List<String> movies) {}
```

### 方法二：使用傳統的 StructuredOutputConverter

如果你需要更多控制權，仍可以使用傳統的轉換器：

#### Bean Output Converter

```java
@GetMapping("/actor-films-converter")
public ActorsFilms getActorFilmsWithConverter(@RequestParam String actor) {
    BeanOutputConverter<ActorsFilms> beanOutputConverter = 
        new BeanOutputConverter<>(ActorsFilms.class);
    String format = beanOutputConverter.getFormat();
    
    String response = chatClient.prompt()
        .user("Generate the filmography of 5 movies for {actor}. {format}", 
              Map.of("actor", actor, "format", format))
        .call()
        .content();
        
    return beanOutputConverter.convert(response);
}
```

#### Map Output Converter

```java
@GetMapping("/numbers-map")
public Map<String, Object> getNumbersAsMap() {
    MapOutputConverter mapOutputConverter = new MapOutputConverter();
    String format = mapOutputConverter.getFormat();
    
    String response = chatClient.prompt()
        .user("Provide me a List of an array of numbers from 1 to 9 under they key name 'numbers'. {format}", 
              Map.of("format", format))
        .call()
        .content();
        
    return mapOutputConverter.convert(response);
}
```

#### List Output Converter

```java
@GetMapping("/ice-cream-flavors")
public List<String> getIceCreamFlavors() {
    ListOutputConverter listOutputConverter = new ListOutputConverter(new DefaultConversionService());
    String format = listOutputConverter.getFormat();
    
    String response = chatClient.prompt()
        .user("List five ice cream flavors. {format}", 
              Map.of("format", format))
        .call()
        .content();
        
    return listOutputConverter.convert(response);
}
```

## Spring AI 1.1 的新特性與優勢

### ChatClient entity() 方法的優勢

1. **自動類型轉換**：無需手動處理格式描述和轉換邏輯
2. **類型安全**：編譯時就能確保類型正確性
3. **簡潔的 API**：減少樣板代碼
4. **更好的錯誤處理**：框架自動處理轉換錯誤

### 完整的控制器範例

```java
@RestController
@RequestMapping("/api/structured")
public class StructuredOutputController {
    
    private final ChatClient chatClient;
    
    public StructuredOutputController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
    
    // 使用新的 entity() 方法 - 推薦方式
    @GetMapping("/movie-info")
    public MovieInfo getMovieInfo(@RequestParam String movie) {
        return chatClient.prompt()
            .user("Provide detailed information about the movie: {movie}", 
                  Map.of("movie", movie))
            .call()
            .entity(MovieInfo.class);
    }
    
    // 處理列表類型
    @GetMapping("/top-movies")
    public List<MovieInfo> getTopMovies(@RequestParam String genre) {
        return chatClient.prompt()
            .user("List the top 5 {genre} movies with their details", 
                  Map.of("genre", genre))
            .call()
            .entity(new ParameterizedTypeReference<List<MovieInfo>>() {});
    }
    
    // 使用傳統轉換器的方式
    @GetMapping("/movie-info-traditional")
    public MovieInfo getMovieInfoTraditional(@RequestParam String movie) {
        BeanOutputConverter<MovieInfo> converter = new BeanOutputConverter<>(MovieInfo.class);
        String format = converter.getFormat();
        
        String response = chatClient.prompt()
            .user("Provide detailed information about the movie: {movie}. {format}", 
                  Map.of("movie", movie, "format", format))
            .call()
            .content();
            
        return converter.convert(response);
    }
}

record MovieInfo(
    String title, 
    String director, 
    int year, 
    List<String> genres, 
    double rating
) {}
```

## 與舊版本的比較

### 舊版本 (Spring AI 1.0)
```java
// 需要手動處理轉換器和格式
BeanOutputConverter<ActorsFilms> converter = new BeanOutputConverter<>(ActorsFilms.class);
String format = converter.getFormat();
Generation generation = chatModel.call(
    new Prompt(new PromptTemplate(template, 
        Map.of("actor", actor, "format", format)).createMessage())).getResult();
ActorsFilms result = converter.convert(generation.getOutput().getContent());
```

### 新版本 (Spring AI 1.1)
```java
// 一行代碼搞定
ActorsFilms result = chatClient.prompt()
    .user("Generate filmography for {actor}", Map.of("actor", actor))
    .call()
    .entity(ActorsFilms.class);
```

## 回顧

今天學到的內容:

1. Spring AI 1.1 新增的 ChatClient entity() 方法
2. 傳統 StructuredOutputConverter 的使用方式
3. 三種轉換器的適用場景
4. 新舊版本 API 的比較和優勢

### 最佳實踐建議

1. **優先使用 entity() 方法**：對於大多數結構化輸出需求，新的 entity() 方法更簡潔
2. **複雜場景使用轉換器**：需要自定義格式或特殊處理時，使用傳統轉換器
3. **類型安全**：盡量使用具體的類型而非 Map，提高代碼可維護性
4. **錯誤處理**：在生產環境中添加適當的異常處理

## Source Code

今日程式碼: [https://github.com/kevintsai1202/SpringBoot-AI-Day14.git](https://github.com/kevintsai1202/SpringBoot-AI-Day14.git)