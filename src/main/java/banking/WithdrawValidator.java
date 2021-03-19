package banking;

public class WithdrawValidator extends CommandValidator {
    Bank bank;
    private int id;
    private int amount;

    public WithdrawValidator(Bank bank) {
        this.bank = bank;
    }

    public boolean withdrawValidate(String command) {
        String[] commandSplit = splitString(command);
        if ((!(withdrawIsValid(commandSplit))) || (IsDigits(commandSplit[1]) == false) || (IsDigits(commandSplit[2]) == false)) {
            return false;
        }
        getVariables(commandSplit);
        if (bank.withdrawAmountTooGreat(id, amount) || amount <= 0) {
            return false;
        }
        if (idIsValid(id)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean withdrawIsValid(String[] commandSplit) {
        if ((commandSplit[0].equalsIgnoreCase(("withdraw"))) && ((commandSplit.length == 3))) {
            return true;
        } else {
            return false;
        }
    }

    private void getVariables(String[] commandSplit) {
        id = Integer.parseInt(commandSplit[1]);
        amount = Integer.parseInt(commandSplit[2]);
    }
}