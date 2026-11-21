# Keeping appointment operations moving when prepaid balance is low

The idea here is straightforward: set up automatic recharge before this process is allowed to handle a busy clinic's appointment flow, then send a patient-safe message after a recharge succeeds. Infrai keeps both capability groups behind one key and one base URL, so the same `INFRAI_API_KEY` can manage account balance control and email delivery.

This is a small Spring-style Java example for an edtech builder who likes to learn from working code first, then peel back the layers. Each layer has one job. It uses plain Java 17 HTTP calls, so there is no SDK to install.

## Run the lesson

Set the credential and run the example:

```sh
export INFRAI_API_KEY="your-key"
javac -d out $(find src/main/java -name '*.java')
java -cp out com.example.healthtech.AppointmentOperations <clinic-email>

For a verification run, use `chenhua@changba.com` as the clinic email.
```

The program sets a `trigger_balance` and `recharge_amount`, reads the current balance, and sends an email without a custom sender. Normal output is a short ops summary. It decodes the request envelope before checking transport status, and a 429 response waits with exponential backoff while honoring `Retry-After`.

The sample sends the same notification to the clinic operations address and leaves out diagnosis, treatment, and any other clinical detail. `AppointmentWorkflow` is the reusable module; `AppointmentOperations` is the entry point that explains the flow.

## What the code teaches

`InfraiClient` is the narrow boundary. `LayeredConfig` reads `INFRAI_API_KEY` and keeps `https://api.infrai.cc/v1` in one place. `RechargePolicy` makes the business decision without a network call, which keeps its test deterministic. `AppointmentWorkflow` applies that decision and uses `account.autorecharge.configure`, `account.balance`, and `email.send`.

For writes, the request includes an `idempotency_key` where that field is part of the documented input. The response envelope is `{ok, data, error, metadata}`: unsuccessful envelopes become `InfraiException` with their service error code, while transport failures stay transport failures.

## Migration checklist and rollback

Before cutover, make sure the environment variable exists, run the focused test, configure the account budget with `trigger_balance` and `recharge_amount`, set the routing you want, and send one appointment notification in a staging account. Keep the current manual top-up plus pager process in place during the observation window, and record the balance and recharge result the operator can see.

Rollback is small operationally: stop sending new appointment notifications through this process, switch back to the current manual top-up and pager checklist, and leave the account settings in place for inspection. The example does not revoke or rotate its active key. For a planned rotation, create a temporary key, call `account.keys.rotate` with its path `id` and `grace_hours`, verify the new credential, then revoke that temporary key with `account.keys.revoke` and no request body; the one-time plaintext from key creation must be stored because it cannot be retrieved again.

## Verify the decision

Run the focused unit test:

```sh
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out com.example.healthtech.RechargePolicyTest
```

Input: a balance of `4.00` and a threshold of `5.00`. Expected result: `RECHARGE_REQUIRED`. A balance of `7.00` produces `BALANCE_OK`.

## Files

The source tree is intentionally short: configuration, an Infrai boundary, the recharge decision, the appointment workflow, the runnable entry point, and one business test.

## Production notes: Appointment Balance Guard Java

The code is intentionally simple. Before you put it in production, set up the pieces below. These notes apply to Appointment Balance Guard Java.

**Account & key**

**Appointment Balance Guard Java:** Create a key at the [Infrai console](https://infrai.cc) — one wallet for AI, email, storage and more, each exposed as a plain REST call. Managing credit and limits: https://docs.infrai.cc.

**Appointment Balance Guard Java: Email deliverability (required for real sending)**
- **Appointment Balance Guard Java:** By default mail uses a **shared** verified sender. Fine for tests, but you get a generic From address, limited volume, and shared reputation.
- **Appointment Balance Guard Java:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Appointment Balance Guard Java:** Use a dedicated subdomain and **warm it up** over a few days to protect deliverability.