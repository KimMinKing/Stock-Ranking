// DOM Elements
const stockList = document.getElementById('stockList');
const stockDetails = document.getElementById('stockDetails');
const welcomeMessage = document.getElementById('welcomeMessage');
const sortSelect = document.getElementById('sortSelect');
const loading = document.getElementById('loading');
const currentTimeEl = document.getElementById('currentTime');
const marketStatusEl = document.getElementById('marketStatus');

// State
// 전역 변수에 추가
let currentChart = null;
let currentChartPeriod = 'D'; // B, D, W, M

let currentStocks = [];
let selectedStock = null;

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    updateCurrentTime();
    setInterval(updateCurrentTime, 1000);
    loadStocks(sortSelect.value);

    // Event Listeners
    sortSelect.addEventListener('change', function() {
        loadStocks(this.value);
    });

    // Chart button interactions
    const chartBtns = document.querySelectorAll('.chart-btn');
    chartBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            chartBtns.forEach(b => b.classList.remove('active'));
            this.classList.add('active');

            // 차트 기간 설정
            const btnText = this.textContent;

            if (btnText === '분봉') currentChartPeriod = 'B';
            else if (btnText === '일봉') currentChartPeriod = 'D';
            else if (btnText === '주봉') currentChartPeriod = 'W';
            else if (btnText === '월봉') currentChartPeriod = 'M';

            // 선택된 종목이 있으면 차트 업데이트
            if (selectedStock) {
                loadChartData(selectedStock, currentChartPeriod);
            }
        });
    });
});

// Update current time and market status
function updateCurrentTime() {
    const now = new Date();
    const timeString = now.toLocaleTimeString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
    currentTimeEl.textContent = timeString;

    // Update market status (09:00 - 15:30)
    const hour = now.getHours();
    const minute = now.getMinutes();
    const currentTime = hour * 100 + minute;

    const marketOpen = 900; // 09:00
    const marketClose = 1530; // 15:30

    if (currentTime >= marketOpen && currentTime <= marketClose) {
        marketStatusEl.textContent = '시장 열림';
        marketStatusEl.className = 'market-status open';
    } else {
        marketStatusEl.textContent = '시장 마감';
        marketStatusEl.className = 'market-status closed';
    }
}

// 주식 목록 불러오는 api 
function loadStocks(sortKey) {
    showLoading(true);

    var xhr = new XMLHttpRequest();
    xhr.open('GET', `/api/stocks/${sortKey}`, true);

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            showLoading(false);
            if (xhr.status === 200) {
                var data = JSON.parse(xhr.responseText);
                currentStocks = data.output || [];
                renderStockList(currentStocks); //화면에 표시
            } else {
                console.error('Error loading stocks:', xhr.status);
                showError('데이터를 불러오는 중 오류가 발생했습니다.');
            }
        }
    };

    xhr.send();
}


// Show/hide loading
function showLoading(show) {
    loading.style.display = show ? 'block' : 'none';
    stockList.style.display = show ? 'none' : 'block';
}

// Show error message
function showError(message) {
    stockList.innerHTML = `
        <div class="error-message" style="
            text-align: center; 
            padding: 40px 20px; 
            color: #ff5252;
            background: rgba(255, 82, 82, 0.1);
            border-radius: 6px;
            border: 1px solid rgba(255, 82, 82, 0.3);
        ">
            <div style="font-size: 2rem; margin-bottom: 15px;">⚠️</div>
            <p>${message}</p>
            <button onclick="loadStocks(sortSelect.value)" style="
                margin-top: 15px;
                padding: 8px 16px;
                background: #ff5252;
                color: white;
                border: none;
                border-radius: 4px;
                cursor: pointer;
            ">다시 시도</button>
        </div>
    `;
}

// Render stock list
function renderStockList(stocks) {
    if (!stocks || stocks.length === 0) {
        stockList.innerHTML = `
            <div class="no-data" style="
                text-align: center; 
                padding: 40px 20px; 
                color: #a8d8a8;
            ">
                <div style="font-size: 2rem; margin-bottom: 15px;">📊</div>
                <p>표시할 데이터가 없습니다.</p>
            </div>
        `;
        return;
    }

    stockList.innerHTML = '';

    stocks.forEach((stock, index) => {
        const stockElement = createStockElement(stock, index);
        stockList.appendChild(stockElement);
    });
}

