package com.example.healthtech;

import java.math.BigDecimal;

public final class RechargePolicyTest {
    public static void main(String[] args) {
        RechargePolicy policy = new RechargePolicy();
        require(policy.decide(new BigDecimal("4.00"), new BigDecimal("5.00"))
                == RechargePolicy.Decision.RECHARGE_REQUIRED);
        require(policy.decide(new BigDecimal("7.00"), new BigDecimal("5.00"))
                == RechargePolicy.Decision.BALANCE_OK);
        System.out.println("RechargePolicyTest passed");
    }

    private static void require(boolean condition) {
        if (!condition) throw new AssertionError("business decision mismatch");
    }
}
