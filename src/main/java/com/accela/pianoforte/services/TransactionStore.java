package com.accela.pianoforte.services;

import com.accela.pianoforte.model.Response;
import io.vavr.control.Option;

import java.util.concurrent.ConcurrentHashMap;

public class TransactionStore {
    private final ConcurrentHashMap<String, Response> transactions;

    public TransactionStore() {
        transactions = new ConcurrentHashMap<>();
    }

    public void add(final Response response) {
        transactions.putIfAbsent(response.getTransactionId(), response);
    }

    public Option<Response> get(final String transactionId) {
        return Option.of(transactions.get(transactionId));
    }
}
