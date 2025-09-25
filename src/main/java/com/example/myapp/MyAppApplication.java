@SpringBootApplication
public class MyAppApplication extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(MyAppApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(MyAppApplication.class, args);
    }

    @RestController
    public static class HelloController {
        @GetMapping("/")
        public String hello() {
            return "Hello from CI/CD Pipeline on Tomcat!";
        }
    }
}
