package pfe.stockWatch.Backend.repositories;


import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;
import pfe.stockWatch.Backend.dao.ConsumptionRate;
import pfe.stockWatch.Backend.dao.Product;
import pfe.stockWatch.Backend.dao.ProductUpdateRequest;

import com.google.cloud.Timestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Repository
public class ProductRepository {

    private static final String COLLECTION_NAME = "products";

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    public String saveProduct(Product product) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getFirestore().collection(COLLECTION_NAME).document(product.getId());
        ApiFuture<WriteResult> future = docRef.set(product);
        return future.get().getUpdateTime().toString();
    }

    public Product getProductById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getFirestore().collection(COLLECTION_NAME).document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.toObject(Product.class);
        } else {
            return null;
        }
    }

    public List<Product> getAllProducts() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getFirestore().collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Product> products = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Product product = doc.toObject(Product.class);
            products.add(product);
        }
        return products;
    }

    public String deleteProduct(String id) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> future = getFirestore().collection(COLLECTION_NAME).document(id).delete();
        return future.get().getUpdateTime().toString();
    }

    public String updateProduct(String id, Product updatedProduct) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getFirestore().collection(COLLECTION_NAME).document(id);
        ApiFuture<WriteResult> future = docRef.set(updatedProduct);
        return future.get().getUpdateTime().toString();
    }

    public String updateWithRequest(ProductUpdateRequest request) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(request.getId());

        // Récupérer le document
        DocumentSnapshot document = docRef.get().get();

        if (document.exists()) {
            Product existingProduct = document.toObject(Product.class);
            if (existingProduct != null) {
                int currentQty = existingProduct.getQuantity();
                existingProduct.setQuantity(currentQty + request.getQuantityToAdd());
                ApiFuture<WriteResult> writeFuture = docRef.set(existingProduct);
                return writeFuture.get().getUpdateTime().toString();
            }
        }

        return null;
    }

    public String decreaseQuantity(Product product) throws ExecutionException, InterruptedException {
        Firestore db = getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(product.getId());

        return db.runTransaction(transaction -> {


            Instant lastDecrementedTimestamp = product.getLastDecremented();
            LocalDateTime lastDecrementedDate = (lastDecrementedTimestamp != null)
                    ? lastDecrementedTimestamp.atZone(ZoneId.systemDefault()).toLocalDateTime()
                    : LocalDateTime.of(1970, 1, 1, 0, 0);

            LocalDateTime now = LocalDateTime.now();
            long diffMillis = ChronoUnit.MILLIS.between(lastDecrementedDate, now);

            if (diffMillis <= 0 || product.getConsumptionRate() == null) {
                return "No update needed";
            }

            double diffHours = diffMillis / (1000.0 * 60 * 60); // convert to hours
            int periodsPassed = calculatePeriodsPassed(diffHours, product.getConsumptionRate());

            if (periodsPassed > 0) {
                int quantityToDecrement = periodsPassed * product.getConsumptionRate().getAmount();
                int newQuantity = Math.max(0, product.getQuantity() - quantityToDecrement);

                if (newQuantity < product.getQuantity()) {
                    product.setQuantity(newQuantity);
                    Instant nowTimestamp = Instant.now();
                    product.setLastDecremented(nowTimestamp);
                    product.setLastUpdated(nowTimestamp);

                    transaction.set(docRef, product);
                    return "Product updated. New quantity: " + newQuantity;
                }
            }

            return "No quantity change";
        }).get();
    }

    private int calculatePeriodsPassed(double diffHours, ConsumptionRate rate) {
        double periodInHours;

        switch (rate.getUnit()) {
            case "hour":
                periodInHours = rate.getPeriod();
                break;
            case "day":
                periodInHours = rate.getPeriod() * 24;
                break;
            case "week":
                periodInHours = rate.getPeriod() * 24 * 7;
                break;
            case "month":
                periodInHours = rate.getPeriod() * 24 * 30;
                break;
            default:
                throw new IllegalArgumentException("Unknown time unit: " + rate.getUnit());
        }

        return (int) (diffHours / periodInHours);
    }


    public boolean checkLowStockForProduct(String productId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getFirestore().collection(COLLECTION_NAME).document(productId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            Product product = document.toObject(Product.class);
            if (product.getMinStockLevel() != null) {
                return product.getQuantity() < product.getMinStockLevel();
            }
        }
        return false;
    }

    public String addStock(String productId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = getFirestore().collection(COLLECTION_NAME).document(productId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            Product product = document.toObject(Product.class);
            int newQuantity = product.getQuantity() +product.getReorderQuantity();

            Instant now = Instant.now();

            product.setQuantity(newQuantity);
            product.setLastUpdated(now);

            ApiFuture<WriteResult> updateFuture = docRef.set(product);

            return updateFuture.get().getUpdateTime().toString();
        }
        return null;
    }




}