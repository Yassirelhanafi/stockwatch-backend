package pfe.stockWatch.Backend.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pfe.stockWatch.Backend.dao.Product;
import pfe.stockWatch.Backend.dao.ProductUpdateRequest;
import pfe.stockWatch.Backend.services.ProductService;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductController {

    private ProductService productService;

    @PostMapping()
    public ResponseEntity<String> addProduct(@RequestBody Product product) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(productService.addProduct(product));
    }

    @GetMapping()
    public ResponseEntity<List<Product>> getAllProducts() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable String id) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }

    @PatchMapping("/quantity")
    public ResponseEntity<String> updateProductQuantity(@RequestBody ProductUpdateRequest request) throws ExecutionException, InterruptedException {
        System.out.println("=== DEBUG REQUEST ===");
        System.out.println("Request object: " + request);
        System.out.println("Request ID: '" + request.getId() + "'");
        System.out.println("Request quantityToAdd: " + request.getQuantityToAdd());
        System.out.println("ID is null: " + (request.getId() == null));
        System.out.println("ID is empty: " + (request.getId() != null && request.getId().isEmpty()));
        System.out.println("====================");
        return ResponseEntity.ok(productService.updateProductStock(request));
    }

}
