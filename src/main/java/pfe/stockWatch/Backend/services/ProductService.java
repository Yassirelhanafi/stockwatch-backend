package pfe.stockWatch.Backend.services;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pfe.stockWatch.Backend.dao.Product;
import pfe.stockWatch.Backend.dao.ProductUpdateRequest;
import pfe.stockWatch.Backend.repositories.ProductRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@AllArgsConstructor
public class ProductService {

    private ProductRepository productRepository;

    private NotificationService notificationService;





    public List<Product> getAllProducts() throws ExecutionException, InterruptedException {
        return productRepository.getAllProducts();
    }

    public Product getProductById(String id) throws ExecutionException, InterruptedException {
        return productRepository.getProductById(id);
    }

    public String addProduct(Product product) throws ExecutionException, InterruptedException {


        String msg = productRepository.saveProduct(product);
        CompletableFuture.runAsync(() -> {
            try {
                notificationService.checkLowStock(product);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la vérification du stock bas");
            }
        });
        return msg;
    }

    public String updateProduct(String id,Product product) throws ExecutionException, InterruptedException {

        String msg = productRepository.updateProduct(id, product);
        CompletableFuture.runAsync(() -> {
            try {
                notificationService.checkLowStock(product);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la vérification du stock bas");
            }
        });

        return msg;
    }

    public String deleteProduct(String id) throws ExecutionException, InterruptedException {
        notificationService.deleteNotification(id);
        return productRepository.deleteProduct(id);
    }

    public String updateProductStock(ProductUpdateRequest request) throws ExecutionException, InterruptedException {

        String msg = productRepository.updateWithRequest(request);

        try {
            Product product = getProductById(request.getId());
            notificationService.checkLowStock(product);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la vérification du stock bas");
        }

        return msg;
    }



}