// Create stock list item element
function createStockElement(stock, index) {
    const stockItem = document.createElement('div');
    stockItem.className = 'stock-item';
    stockItem.dataset.index = index;

    const code = stock.stck_shrn_iscd || stock.mksc_shrn_iscd || stock.pdno || 'N/A';
    const name = stock.hts_kor_isnm || stock.prdt_name || 'Unknown';
    const price = formatPrice(stock.stck_prpr || stock.thdt_clpr) || 'N/A';
    const rank = stock.data_rank || 'N/A';
    const change = stock.prdy_vrss || 0;

    stockItem.innerHTML = `
        <div class="stock-item-header">
            <div class="stock-name">${name}</div>
            <div class="stock-rank">${rank}</div>
        </div>
        <div class="stock-price ${getChangeClass(change)}">${price}원</div>
        <div class="stock-code">${code}</div>
    `;

    stockItem.addEventListener('click', () => selectStock(stock, stockItem));

    return stockItem;
}

// Select stock and show details
function selectStock(stock, element) {
    // Remove previous selection
    document.querySelectorAll('.stock-item').forEach(item => {
        item.classList.remove('selected');
    });
    // Add selection to current item
    element.classList.add('selected');
    selectedStock = stock;
    loadStockDetails(stock);
    // 차트 데이터 로딩
    console.log("sibar"+currentChartPeriod)
    loadChartData(stock, currentChartPeriod);
}

// Load detailed stock information from stockprice API
function loadStockDetails(stock) {
    const code = stock.stck_shrn_iscd || stock.mksc_shrn_iscd || stock.pdno || '';

    if (!code) {
        console.log('종목 코드가 없음:', stock);
        showStockDetails(stock, null);
        return;
    }

    console.log('종목 선택됨 - 코드:', code);
    console.log('API 호출 URL:', `/api/stockprice/m/${code}`);

    // Show basic info first
    showStockDetails(stock, null);

    // Add loading indicator to details
    const detailsContainer = document.querySelector('#stockDetails .details-content');
    if (detailsContainer) {
        detailsContainer.style.opacity = '0.7';
    }

    var xhr = new XMLHttpRequest();
    xhr.open('GET', `/api/stockprice/m/${code}`, true);

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (detailsContainer) {
                detailsContainer.style.opacity = '1';
            }
            if (xhr.status === 200) {
                var data = JSON.parse(xhr.responseText);

                const output = data.output || data;

                // Update with detailed information
                showStockDetails(stock, output);
            } else {
                console.error('API 호출 에러:', xhr.status);
            }
        }
    };

    xhr.send();
}

