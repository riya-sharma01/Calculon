package com.calculon.controller;

import com.calculon.entity.MathDomain;
import com.calculon.repository.MathDomainRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/domains")
public class DomainController {

    private final MathDomainRepository domainRepository;

    public DomainController(MathDomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }

    @GetMapping
    public List<MathDomain> listDomains() {
        return domainRepository.findAll();
    }
}
