package com.stanislav.vgt_chat_bot.Service;

import com.stanislav.vgt_chat_bot.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stanislav.vgt_chat_bot.domain.*;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final ProfileService profileService;
    private final FeedbackRepository repo;
    private final OpenAiAnalysisService ai;
    private final GoogleDocsService docs;
    private final TrelloService trello;


    @Value("${trello.createThreshold:4}")
    int threshold;

    @Transactional
    public Result processFeedback(long telegramUserId, String text) {
        EmployeeProfile profile = profileService.getRequired(telegramUserId);

        var analysis = ai.analyze(text);

        var fb = new Feedback();
        fb.setProfile(profile);
        fb.setText(text);
        fb.setSentiment(analysis.sentiment());
        fb.setCriticality(analysis.criticality());
        fb.setResolution(analysis.resolution());

        String trelloId = null;
        if (analysis.criticality() >= threshold) {
            String title = "Критичний фідбек (" + profile.getBranch().getName() + ", " + profile.getRole() + ")";
            String desc = text + "\n\nТональність: " + analysis.sentiment()
                    + " | Критичність: " + analysis.criticality()
                    + "\nПорада: " + analysis.resolution();
            trelloId = trello.createCard(title, desc);
            fb.setTrelloCardId(trelloId);
        }

        repo.save(fb);
        docs.appendFeedbackRow(fb);

        return new Result(analysis.sentiment(), analysis.criticality(), trelloId);
    }

    public record Result(String sentiment, short criticality, String trelloCardId) {}
}

