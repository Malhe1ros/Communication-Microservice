package ufrn.br.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class ProblemService {
    private final ExecutorService executorService = Executors.newFixedThreadPool(10000);
    private final RateLimiter rateLimiter = RateLimiter.create(0.5);

    CompletableFuture<String> solve(String url){
        return  CompletableFuture.supplyAsync(() -> {
                rateLimiter.acquire();

                RestTemplate restTemplate = new RestTemplate();
                String requestURL = url;

                String json = restTemplate.getForObject(requestURL, String.class);
                return json;
            },executorService);
    }

    @GetMapping("/getAllProblems")
    public CompletableFuture<String> getProblems() throws IOException, InterruptedException {
        System.out.println("CHAMOU");
        return solve("https://codeforces.com/api/problemset.problems");
    }

    @GetMapping("/checkTrue/{handle}&{problema}")
    public boolean checkTrue(@PathVariable(value="handle") String handle,@PathVariable(value="problema") String problema) throws ExecutionException, InterruptedException {
        String requestURL = "https://codeforces.com/api/user.status?handle="+handle+"&from=1&count=1";
        String x = solve(requestURL).get();
        String number = problema.replaceAll("\\D+", "");

        String characters = problema.replaceAll("\\d+", "");
        System.out.println(x);
        return x.contains("OK") && x.contains(number) && x.contains(characters);
    }

}
