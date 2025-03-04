package com.Legacy.LegacyBank.Controller;
import com.Legacy.LegacyBank.Model.Account;
import com.Legacy.LegacyBank.Service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;

@Controller
public class BankController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/dashboard")
    public String dashboard(Model model){
        String userName= SecurityContextHolder.getContext().getAuthentication().getName();
        Account account=accountService.findAccountByUserName(userName);
        model.addAttribute("account",account);
        return "dashboard";
    }

    @GetMapping("/register")
    public String showRegisterationForm(){
        return "register";
    }

    @PostMapping("/register")
    public String registerAccount(
            @RequestParam String username,
            @RequestParam String password,
            Model model
    ){
        try {
            accountService.registerAccount(username,password);
            return "redirect:/login";
        }catch (Exception e){
            model.addAttribute("error",e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @PostMapping("/deposit")
    public String deposit(
            @RequestParam BigDecimal amount
            ){
        String userName=SecurityContextHolder.getContext()
                .getAuthentication().getName();
        Account account=accountService.findAccountByUserName(userName);
        accountService.deposit(account,amount);
        return "redirect:/dashboard";
    }

    @PostMapping("/withdraw")
    public String withdraw(
            @RequestParam BigDecimal amount,
            Model model
    ){
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        Account account=accountService.findAccountByUserName(username);
        try {
            accountService.withdraw(account,amount);
        }
        catch (RuntimeException e){
            model.addAttribute("error",e.getMessage());
            model.addAttribute("account",account);
            return "dashboard";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("transactions")
    public String transactionHistory(Model model){
        String userName=SecurityContextHolder.getContext().getAuthentication().getName();
        Account account=accountService.findAccountByUserName(userName);
        model.addAttribute("transactions",accountService.getTransactionHistory(account));
        return "transactions";
    }

    @PostMapping("transfer")
    public String transferAmount(
            @RequestParam String toUsername,
            @RequestParam BigDecimal amount,
            Model model
    ){
        String userName=SecurityContextHolder.getContext().getAuthentication().getName();
        Account fromAccount =accountService.findAccountByUserName(userName);
        try {
            accountService.transferAmount(fromAccount, toUsername, amount);
        }catch (RuntimeException e){
            model.addAttribute("error",e.getMessage());
            model.addAttribute("account",fromAccount);
            return "dashboard";
        }
        return "redirect:/dashboard";
    }
}