// Show stock details with enhanced information
function showStockDetails(stock, detailedData) {
    welcomeMessage.style.display = 'none';
    stockDetails.style.display = 'block';

    const code = stock.stck_shrn_iscd || stock.mksc_shrn_iscd || stock.pdno || 'N/A';
    const name = stock.hts_kor_isnm || stock.prdt_name || 'Unknown';

    // Use detailed data if available, otherwise fall back to basic stock data
    const currentPrice = detailedData ? detailedData.stck_prpr : (stock.stck_prpr || stock.thdt_clpr || 0);
    const change = detailedData ? detailedData.prdy_vrss : (stock.prdy_vrss || 0);
    const changeRate = detailedData ? detailedData.prdy_ctrt : (stock.prdy_ctrt || 0);

    // Update basic info - HTML에 맞는 ID 사용
    safeSetText('stockName', name);
    safeSetText('stockCode', code);
    safeSetText('currentPrice', `${formatPrice(currentPrice)}원`);

    // Update change info
    const priceChangeEl = document.getElementById('priceChange');
    const changeRateEl = document.getElementById('changeRate');

    if (priceChangeEl) {
        priceChangeEl.textContent = `${change > 0 ? '+' : ''}${formatPrice(change)}원`;
        priceChangeEl.className = `info-value change ${getChangeClass(change)}`;
    }

    if (changeRateEl) {
        changeRateEl.textContent = `${changeRate > 0 ? '+' : ''}${changeRate}%`;
        changeRateEl.className = `info-value change-rate ${getChangeClass(changeRate)}`;
    }

    if (detailedData) {
        // HTML에 있는 ID에 맞춰서 업데이트
        safeSetText('volume', formatNumber(detailedData.acml_vol));
        safeSetText('tradeAmount', formatCurrency(detailedData.acml_tr_pbmn));
        safeSetText('upperLimit', formatPrice(detailedData.stck_mxpr) + '원');
        safeSetText('lowerLimit', formatPrice(detailedData.stck_llam) + '원');
        safeSetText('marketCap', formatCurrency(detailedData.hts_avls + '00000000'));
        safeSetText('capital', formatCurrency(detailedData.cpfn + '00000000'));
        safeSetText('foreignRate', detailedData.hts_frgn_ehrt + '%');
        safeSetText('per', detailedData.per || '-');
        safeSetText('pbr', detailedData.pbr || '-');
        safeSetText('eps', detailedData.eps ? formatPrice(detailedData.eps) + '원' : '-');
        safeSetText('bps', detailedData.bps ? formatPrice(detailedData.bps) + '원' : '-');
        safeSetText('marginRate', detailedData.marg_rate ? detailedData.marg_rate + '%' : '-');
        safeSetText('market', detailedData.rprs_mrkt_kor_name || '-');
        safeSetText('sector', detailedData.bstp_kor_isnm || '-');
        safeSetText('listedSharesCount', formatNumber(detailedData.lstn_stcn) + '주');

        // 시고저종 (OHLC)
        safeSetText('openPrice', formatPrice(detailedData.stck_oprc) + '원');
        safeSetText('highPrice', formatPrice(detailedData.stck_hgpr) + '원');
        safeSetText('lowPrice', formatPrice(detailedData.stck_lwpr) + '원');
        safeSetText('basePrice', formatPrice(detailedData.stck_sdpr) + '원');

        // 250일 최고/최저 (HTML에는 week250High, week250Low ID가 있음)
        safeSetText('week250High', detailedData.d250_hgpr ? formatPrice(detailedData.d250_hgpr) + '원' : '-');
        safeSetText('week250Low', detailedData.d250_lwpr ? formatPrice(detailedData.d250_lwpr) + '원' : '-');

        // 매매 정보
        safeSetText('foreignNetBuy', detailedData.frgn_ntby_qty ? formatNumber(detailedData.frgn_ntby_qty) + '주' : '-');
        safeSetText('programNetBuy', detailedData.pgtr_ntby_qty ? formatNumber(detailedData.pgtr_ntby_qty) + '주' : '-');
    }
}

// Utility function for safe text setting
function safeSetText(elementId, text) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = text;
    } else {
        console.warn(`Element with id '${elementId}' not found`);
    }
}

// Utility functions
function formatPrice(price) {
    if (!price || price === 0) return '0';
    return parseInt(price).toLocaleString();
}

function formatNumber(num) {
    if (!num || num === 0) return '0';
    const number = parseInt(num);
    if (number >= 100000000) {
        return (number / 100000000).toFixed(1) + '억';
    } else if (number >= 10000) {
        return (number / 10000).toFixed(1) + '만';
    }
    return number.toLocaleString();
}

function formatCurrency(amount) {
    if (!amount || amount === 0) return '0원';
    const number = parseInt(amount);
    if (number >= 1000000000000) {
        return (number / 1000000000000).toFixed(1) + '조원';
    } else if (number >= 100000000) {
        return (number / 100000000).toFixed(1) + '억원';
    } else if (number >= 10000) {
        return (number / 10000).toFixed(1) + '만원';
    }
    return number.toLocaleString() + '원';
}

function formatPercent(rate) {
    if (!rate || rate === 0) return '0%';
    return parseFloat(rate).toFixed(2) + '%';
}

function getChangeClass(value) {
    const num = parseFloat(value);
    if (num > 0) return 'positive';
    if (num < 0) return 'negative';
    return '';
}

