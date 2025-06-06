package pfe.stockWatch.Backend.dao;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.common.aliasing.qual.Unique;

import java.time.Instant;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class Product {

    @Unique
    private String id;

    private String name;
    private int quantity;
    private ConsumptionRate consumptionRate;
    private Integer minStockLevel;
    private Integer reorderQuantity;
    private Instant lastUpdated;
    private Instant lastDecremented;
    private Notification notification;







}