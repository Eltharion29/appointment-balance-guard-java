package com.example.healthtech;

import java.math.BigDecimal;

public final class RechargePolicy {
    public Decision decide(BigDecimal balance, BigDecimal triggerBalance) {
        return balance.compareTo(triggerBalance) <= 0 ? Decision.RECHARGE_REQUIRED : Decision.BALANCE_OK;
    }

    public enum Decision { RECHARGE_REQUIRED, BALANCE_OK }
}
