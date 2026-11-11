package com.example.healthtech;

import java.math.BigDecimal;

public final class AppointmentWorkflow {
    private final InfraiClient infrai;
    private final RechargePolicy policy = new RechargePolicy();

    public AppointmentWorkflow(InfraiClient infrai) { this.infrai = infrai; }

    public String prepareClinic(String clinicEmail, BigDecimal triggerBalance, BigDecimal rechargeAmount) {
        String configBody = "{\"trigger_balance\":%s,\"recharge_amount\":%s}".formatted(triggerBalance, rechargeAmount);
        infrai.request("PUT", "/v1/account/autorecharge/configure", configBody);
        String balanceEnvelope = infrai.request("GET", "/v1/account/balance", null);
        String message = "Appointment operations are funded; the clinic queue is ready.";
        String emailBody = "{\"to\":\"%s\",\"subject\":\"Appointment operations ready\",\"body\":\"%s\"}"
                .formatted(clinicEmail, message);
        infrai.request("POST", "/v1/email/send", emailBody);
        return "balance checked: " + balanceEnvelope.contains("\"data\"") + ", notification queued";
    }

    public RechargePolicy.Decision decision(BigDecimal balance, BigDecimal triggerBalance) {
        return policy.decide(balance, triggerBalance);
    }
}
