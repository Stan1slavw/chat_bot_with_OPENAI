package com.stanislav.vgt_chat_bot.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.Map;


@Service
public class TrelloService {
    @Value("${trello.enabled:false}")
    boolean enabled;
    @Value("${trello.key:}")
    String key;
    @Value("${trello.token:}")
    String token;
    @Value("${trello.listId:}")
    String listId;
    private final WebClient web = WebClient.create("https://api.trello.com/1");

    public String createCard(String name, String desc) {
        if (!enabled) return null;
        try {
            Map resp = web.post().uri(uri -> uri.path("/cards")
                            .queryParam("idList", listId)
                            .queryParam("name", name)
                            .queryParam("desc", desc)
                            .queryParam("key", key)
                            .queryParam("token", token)
                            .build())
                    .retrieve().bodyToMono(Map.class).block();
            return resp == null ? null : (String) resp.get("id");
        } catch (Exception e) {
            return null;
        }
    }
}

