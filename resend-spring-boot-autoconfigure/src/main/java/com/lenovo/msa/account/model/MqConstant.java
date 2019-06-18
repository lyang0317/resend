package com.lenovo.msa.account.model;

public class MqConstant {

    public static final String account_exchange = "li-eComm-CrowdTwist-acct";
    public static final String account_dead_letter_exchange = "li-eComm-CrowdTwist-acct.dead-latter";
    public static final String account_bind_key = "account";

    public static final String account_router_key = account_bind_key;

    public static final String account_queue = "queue.li-eComm-CrowdTwist-acct";
    public static final String account_dead_letter_queue = "queue.li-eComm-CrowdTwist-acct.dead-latter";

    public static final String generate_exchange_name(String name, String profile) {
        return name + "#" + profile;
    }

    public static final String header_retry_count_key = "x_retry_number";
    public static final String header_latest_message_key = "x_latest_message";

}
