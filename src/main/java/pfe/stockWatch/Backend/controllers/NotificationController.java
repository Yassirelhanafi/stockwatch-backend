package pfe.stockWatch.Backend.controllers;


import com.google.api.client.auth.oauth2.TokenRequest;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pfe.stockWatch.Backend.dao.FcmToken;
import pfe.stockWatch.Backend.dao.Notification;
import pfe.stockWatch.Backend.services.FcmTokenService;
import pfe.stockWatch.Backend.services.NotificationService;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/notifications")
@AllArgsConstructor
public class NotificationController {

    private NotificationService notificationService;
    private final FcmTokenService tokenService;


    @PostMapping("/save-token")
    public ResponseEntity<?> saveToken(@RequestBody FcmToken tokenRequest) {
        try {
            tokenService.saveToken(tokenRequest.getToken());
            return ResponseEntity.ok("Token enregistré");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur lors de l'enregistrement");
        }
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Notification>> getAllNotifications() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }


}