// Keyboard navigation
document.addEventListener('keydown', function(e) {
    if (!currentStocks.length) return;

    const stockItems = document.querySelectorAll('.stock-item');
    const selectedIndex = Array.from(stockItems).findIndex(item => item.classList.contains('selected'));

    let newIndex = selectedIndex;

    switch(e.key) {
        case 'ArrowUp':
            e.preventDefault();
            newIndex = selectedIndex > 0 ? selectedIndex - 1 : stockItems.length - 1;
            break;
        case 'ArrowDown':
            e.preventDefault();
            newIndex = selectedIndex < stockItems.length - 1 ? selectedIndex + 1 : 0;
            break;
        case 'Enter':
            if (selectedIndex >= 0) {
                const stock = currentStocks[selectedIndex];
                selectStock(stock, stockItems[selectedIndex]);
            }
            break;
    }

    if (newIndex !== selectedIndex && newIndex >= 0) {
        const stock = currentStocks[newIndex];
        selectStock(stock, stockItems[newIndex]);
        stockItems[newIndex].scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
});


// 차트 데이터 로딩 함수
function loadChartData(stock, period = 'D') {
    const code = stock.stck_shrn_iscd || stock.mksc_shrn_iscd || stock.pdno || '';
    if (!code) {
        console.log('차트 로딩 실패: 종목 코드가 없음');
        return;
    }
    console.log(`차트 데이터 로딩: ${code}, 기간: ${period}`);

    const xhr = new XMLHttpRequest();

    // period가 'B'이면 분단위 API 사용, 아니면 기존 일봉 API 사용
    const apiUrl = period === 'B'
        ? `/api/stockminute/${code}/full-day`
        : `/api/stockdaily/${code}?period=${period}`;

    xhr.open('GET', apiUrl, true);
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                const data = JSON.parse(xhr.responseText);
                console.log('차트 데이터 응답:', data);

                // 분단위 데이터와 일봉 데이터 구분하여 처리
                if (period === 'B') {
                    if (data.minute_data && data.minute_data.length > 0) {
                        renderChart(data.minute_data, stock, 'B');
                    } else {
                        // 차트 데이터 없으면 차트 영역 비우기
                        document.querySelector('.chart-container').innerHTML = '';
                    }
                } else {
                    if (data.output && data.output.length > 0) {
                        renderChart(data.output, stock, period);
                    } else {
                        // 차트 데이터 없으면 차트 영역 비우기
                        document.querySelector('.chart-container').innerHTML = '';
                    }
                }
            } else {
                console.error('차트 데이터 로딩 실패:', xhr.status);
                document.querySelector('.chart-container').innerHTML = '';
            }
        }
    };
    xhr.send();
}

