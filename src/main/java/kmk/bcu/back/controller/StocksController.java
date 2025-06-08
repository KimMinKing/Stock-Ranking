package kmk.bcu.back.controller;

import kmk.bcu.back.service.KoreaInvestmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/stocks")
public class StocksController {

    private final KoreaInvestmentService service;

    public StocksController(KoreaInvestmentService service) {
        this.service = service;
    }

    //등락률 순위
    @GetMapping("/fluctuation")
    public Mono<String> get_Ranking() {
        return service.getfluctuation();
    }

    //시가총액 상위
    @GetMapping("/market_cap")
    public Mono<String> get_market_cap() {
        return service.getmarket_cap();
    }

    //수익자산지표 순위
    @GetMapping("/profit_asset_index")
    public Mono<String> get_profit_asset_index() {
        return service.getprofit_asset_index();
    }

    //체결강도 상위
    @GetMapping("/volume_power")
    public Mono<String> get_volume_power() {
        return service.getvolume_power();
    }

    //관심종목등록 상위
    @GetMapping("/top_interest_stock")
    public Mono<String> get_top_interest_stock() {
        return service.gettop_interest_stock();
    }
}

