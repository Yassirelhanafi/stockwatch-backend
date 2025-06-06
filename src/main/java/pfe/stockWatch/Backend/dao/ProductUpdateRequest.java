package pfe.stockWatch.Backend.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class ProductUpdateRequest {
    private String id;
    private int quantityToAdd;
}