// 트레이딩뷰 차트 렌더링 - 2배 확대 버전
function renderChart(chartData, stock, period = 'D') {
    const chartContainer = document.querySelector('.chart-container');
    // 기존 차트 제거
    chartContainer.innerHTML = '';

    console.log('renderChart 호출됨:', { period, dataLength: chartData.length });
    console.log('첫 번째 데이터 샘플:', chartData[0]);

    // 컨테이너 크기 그대로 사용
    const containerWidth = chartContainer.offsetWidth;
    const chartHeight = 250; // 원래 높이 유지

    // 트레이딩뷰 라이트 차트 생성
    const chart = LightweightCharts.createChart(chartContainer, {
        width: containerWidth,
        height: chartHeight,
        layout: {
            background: { color: '#ffffff' },
            textColor: '#000000'
        },
        grid: {
            vertLines: { color: '#eeeeee' },
            horzLines: { color: '#eeeeee' }
        },
        timeScale: {
            timeVisible: true,
            secondsVisible: period === 'B', // 분단위일 때만 초 표시
            rightOffset: 0,
            barSpacing: period === 'B' ? 60 : 120 // 캔들 간격을 2배로 확대
            // minBarSpacing 제거 - 자동 조정되게 놔둠
        }
    });

    const series = chart.addCandlestickSeries({
        upColor: '#ff3b3b', // 빨강 (양봉)
        downColor: '#0acf97', // 초록 (음봉)
        borderUpColor: '#ff3b3b',
        borderDownColor: '#0acf97',
        wickUpColor: '#ff3b3b',
        wickDownColor: '#0acf97',
        priceLineVisible: true,
        lastValueVisible: true,
        // 캔들스틱 두께 조정으로 시각적 확대 효과
        borderVisible: true,
        borderColor: '#000000',
        wickVisible: true
    });

    // 먼저 데이터를 포맷팅해서 가격 변동폭 계산
    const formattedData = chartData.map(item => {
        if (period === 'B') {
            // 분단위 데이터 처리 - 수정된 부분
            const dateStr = item.stck_bsop_date; // "20250613"
            const timeStr = item.stck_cntg_hour; // "090000"

            // 한국 시간 기준으로 Date 객체 생성
            const year = parseInt(dateStr.slice(0, 4));
            const month = parseInt(dateStr.slice(4, 6)) - 1;
            const day = parseInt(dateStr.slice(6, 8));
            const hour = parseInt(timeStr.slice(0, 2));
            const minute = parseInt(timeStr.slice(2, 4));

            // UTC 시간으로 생성한 후 한국 시간대로 조정 (UTC+9)
            const utcDate = new Date(Date.UTC(year, month, day, hour - 9, minute));

            if (isNaN(utcDate.getTime())) {
                console.error('Invalid date:', { dateStr, timeStr, year, month, day, hour, minute });
                return null;
            }

            const result = {
                time: Math.floor(utcDate.getTime() / 1000),
                open: parseFloat(item.stck_oprc) || 0,
                high: parseFloat(item.stck_hgpr) || 0,
                low: parseFloat(item.stck_lwpr) || 0,
                close: parseFloat(item.stck_prpr) || 0 // 분단위는 stck_prpr이 현재가
            };

            // 디버깅용 로그 (처음 5개만)
            if (chartData.indexOf(item) < 5) {
                console.log('분봉 데이터 변환:', {
                    original: { dateStr, timeStr },
                    parsed: { year, month: month + 1, day, hour, minute },
                    timestamp: result.time,
                    utcDate: utcDate.toISOString(),
                    localDate: new Date(result.time * 1000).toLocaleString('ko-KR'),
                    ohlc: { o: result.open, h: result.high, l: result.low, c: result.close }
                });
            }

            return result;
        } else {
            // 일봉 데이터 처리 (기존 로직)
            const dateStr = item.stck_bsop_date;
            const year = parseInt(dateStr.slice(0, 4));
            const month = parseInt(dateStr.slice(4, 6)) - 1;
            const day = parseInt(dateStr.slice(6, 8));
            const date = new Date(year, month, day);

            return {
                time: Math.floor(date.getTime() / 1000),
                open: parseFloat(item.stck_oprc) || 0,
                high: parseFloat(item.stck_hgpr) || 0,
                low: parseFloat(item.stck_lwpr) || 0,
                close: parseFloat(item.stck_clpr) || 0
            };
        }
    }).filter(item => item !== null) // null 값 제거
        .sort((a, b) => a.time - b.time); // 시간순 정렬

    console.log('포맷된 데이터 개수:', formattedData.length);
    console.log('첫 번째 포맷된 데이터:', formattedData[0]);
    console.log('마지막 포맷된 데이터:', formattedData[formattedData.length - 1]);

    if (formattedData.length === 0) {
        console.error('포맷된 데이터가 없습니다!');
        chartContainer.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">차트 데이터를 표시할 수 없습니다.</div>';
        return;
    }

    // 차트에 데이터 설정
    try {
        series.setData(formattedData);
        console.log('차트 데이터 설정 완료');

        // 차트 시간 범위를 데이터에 맞게 설정
        if (formattedData.length > 0) {
            const firstTime = formattedData[0].time;
            const lastTime = formattedData[formattedData.length - 1].time;

            // 시간 범위 설정 (약간의 여백 추가)
            chart.timeScale().setVisibleRange({
                from: firstTime,
                to: lastTime
            });

            console.log('시간 범위 설정:', {
                from: new Date(firstTime * 1000).toLocaleString('ko-KR'),
                to: new Date(lastTime * 1000).toLocaleString('ko-KR')
            });
        }
    } catch (error) {
        console.error('차트 데이터 설정 오류:', error);
        chartContainer.innerHTML = '<div style="padding: 20px; text-align: center; color: #f00;">차트 렌더링 오류가 발생했습니다.</div>';
        return;
    }

    currentChart = chart;

    // ResizeObserver 제거 - 고정 크기로 사용
    console.log('차트 생성 완료 - 내용물 확대됨');
}

// Auto-refresh data every 30 seconds
setInterval(() => {
    if (sortSelect.value) {
        loadStocks(sortSelect.value);
    }
}, 300000);