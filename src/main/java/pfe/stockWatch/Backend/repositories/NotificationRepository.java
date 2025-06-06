package pfe.stockWatch.Backend.repositories;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;
import pfe.stockWatch.Backend.dao.Notification;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class NotificationRepository {

    private static final String COLLECTION_NAME = "notifications";

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    public String saveNotification(Notification notification) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getFirestore().collection(COLLECTION_NAME).document(notification.getProductId());
        ApiFuture<WriteResult> result = docRef.set(notification);
        return result.get().getUpdateTime().toString();
    }

    public Notification getNotificationById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getFirestore().collection(COLLECTION_NAME).document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.toObject(Notification.class);
        }
        return null;
    }





    public List<Notification> getAllNotifications() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getFirestore().collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Notification> notifications = new ArrayList<>();
        for (DocumentSnapshot doc : documents) {
            notifications.add(doc.toObject(Notification.class));
        }
        return notifications;
    }

    public String updateNotification(Notification notification) throws ExecutionException, InterruptedException {
        return saveNotification(notification);
    }

    public String deleteNotification(String id) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> writeResult = getFirestore().collection(COLLECTION_NAME).document(id).delete();
        return writeResult.get().getUpdateTime().toString();
    }



}
