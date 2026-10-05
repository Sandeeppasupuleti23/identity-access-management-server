package com.example.iam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConsentController {

    @GetMapping("/oauth2/consent")
    public String consentPage(
            @org.springframework.web.bind.annotation.RequestParam("client_id") String clientId,
            @org.springframework.web.bind.annotation.RequestParam("scope") String scope,
            @org.springframework.web.bind.annotation.RequestParam("state") String state,
            Model model) {

        model.addAttribute("clientId", clientId);
        model.addAttribute("scope", scope);
        model.addAttribute("state", state);

        return "consent";
    }
}
