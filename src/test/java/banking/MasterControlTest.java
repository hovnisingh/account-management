package banking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MasterControlTest {
    List<String> input;
    MasterControl masterControl;
    Bank bank;

    @BeforeEach
    void setUp() {
        input = new ArrayList<>();
        bank = new Bank();
        masterControl = new MasterControl(bank, new CommandValidator(bank), new CommandProcessor(bank), new CommandStorage());
    }

    @Test
    void typo_in_create_command_is_invalid() {
        input.add("creat checking 12345678 1.0");
        List<String> actual = masterControl.start(input);
        assertSingleCommand("creat checking 12345678 1.0", actual);
    }


    @Test
    void typo_in_deposit_command_is_invalid() {
        input.add("depositt 12345678 100");
        List<String> actual = masterControl.start(input);
        assertSingleCommand("depositt 12345678 100", actual);
    }

    @Test
    void two_typo_commands_both_invalid() {
        input.add("creat checking 12345678 0.05");
        input.add("depositt 12345678 100");
        List<String> actual = masterControl.start(input);
        assertEquals(2, actual.size());
        assertEquals("creat checking 12345678 0.05", actual.get(0));
        assertEquals("depositt 12345678 100", actual.get(1));
    }

    @Test
    void invalid_to_create_accounts_with_same_ID() {
        input.add("create checking 12345678 0.05");
        input.add("create checking 12345678 0.05");
        List<String> actual = masterControl.start(input);
        assertEquals(2, actual.size());
        assertEquals("Checking 12345678 0.00 0.05", actual.get(0));
        assertEquals("create checking 12345678 0.05", actual.get(1));
    }

    @Test
    void invalid_to_create_cd_without_balance() {
        input.add("create cd 12345678 0.05");
        List<String> actual = masterControl.start(input);
        assertSingleCommand("create cd 12345678 0.05", actual);
    }


    @Test
    void invalid_to_create_accounts_with_character_in_id() {
        input.add("create checking 1234a678 0.05");
        List<String> actual = masterControl.start(input);
        assertSingleCommand("create checking 1234a678 0.05", actual);
    }

    @Test
    void invalid_to_create_accounts_with_character_in_apr() {
        input.add("create checking 12345678 0.a5");
        List<String> actual = masterControl.start(input);
        assertSingleCommand("create checking 12345678 0.a5", actual);
    }

    @Test
    void invalid_to_create_cd_with_less_than_1000() {
        input.add("create cd 12345678 0.05 100");
        List<String> actual = masterControl.start(input);
        assertSingleCommand("create cd 12345678 0.05 100", actual);
    }

    @Test
    void invalid_to_create_cd_with_more_than_10000() {
        input.add("create cd 12345678 0.05 20000");
        List<String> actual = masterControl.start(input);
        assertSingleCommand("create cd 12345678 0.05 20000", actual);
    }

    @Test
    void invalid_to_deposit_into_cd() {
        input.add("create cd 12345678 0.05 1000");
        input.add("deposit 12345678 100");
        List<String> actual = masterControl.start(input);
        assertEquals(2, actual.size());
        assertEquals("Cd 12345678 1000.00 0.05", actual.get(0));
        assertEquals("deposit 12345678 100", actual.get(1));
    }

    @Test
    void invalid_to_deposit_more_than_2500_into_savings() {
        input.add("create savings 12345678 0.05");
        input.add("deposit 12345678 3000");
        List<String> actual = masterControl.start(input);
        assertEquals(2, actual.size());
        assertEquals("Savings 12345678 0.00 0.05", actual.get(0));
        assertEquals("deposit 12345678 3000", actual.get(1));
    }

    @Test
    void invalid_to_deposit_more_than_1000_into_checking() {
        input.add("create checking 12345678 0.05");
        input.add("deposit 12345678 2000");
        List<String> actual = masterControl.start(input);
        assertEquals(2, actual.size());
        assertEquals("Checking 12345678 0.00 0.05", actual.get(0));
        assertEquals("deposit 12345678 2000", actual.get(1));
    }

    @Test
    void invalid_to_deposit_zero_into_account() {
        input.add("create checking 12345678 0.05");
        input.add("deposit 12345678 0");
        List<String> actual = masterControl.start(input);
        assertEquals(2, actual.size());
        assertEquals("Checking 12345678 0.00 0.05", actual.get(0));
        assertEquals("deposit 12345678 0", actual.get(1));
    }

    @Test
    void invalid_to_withdraw_from_cd_before_12_months() {
        input.add("create cd 12345678 0.05 2000");
        input.add("pass 3");
        input.add("withdraw 12345678 400");
        List<String> actual = masterControl.start(input);
        assertEquals(2, actual.size());
        assertEquals("Cd 12345678 2002.00 0.05", actual.get(0));
        assertEquals("withdraw 12345678 400", actual.get(1));
    }

    @Test
    void invalid_to_withdraw_more_than_1000_from_savings() {
        input.add("create savings 12345678 0.05");
        input.add("deposit 12345678 600");
        input.add("deposit 12345678 600");
        input.add("deposit 12345678 800");
        input.add("withdraw 12345678 1200");
        List<String> actual = masterControl.start(input);
        assertEquals(5, actual.size());
        assertEquals("Savings 12345678 2000.00 0.05", actual.get(0));
        assertEquals("deposit 12345678 600", actual.get(1));
        assertEquals("deposit 12345678 600", actual.get(2));
        assertEquals("deposit 12345678 800", actual.get(3));
        assertEquals("withdraw 12345678 1200", actual.get(4));
    }

    @Test
    void invalid_to_withdraw_more_than_400_from_checking() {
        input.add("create checking 12345678 0.05");
        input.add("deposit 12345678 600");
        input.add("withdraw 12345678 500");
        List<String> actual = masterControl.start(input);
        assertEquals(3, actual.size());
        assertEquals("Checking 12345678 600.00 0.05", actual.get(0));
        assertEquals("deposit 12345678 600", actual.get(1));
        assertEquals("withdraw 12345678 500", actual.get(2));
    }

    @Test
    void invalid_to_transfer_from_cd_account() {
        input.add("create cd 12345678 0.05 1300");
        input.add("create checking 12345679 0.05");
        input.add("transfer 12345678 12345679 500");
        List<String> actual = masterControl.start(input);
        assertEquals(3, actual.size());
        assertEquals("Cd 12345678 1300.00 0.05", actual.get(0));
        assertEquals("Checking 12345679 0.00 0.05", actual.get(1));
        assertEquals("transfer 12345678 12345679 500", actual.get(2));
    }

    @Test
    void invalid_to_transfer_between_same_accounts() {
        input.add("create checking 12345679 0.05");
        input.add("deposit 12345679 300");
        input.add("transfer 12345679 12345679 500");
        List<String> actual = masterControl.start(input);
        assertEquals(3, actual.size());
        assertEquals("Checking 12345679 300.00 0.05", actual.get(0));
        assertEquals("deposit 12345679 300", actual.get(1));
        assertEquals("transfer 12345679 12345679 500", actual.get(2));
    }

    @Test
    void transfer_between_checking_and_savings_account() {
        input.add("create checking 12345679 0.05");
        input.add("deposit 12345679 500");
        input.add("create savings 12365434 0.05");
        input.add("deposit 12365434 300");
        input.add("transfer 12345679 12365434 200");
        List<String> actual = masterControl.start(input);
        assertEquals(6, actual.size());
        assertEquals("Checking 12345679 300.00 0.05", actual.get(0));
        assertEquals("deposit 12345679 500", actual.get(1));
        assertEquals("transfer 12345679 12365434 200", actual.get(2));
        assertEquals("Savings 12365434 500.00 0.05", actual.get(3));
        assertEquals("deposit 12365434 300", actual.get(4));
        assertEquals("transfer 12345679 12365434 200", actual.get(5));
    }

    @Test
    void valid_create_and_deposit_command() {
        input.add("create checking 12345678 0.05");
        input.add("deposit 12345678 600");
        List<String> actual = masterControl.start(input);
        assertEquals(2, actual.size());
        assertEquals("Checking 12345678 600.00 0.05", actual.get(0));
        assertEquals("deposit 12345678 600", actual.get(1));
    }

    @Test
    void withdraw_from_savings_before_month_has_passed_invalid() {
        input.add("create savings 12345678 0.05");
        input.add("deposit 12345678 600");
        input.add("withdraw 12345678 200");
        List<String> actual = masterControl.start(input);
        assertEquals(3, actual.size());
        assertEquals("Savings 12345678 600.00 0.05", actual.get(0));
        assertEquals("deposit 12345678 600", actual.get(1));
        assertEquals("withdraw 12345678 200", actual.get(2));
    }

    @Test
    void withdraw_from_savings_once_after_one_month() {
        input.add("create savings 12345678 0.05");
        input.add("deposit 12345678 600");
        input.add("pass 1");
        input.add("withdraw 12345678 200");
        List<String> actual = masterControl.start(input);
        assertEquals(3, actual.size());
        assertEquals("Savings 12345678 400.03 0.05", actual.get(0));
        assertEquals("deposit 12345678 600", actual.get(1));
        assertEquals("withdraw 12345678 200", actual.get(2));
    }

    @Test
    void withdraw_from_savings_twice_in_one_month_invalid() {
        input.add("create savings 12345678 0.05");
        input.add("deposit 12345678 600");
        input.add("pass 1");
        input.add("withdraw 12345678 200");
        input.add("withdraw 12345678 100");
        List<String> actual = masterControl.start(input);
        assertEquals(4, actual.size());
        assertEquals("Savings 12345678 400.03 0.05", actual.get(0));
        assertEquals("deposit 12345678 600", actual.get(1));
        assertEquals("withdraw 12345678 200", actual.get(2));
        assertEquals("withdraw 12345678 100", actual.get(3));
    }

    @Test
    void closed_account_does_not_output_transaction_history() {
        input.add("Create checking 12345678 0.6");
        input.add("Deposit 12345678 20");
        input.add("withdraw 12345678 20");
        input.add("pass 1");
        List<String> actual = masterControl.start(input);
        assertEquals(0, actual.size());
    }

    @Test
    void account_below_100_deducts_25_after_one_month() {
        input.add("Create checking 12345678 0.6");
        input.add("Deposit 12345678 200");
        input.add("withdraw 12345678 150");
        input.add("pass 1");
        List<String> actual = masterControl.start(input);
        assertEquals(3, actual.size());
        assertEquals("Checking 12345678 25.01 0.60", actual.get(0));
        assertEquals("Deposit 12345678 200", actual.get(1));
        assertEquals("withdraw 12345678 150", actual.get(2));
    }


    @Test
    void sample_make_sure_this_passes_unchanged_or_you_will_fail() {
        input.add("Create savings 12345678 0.6");
        input.add("Deposit 12345678 700");
        input.add("Deposit 12345678 5000");
        input.add("creAte cHecKing 98765432 0.01");
        input.add("Deposit 98765432 300");
        input.add("Transfer 98765432 12345678 300");
        input.add("Pass 1");
        input.add("Create cd 23456789 1.2 2000");
        List<String> actual = masterControl.start(input);
        assertEquals(5, actual.size());
        assertEquals("Savings 12345678 1000.50 0.60", actual.get(0));
        assertEquals("Deposit 12345678 700", actual.get(1));
        assertEquals("Transfer 98765432 12345678 300", actual.get(2));
        assertEquals("Cd 23456789 2000.00 1.20", actual.get(3));
        assertEquals("Deposit 12345678 5000", actual.get(4));
    }


    private void assertSingleCommand(String command, List<String> actual) {
        assertEquals(1, actual.size());
        assertEquals(command, actual.get(0));
    }
}

