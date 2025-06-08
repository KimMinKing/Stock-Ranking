package kmk.bcu.back.controller;

import kmk.bcu.back.service.StockPriceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/stockprice/d")
public class StockPriceController {


    private final StockPriceService kiService;

    public StockPriceController(StockPriceService kiService) {
        this.kiService = kiService;
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> getDailyPrice(@PathVariable("code") String stockCode) {
        try {
            String result = kiService.getDailyPrice(stockCode);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
