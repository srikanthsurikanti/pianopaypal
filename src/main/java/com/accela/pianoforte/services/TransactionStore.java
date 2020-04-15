package com.accela.pianoforte.services;

import com.accela.pianoforte.model.Response;
import io.vavr.control.Option;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionStore {
    private final ConcurrentHashMap<URI, Response> transactions;

    public TransactionStore() {
        transactions = new ConcurrentHashMap<>();
    }

    public void add(final Option<Response> maybeResponse) {
        maybeResponse.forEach(response ->
            transactions.putIfAbsent(response.getTransactionId(), response));
    }

    public Option<Response> get(final URI transactionId) {
        return Option.of(transactions.get(transactionId));
    }
}
