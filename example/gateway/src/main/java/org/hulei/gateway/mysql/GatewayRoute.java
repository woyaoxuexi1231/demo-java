package org.hulei.gateway.mysql;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Table(name = "gateway_route")
@Data
public class GatewayRoute {

    @Id
    private String id;

    private String uri;

    @Column(columnDefinition = "TEXT")
    private String predicates; // JSON

    @Column(columnDefinition = "TEXT")
    private String filters; // JSON

    private Integer orderNum;

    private Integer status; // 1: enabled

    private LocalDateTime updateTime;
}