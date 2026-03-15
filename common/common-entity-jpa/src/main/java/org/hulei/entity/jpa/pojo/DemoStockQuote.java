package org.hulei.entity.jpa.pojo;

import com.github.javafaker.Faker;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
@Entity
@Table(name = "demo_stock_quotes", schema = "test")
public class DemoStockQuote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 10)
    @NotNull
    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Size(max = 50)
    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "current_price", precision = 10, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "change_percent", precision = 10, scale = 2)
    private BigDecimal changePercent;

    @Column(name = "change_amount", precision = 10, scale = 2)
    private BigDecimal changeAmount;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "turnover", precision = 20, scale = 2)
    private BigDecimal turnover;

    @Column(name = "high", precision = 10, scale = 2)
    private BigDecimal high;

    @Column(name = "low", precision = 10, scale = 2)
    private BigDecimal low;

    @Column(name = "open_price", precision = 10, scale = 2)
    private BigDecimal openPrice;

    @Column(name = "pre_close", precision = 10, scale = 2)
    private BigDecimal preClose;

    @Size(max = 20)
    @Column(name = "source", length = 20)
    private String source;

    @Column(name = "data_time")
    private Instant dataTime;

    @Column(name = "created_at")
    private Instant createdAt;

    public static DemoStockQuote gen() {
        Faker faker = new Faker();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        DemoStockQuote quote = new DemoStockQuote();
        quote.setCode("STK" + faker.number().digits(4));
        quote.setName(faker.company().name());

        BigDecimal preClose = BigDecimal.valueOf(random.nextDouble(10.0, 200.0)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal currentPrice = preClose.multiply(BigDecimal.valueOf(1 + random.nextDouble(-0.1, 0.1)))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal openPrice = preClose.multiply(BigDecimal.valueOf(1 + random.nextDouble(-0.05, 0.05)))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal maxBase = openPrice.max(currentPrice);
        BigDecimal minBase = openPrice.min(currentPrice);
        BigDecimal high = maxBase.multiply(BigDecimal.valueOf(1 + random.nextDouble(0.0, 0.03)))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal low = minBase.multiply(BigDecimal.valueOf(1 - random.nextDouble(0.0, 0.03)))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal changeAmount = currentPrice.subtract(preClose).setScale(2, RoundingMode.HALF_UP);
        BigDecimal changePercent = changeAmount.multiply(BigDecimal.valueOf(100))
                .divide(preClose, 2, RoundingMode.HALF_UP);

        long volume = random.nextLong(10_000L, 5_000_000L);
        BigDecimal turnover = currentPrice.multiply(BigDecimal.valueOf(volume)).setScale(2, RoundingMode.HALF_UP);

        Instant dataTime = Instant.now().minus(random.nextLong(1, 300), ChronoUnit.SECONDS);

        quote.setCurrentPrice(currentPrice);
        quote.setChangePercent(changePercent);
        quote.setChangeAmount(changeAmount);
        quote.setVolume(volume);
        quote.setTurnover(turnover);
        quote.setHigh(high);
        quote.setLow(low);
        quote.setOpenPrice(openPrice);
        quote.setPreClose(preClose);
        quote.setSource("faker");
        quote.setDataTime(dataTime);
        quote.setCreatedAt(Instant.now());

        return quote;
    }
}