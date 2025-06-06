package pfe.stockWatch.Backend.dao;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ConsumptionRate {
    private int amount;
    private int period;
    private String unit; // "hour", "day", "week", "month"


}