package org.hulei.gateway.mysql;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface GatewayRouteRepository extends JpaRepository<GatewayRoute, String> {
    List<GatewayRoute> findByStatus(Integer status);
}