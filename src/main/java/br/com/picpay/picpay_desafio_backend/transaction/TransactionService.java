package br.com.picpay.picpay_desafio_backend.transaction;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.picpay.picpay_desafio_backend.authorization.AuthorizerService;
import br.com.picpay.picpay_desafio_backend.wallet.Wallet;
import br.com.picpay.picpay_desafio_backend.wallet.WalletRepository;
import br.com.picpay.picpay_desafio_backend.wallet.WalletType;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final AuthorizerService authorizerService;

    public TransactionService(TransactionRepository transactionRepository, WalletRepository walletRepository, AuthorizerService authorizerService) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.authorizerService = authorizerService;
    }

    @Transactional
    public Transaction create(Transaction transaction) {
        validate(transaction);
        var newTransaction = transactionRepository.save(transaction);
        var wallet = walletRepository.findById(transaction.payer()).orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        walletRepository.save(wallet.debit(transaction.value()));

        authorizerService.authorize(transaction);

        return newTransaction;
    }
    
    private void validate(Transaction transaction){
        walletRepository.findById(transaction.payee())
        .map( payee -> walletRepository.findById(transaction.payer())
            .map(payer -> isValidTransaction(transaction, payer) ? transaction : null)
            .orElseThrow(() -> new InvalidTransactionException("Transacao invalida - %s".formatted(transaction))))
        .orElseThrow(() -> new InvalidTransactionException("Transacao invalida - %s".formatted(transaction)));
    }

    private boolean isValidTransaction(Transaction transaction, Wallet payer) {
        return payer.type() == WalletType.COMUM.getValue() &&
               payer.balance().compareTo(transaction.value()) >= 0 &&
               payer.id() != transaction.payee(); 
    }

}
