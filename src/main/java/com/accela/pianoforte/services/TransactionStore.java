package com.accela.pianoforte.services;

import com.accela.pianoforte.model.Response;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionStore {
    private final ConcurrentHashMap<String, Response> transactions;

    public TransactionStore() {
        transactions = new ConcurrentHashMap<>();
    }

    public void add(final Response response) {
        transactions.putIfAbsent(response.getTransactionId(), response);
    }

    public Optional<Response> get(final String transactionId) {
        return Optional.ofNullable(transactions.get(transactionId));
    }
}
