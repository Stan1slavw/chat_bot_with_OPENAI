package com.stanislav.vgt_chat_bot.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.stanislav.vgt_chat_bot.domain.Feedback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class GoogleDocsService {

    @Value("${google.docs.documentId:}")
    String documentId;

    @Value("${google.credentials.path:credentials.json}")
    String credentialsPath;

    private Docs docsService() throws Exception {
        File credsFile = new File(credentialsPath);
        System.out.println("[Docs] creds path: " + credsFile.getAbsolutePath() + ", exists=" + credsFile.exists());
        if (!credsFile.exists()) {
            throw new IllegalStateException("credentials.json not found at: " + credsFile.getAbsolutePath());
        }

        try (FileInputStream fis = new FileInputStream(credsFile)) {
            byte[] preview = fis.readNBytes(256);
            String head = new String(preview, StandardCharsets.UTF_8);
            if (!head.contains("\"type\"")) {
                throw new IllegalStateException("Invalid credentials.json: no \"type\" field");
            }
        }
        GoogleCredentials creds = GoogleCredentials
                .fromStream(new FileInputStream(credsFile))
                .createScoped(List.of("https://www.googleapis.com/auth/documents"));

        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Docs.Builder(httpTransport, JacksonFactory.getDefaultInstance(), new HttpCredentialsAdapter(creds))
                .setApplicationName("vgr-feedback-bot")
                .build();
    }

    public void appendFeedbackRow(Feedback fb) {
        if (documentId == null || documentId.isBlank()) {
            System.err.println("[Docs] GOOGLE_DOC_ID is empty — skipping");
            return;
        }
        System.out.println("[Docs] DOC_ID prefix: " + documentId.substring(0, Math.min(documentId.length(), 6)));

        String line = String.format(
                "[%s] Філія: %s | Роль: %s%nТекст: %s%nТональність: %s | Критичність: %d/5%nРішення: %s%n%n",
                DateTimeFormatter.ISO_INSTANT.format(fb.getCreatedAt()),
                fb.getProfile().getBranch().getName(),
                fb.getProfile().getRole(),
                fb.getText(),
                fb.getSentiment(),
                fb.getCriticality(),
                fb.getResolution() == null ? "-" : fb.getResolution()
        );

        try {
            Request insert = new Request().setInsertText(new InsertTextRequest()
                    .setEndOfSegmentLocation(new EndOfSegmentLocation())
                    .setText(line));

            BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest().setRequests(List.of(insert));
            BatchUpdateDocumentResponse resp = docsService().documents().batchUpdate(documentId, body).execute();
            System.out.println("[Docs] Appended OK. Replies: " + (resp.getReplies() == null ? 0 : resp.getReplies().size()));

        } catch (GoogleJsonResponseException e) {
            int code = e.getStatusCode();
            String msg = e.getDetails() == null ? e.getMessage() : e.getDetails().getMessage();
            System.err.println("[Docs] GJRE " + code + ": " + msg);

            if (code == 403 && msg != null && msg.contains("Google Docs API has not been used")) {
                System.err.println("[Docs] Enable Docs API for your GCP project (SERVICE_DISABLED).");
            } else if (code == 403 && msg != null && msg.contains("Permission")) {
                System.err.println("[Docs] Share the document with the service account (client_email) as Editor.");
            } else if (code == 400 && msg != null && msg.contains("Index") && msg.contains("must be less")) {
                System.err.println("[Docs] Invalid insert index. Use EndOfSegmentLocation (already applied).");
            }
            e.printStackTrace();

        } catch (Exception e) {
            System.err.println("[Docs] Failed to append: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
