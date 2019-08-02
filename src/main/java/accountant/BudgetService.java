package accountant;

import accountant.vo.Budget;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.time.temporal.ChronoUnit.DAYS;

public class BudgetService {
    private BudgetRepo budgetRepo;


    public BudgetService(BudgetRepo budgetRepo) {
        this.budgetRepo = budgetRepo;
    }

    public double query(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            return 0;
        }

        double totalAmount = 0;
        if (diffMonth(start, end) == 0) {
            Optional<Budget> budget = getBudget(start);
            if (budget.isPresent()) {
                return budget.get().dailyAmount() * dayCount(start, end);
            }
        } else {
            int diffMonth = diffMonth(start, end);

//            Optional<Budget> budgetOfStart = getBudget(start);
//            double firstMonthAmount = 0D;
//            if (budgetOfStart.isPresent()) {
//                Budget firstBudget = budgetOfStart.get();
//                firstMonthAmount = firstBudget.dailyAmount() * dayCount(start, firstBudget.lastDay());
//            }
//            totalAmount += firstMonthAmount;

            Optional<Budget> budgetOfEnd = getBudget(end);
            double lastMonthAmount = 0D;
            if (budgetOfEnd.isPresent()) {
                Budget lastBudget = budgetOfEnd.get();
                lastMonthAmount = lastBudget.dailyAmount() * dayCount(lastBudget.firstDay(), end);
            }
            totalAmount += lastMonthAmount;

            LocalDate currentDate = start;
            for (int i = 0; i < diffMonth; i++) {
                if (YearMonth.from(currentDate).equals(YearMonth.from(start))) {

                    Optional<Budget> budgetOfStart = getBudget(start);
                    double firstMonthAmount = 0D;
                    if (budgetOfStart.isPresent()) {
                        Budget firstBudget = budgetOfStart.get();
                        firstMonthAmount = firstBudget.dailyAmount() * dayCount(start, firstBudget.lastDay());
                    }

                    totalAmount += firstMonthAmount;
                } else {

                    Optional<Budget> budgetOfCurrent = getBudget(currentDate);
                    if (budgetOfCurrent.isPresent()) {
                        Budget currentBudget = budgetOfCurrent.get();
                        totalAmount += currentBudget.dailyAmount() *
                                       dayCount(currentBudget.firstDay(), currentBudget.lastDay());
                    }
                }
                currentDate = start.plusMonths(1);
            }
        }
        return totalAmount;
    }

    private Optional<Budget> getBudget(LocalDate start) {
        DateTimeFormatter formatter = ofPattern("yyyyMM");
        return budgetRepo.getAll().stream()
                         .filter(b -> b.getYearMonth().equals(start.format(formatter))).findFirst();
    }

    private double calculateBudgetAverage(LocalDate current) {
        DateTimeFormatter formatter = ofPattern("yyyyMM");
        for (Budget budget : this.budgetRepo.getAll()) {
            if (budget.getYearMonth().equals(current.format(formatter))) {
                return budget.getAmount() / current.lengthOfMonth();
            }
        }
        return 0D;
    }

    private long dayCount(LocalDate start, LocalDate end) {
        return DAYS.between(start, end) + 1;
    }

    private int diffMonth(LocalDate start, LocalDate end) {
        if (start.getYear() == end.getYear()) {
            return Math.abs(start.getMonth().getValue() - end.getMonth().getValue());
        }
        YearMonth from = YearMonth.from(start);
        return YearMonth.from(end).minusMonths(from.getMonthValue()).getMonthValue();
    }
}
