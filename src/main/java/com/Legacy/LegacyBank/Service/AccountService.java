package com.Legacy.LegacyBank.Service;
import com.Legacy.LegacyBank.Model.Account;
import com.Legacy.LegacyBank.Model.Transaction;
import com.Legacy.LegacyBank.Repository.AccountRepository;
import com.Legacy.LegacyBank.Repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
public class AccountService implements UserDetailsService {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    public Account findAccountByUserName(String username){
        return accountRepository.findByUsername(username)
                .orElseThrow(()->new RuntimeException("Account not found"));
    }

    public Account registerAccount(String username,String password){
        if(accountRepository.findByUsername(username).isPresent()){
            throw new RuntimeException("UserName already exists");
        }
        Account account =new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setBalance(BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    public void deposit(Account account,BigDecimal amount){
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        Transaction transaction=new Transaction(
                amount,
                "Deposit",
                LocalDateTime.now(),
                account
        );
        transactionRepository.save(transaction);
    }

    public void withdraw(Account account,BigDecimal amount){
        if(account.getBalance().compareTo(amount)<0){
            throw new RuntimeException("Insufficient funds");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        Transaction transaction=new Transaction(
                amount,
                "Withdraw",
                LocalDateTime.now(),
                account
        );
        transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionHistory(Account account){
        return transactionRepository.findByAccountId(account.getId());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account=findAccountByUserName(username);
        if(account==null){
            throw new UsernameNotFoundException("UserName or Password Not found");
        }
        return new Account(
                account.getUsername(),
                account.getPassword(),
                account.getBalance(),
                account.getTransactions(),
                authorities()
        );
    }

    public Collection<?extends GrantedAuthority> authorities(){
        return Arrays.asList(new SimpleGrantedAuthority("user"));
    }

    public void transferAmount(Account fromAccount,String toUserName,BigDecimal amount){
        if(fromAccount.getBalance().compareTo(amount)<0){
            throw new RuntimeException("Insuficient funds");
        }
        Account toAccount =accountRepository.findByUsername(toUserName)
                .orElseThrow(()->new RuntimeException("Recipient Account Not Found"));

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction debittransaction=new Transaction(
                amount,
                "transfer out to "+toAccount.getUsername(),
                LocalDateTime.now(),
                fromAccount
        );
        transactionRepository.save(debittransaction);
        Transaction credittransaction=new Transaction(
                amount,
                "transfer in to "+fromAccount.getUsername(),
                LocalDateTime.now(),
                toAccount
        );
        transactionRepository.save(credittransaction);
    }


}
