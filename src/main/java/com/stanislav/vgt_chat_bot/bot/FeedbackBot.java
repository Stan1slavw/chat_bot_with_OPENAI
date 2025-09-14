package com.stanislav.vgt_chat_bot.bot;

import com.stanislav.vgt_chat_bot.Service.FeedbackService;
import com.stanislav.vgt_chat_bot.Service.ProfileService;
import com.stanislav.vgt_chat_bot.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class FeedbackBot extends TelegramLongPollingBot {
    private final ProfileService profileService;
    private final FeedbackService feedbackService;

    @Value("${telegram.bot.username}")
    private String username;
    @Value("${telegram.bot.token}")
    private String token;

    private final Map<Long, String> state = new ConcurrentHashMap<>();
    private final Map<Long, String> tempBranch = new ConcurrentHashMap<>();
    private final Map<Long, Role> tempRole = new ConcurrentHashMap<>();

    @Override
    public String getBotUsername() { return username; }
    @Override
    public String getBotToken() { return token; }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage() == null || !update.getMessage().hasText()) return;
        var msg = update.getMessage();
        var chatId = msg.getChatId();
        var text = msg.getText().trim();
        var profile = profileService.findByTelegramId(chatId);

        if ("/start".equalsIgnoreCase(text)) {
            state.put(chatId, "ASK_ROLE");
            send(chatId, "Привіт! Обери свою посаду: Механік / Електрик / Менеджер");
            return;
        }

        if (profile == null && !state.containsKey(chatId)) {
            state.put(chatId, "ASK_ROLE");
        }

        switch (state.getOrDefault(chatId, "READY")) {
            case "ASK_ROLE" -> {
                var role = parseRole(text);
                if (role == null) { send(chatId, "Вибери: Механік / Електрик / Менеджер"); return; }
                tempRole.put(chatId, role);
                state.put(chatId, "ASK_BRANCH");
                send(chatId, "Вкажи назву філії (напр.: \"СТО Центральна\"):");
            }
            case "ASK_BRANCH" -> {
                tempBranch.put(chatId, text);
                profileService.ensureProfile(chatId, tempRole.remove(chatId), tempBranch.remove(chatId));
                state.put(chatId, "READY");
                send(chatId, "Дякую! Тепер просто надсилай відгук у будь-який момент.");
            }
            default -> {
                var result = feedbackService.processFeedback(chatId, text);
                send(chatId, "Збережено ✅\nТональність: " + result.sentiment() + "\nКритичність: " + result.criticality() + "/5");
                if (result.trelloCardId() != null) send(chatId, "Створена Trello-карта: " + result.trelloCardId());
            }
        }
    }

    private Role parseRole(String text) {
        var t = text.toLowerCase();
        if (t.contains("механ")) return Role.MECHANIC;
        if (t.contains("елект")) return Role.ELECTRICIAN;
        if (t.contains("менедж")) return Role.MANAGER;
        return null;
    }


    private void send(Long chatId, String text) {
        try { execute(SendMessage.builder().chatId(chatId).text(text).build()); } catch (Exception ignored) {}
    }
}

