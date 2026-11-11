package com.example.healthtech;

import java.math.BigDecimal;

public final class AppointmentOperations {
    public static void main(String[] args) {
        if (args.length != 1 || args[0].isBlank()) {
            throw new IllegalArgumentException("usage: AppointmentOperations <clinic-email>");
        }
        LayeredConfig config = LayeredConfig.fromEnvironment();
        InfraiClient infrai = new InfraiClient(config);
        AppointmentWorkflow workflow = new AppointmentWorkflow(infrai);
        System.out.println(workflow.prepareClinic(
                args[0], new BigDecimal("5.00"), new BigDecimal("25.00")));
    }
}
