package com.example.iam.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ConsentController {

    @GetMapping("/oauth2/consent")
    public String consentPage(@RequestParam(value = "client_id", required = false) String clientId,
                             @RequestParam(value = "scope", required = false) String scope,
                             Model model) {
        model.addAttribute("clientId", clientId != null ? clientId : "iam-web-client");
        model.addAttribute("scope", scope != null ? scope : "openid profile email");
        return "consent";
    }

    @PostMapping("/oauth2/consent")
    public String consentSubmit(@RequestParam("client_id") String clientId,
                               @RequestParam("state") String state,
                               @RequestParam(value = "approved", required = false) String approved,
                               HttpServletRequest request) {
        if ("true".equalsIgnoreCase(approved)) {
            return "redirect:/oauth2/authorize?client_id=" + clientId + "&state=" + state + "&user_oauth_approval=true";
        }
        return "redirect:/login?error=consent_denied";
    }
}
