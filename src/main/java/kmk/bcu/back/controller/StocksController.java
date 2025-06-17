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

    //우선주 괴리율 상위
    @GetMapping("/prefer_disparate_ratio")
    public Mono<String> get_prefer_disparate_ratio(){ return service.getprefer_disparate_ratio();}

    //이격도 순위
    @GetMapping("/disparity")
    public Mono<String> get_disparity(){ return service.getdisparity();}

    //시장가치 순위
    @GetMapping("/market_value")
    public Mono<String> get_market_value(){ return service.getmarket_value();}

    //하락 상위?
    @GetMapping("/exp_trans_updown")
    public Mono<String> get_exp_trans_updown(){ return service.getexp_trans_updown();}


    //당사매매?
    @GetMapping("/traded_by_company")
    public Mono<String> get_traded_by_company(){ return service.gettraded_by_company();}

    //신고/신저근접종목 상위
    @GetMapping("/near_new_highlow")
    public Mono<String> get_near_new_highlow(){ return service.getnear_new_highlow();}

    //대량 체결 상위
    @GetMapping("/bulk_trans_num")
    public Mono<String> get_bulk_trans_num(){ return service.getbulk_trans_num();}

    //공매도 상위
    @GetMapping("/short_sale")
    public Mono<String> get_short_sale(){ return service.getshort_sale();}

    //시간외 거래량
    @GetMapping("/overtime_volume")
    public Mono<String> get_overtime_volume(){ return service.getovertime_volume();}
}

