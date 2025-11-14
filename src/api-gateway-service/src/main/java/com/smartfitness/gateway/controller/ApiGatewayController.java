package com.smartfitness.gateway.controller;

import com.smartfitness.gateway.service.IRequestRoutingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Interface Layer: API Gateway Entry Point
 * Component: ApiGatewayController
 * Receives ALL external client requests (Customer, Helper, BranchOwner apps)
 * Reference: 07_ApiGatewayComponent.puml (IApiGatewayEntry)
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ApiGatewayController implements IApiGatewayEntry {
    
    private final IRequestRoutingService requestRoutingService;
    
    /**
     * Forward all requests to RequestRouter
     * RequestRouter will handle: Security → ServiceDiscovery → LoadBalancing
     */
    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> routeRequest(HttpServletRequest request, @RequestBody(required = false) Object body) {
        log.info("Gateway received request: {} {}", request.getMethod(), request.getRequestURI());
        
        return requestRoutingService.routeRequest(
                request.getMethod(),
                request.getRequestURI(),
                request.getHeaderNames(),
                body
        );
    }
}

