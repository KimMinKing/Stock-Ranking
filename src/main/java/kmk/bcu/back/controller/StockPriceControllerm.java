package kmk.bcu.back.controller;

import kmk.bcu.back.service.StockPriceServicem;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/stockprice/m")
public class StockPriceControllerm {

    private final StockPriceServicem stockPriceServicem;

    public StockPriceControllerm(StockPriceServicem stockPriceServicem) {
        this.stockPriceServicem = stockPriceServicem;
    }

    @GetMapping("/{stockCode}")
    public Mono<String> getStockPrice(@PathVariable String stockCode,
                                      @RequestParam(defaultValue = "J") String marketDivCode,
                                      @RequestParam(defaultValue = "P") String custType) {
        return stockPriceServicem.getStockPrice(marketDivCode, stockCode, custType);
    }
}
