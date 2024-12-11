package br.com.picpay.picpay_desafio_backend.transaction;

import org.springframework.stereotype.Service;

import br.com.picpay.picpay_desafio_backend.wallet.Wallet;
import br.com.picpay.picpay_desafio_backend.wallet.WalletRepository;
import br.com.picpay.picpay_desafio_backend.wallet.WalletType;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    public TransactionService(TransactionRepository transactionRepository, WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    public Transaction create(Transaction transaction) {
        //1 validar
        validate(transaction);
        //2 criar a transacao
        var newTransaction = transactionRepository.save(transaction);
        //3 debitar da carteira
        var wallet = walletRepository.findById(transaction.payer()).get();
        walletRepository.save(wallet.debit(transaction.value()));
        //4 chamar servicos externos

        return newTransaction;
    }
    
    private void validate(Transaction transaction){
        walletRepository.findById(transaction.payee())
        .map( payee -> walletRepository.findById(transaction.payer())
            .map(payer -> isValidTransaction(transaction, payer) ? transaction : null)
            .orElseThrow())
        .orElseThrow();
    }

    private boolean isValidTransaction(Transaction transaction, Wallet payer) {
        return payer.type() == WalletType.COMUM.getValue() &&
               payer.balance().compareTo(transaction.value()) >= 0 &&
               payer.id() != transaction.payee(); 
    }

}
