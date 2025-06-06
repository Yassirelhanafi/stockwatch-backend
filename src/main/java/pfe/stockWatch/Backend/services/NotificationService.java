package pfe.stockWatch.Backend.services;


import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pfe.stockWatch.Backend.dao.Notification;
import pfe.stockWatch.Backend.dao.Product;
import pfe.stockWatch.Backend.repositories.NotificationRepository;
import pfe.stockWatch.Backend.repositories.ProductRepository;

import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;


import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@AllArgsConstructor
public class NotificationService {

    ProductRepository productRepository;
    NotificationRepository notificationRepository;
    FcmTokenService fcmTokenService;





    public void addStock(Product product) {
        try {

            String productId = product.getId();
            int quantity = product.getQuantity();
            String name = product.getName();
            int minStockLevel = product.getMinStockLevel();
            int reorderQuantity = product.getReorderQuantity();

            Optional<Notification> existingNotification = Optional.ofNullable(notificationRepository.getNotificationById(productId));
                if (quantity <= minStockLevel) {

                    int newQuantity = quantity + reorderQuantity;
                    product.setQuantity(newQuantity);
                    product.setLastUpdated(Instant.now());

                    productRepository.updateProduct(product.getId(), product);
                    System.out.println("le produit is updated : " +product.getId()+product.getName()+product.getQuantity());

                    if (product.getQuantity() > minStockLevel) {
                        notificationRepository.deleteNotification(productId);
                    } else {
                        checkLowStock(product);
                    }
                } else {

                    if (existingNotification.isPresent()) {
                        notificationRepository.deleteNotification(productId);
                        System.out.println("Niveau de stock OK pour \"{}\" ({}). Suppression de la notification existante.");

                    }
                }


        } catch (Exception e) {
            throw new RuntimeException("echec d'ajout de stock", e);

        }
    }

    public List<Notification> getAllNotifications() throws ExecutionException, InterruptedException {
        return notificationRepository.getAllNotifications();
    }

    public void checkLowStock(Product product) {
        try {

            String productId = product.getId();
            int quantity = product.getQuantity();
            String name = product.getName();
            int minStockLevel = product.getMinStockLevel();

            Optional<Notification> existingNotification = Optional.ofNullable(notificationRepository.getNotificationById(productId));

            if (quantity <= minStockLevel) {


                    Notification notification = existingNotification.orElse(new Notification());

                    notification.setProductId(productId);
                    notification.setProductName(name);
                    notification.setQuantity(quantity);
                    notification.setTimestamp(Instant.now());

                    notificationRepository.saveNotification(notification);

                    List<String> tokens = fcmTokenService.getAllTokens();

                    for (String token : tokens) {
                        this.sendImmediateNotification(token,product.getName(),product.getQuantity());
                    }
                    System.out.println("le produit est en low : " +notification.getProductId()+notification.getProductName()+notification.getQuantity()+notification.getTimestamp());

            } else {
                if (existingNotification.isPresent()) {
                    notificationRepository.deleteNotification(productId);
                    System.out.println("Niveau de stock OK pour \"{}\" ({}). Suppression de la notification existante.");

                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la vérification du stock bas pour le produit {}: {}");
        }
    }


    @Scheduled(fixedRate = 30000)
    public void decrementQuantities() {
        try {
            List<Product> products = productRepository.getAllProducts();

            LocalDateTime now = LocalDateTime.now();
            int updatesMade = 0;

            for (Product product : products) {
                productRepository.decreaseQuantity(product);
                    CompletableFuture.runAsync(() -> {
                        try {
                            checkLowStock(product);
                            Thread.sleep(10000);
                            addStock(product);
                        } catch (Exception e) {
                            throw new RuntimeException("Erreur lors de la vérification/reconstitution du stock");
                        }
                    });
                }

            System.out.println("fin cycle" + updatesMade);
            } catch (ExecutionException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String deleteNotification(String productId) throws ExecutionException, InterruptedException {
        return notificationRepository.deleteNotification(productId);
    }

    private static final String EXPO_API_URL = "https://exp.host/--/api/v2/push/send";

    public void sendImmediateNotification(String token, String productName, int quantity) {
        RestTemplate restTemplate = new RestTemplate();

        // Crée le corps de la notification
        Map<String, Object> message = new HashMap<>();
        message.put("to", token);
        message.put("sound", "default");
        message.put("title", "Stock bas");
        message.put("body", productName + " est presque épuisé ! Quantité restante: " + quantity);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(message, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(EXPO_API_URL, request, String.class);
        System.out.println("✅ Notification envoyée : " + response.getBody());
    }


}



