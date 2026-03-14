package org.hulei.entity.jpa.pojo;

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
import java.time.Instant;

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

}