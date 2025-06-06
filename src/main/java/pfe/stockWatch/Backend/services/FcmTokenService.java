package pfe.stockWatch.Backend.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;
import pfe.stockWatch.Backend.dao.FcmToken;

import java.util.ArrayList;
import java.util.List;

@Service
public class FcmTokenService {

    private static final String COLLECTION_NAME = "fcmTokens";

    public void saveToken(String token) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference tokens = db.collection(COLLECTION_NAME);

        // Vérifie si le token existe déjà
        DocumentSnapshot doc = tokens.document(token).get().get();
        if (!doc.exists()) {
            // Stocke le token en document avec l'ID = token
            ApiFuture<WriteResult> future = tokens.document(token).set(new FcmToken(token));
            future.get();  // attendre la fin de l'opération
        }
    }

    public List<String> getAllTokens() throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference tokens = db.collection(COLLECTION_NAME);

        ApiFuture<QuerySnapshot> future = tokens.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<String> tokenList = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            FcmToken fcmToken = doc.toObject(FcmToken.class);
            tokenList.add(fcmToken.getToken());
        }

        return tokenList;
    }


}
