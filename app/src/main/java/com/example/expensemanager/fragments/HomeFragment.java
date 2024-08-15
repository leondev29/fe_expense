import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.expensemanager.R;
import com.example.expensemanager.adapters.ExpenseAdapter;
import com.example.expensemanager.models.Expense;
import com.example.expensemanager.viewmodels.HomeViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private ExpenseAdapter expenseAdapter;
    private TextView textViewWalletBalance;
    private BarChart barChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        textViewWalletBalance = view.findViewById(R.id.textViewWalletBalance);
        barChart = view.findViewById(R.id.barChart);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewExpenses);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        expenseAdapter = new ExpenseAdapter();
        recyclerView.setAdapter(expenseAdapter);

        // Fetch wallet balance and expenses
        homeViewModel.fetchWalletBalance(getContext());
        homeViewModel.fetchExpenses(getContext());

        // Quan sát số dư ví
        homeViewModel.getWalletBalance().observe(getViewLifecycleOwner(), balance -> {
            textViewWalletBalance.setText(String.format("$%.2f", balance));
        });

        // Quan sát danh sách chi tiêu
        homeViewModel.getExpenses().observe(getViewLifecycleOwner(), expenses -> {
            expenseAdapter.setExpenses(expenses);
            updateBarChart(expenses);
        });

        // Xử lý sự kiện khi nhấn FAB để thêm chi tiêu mới
        view.findViewById(R.id.fabAddExpense).setOnClickListener(v -> {
            // Xử lý khi thêm chi tiêu mới
        });

        return view;
    }

    private void updateBarChart(List<Expense> expenses) {
        List<BarEntry> entries = new ArrayList<>();
        Map<String, Double> dateToAmountMap = new TreeMap<>();

        // Gom nhóm chi tiêu theo ngày
        for (Expense expense : expenses) {
            String date = expense.getDate();
            double amount = expense.getAmount();

            dateToAmountMap.put(date, dateToAmountMap.getOrDefault(date, 0.0) + amount);
        }

        // Chuẩn bị dữ liệu cho BarChart
        int index = 0;
        List<String> labels = new ArrayList<>();
        for (Map.Entry<String, Double> entry : dateToAmountMap.entrySet()) {
            entries.add(new BarEntry(index++, entry.getValue().floatValue()));
            labels.add(entry.getKey());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Expenses");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Cài đặt X-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate(); // Refresh lại biểu đồ
    }
}
