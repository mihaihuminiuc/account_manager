package com.example.home.accountmanager.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.home.accountmanager.R;
import com.example.home.accountmanager.model.ExpensesContract;
import com.example.home.accountmanager.util.Utils;


public class SimpleExpenseAdapter extends CursorAdapter {
    private String mCurrency;

    public SimpleExpenseAdapter(Context context) {
        super(context, null, 0);
    }

    public void setCurrency(String currency) {
        mCurrency = currency;
        notifyDataSetChanged();
    }

    // The newView method is used to inflate a new view and return it
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.expense_list_item, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvExpenseValue = view.findViewById(R.id.expense_value_text_view);
        TextView tvExpenseCurrency = view.findViewById(R.id.expense_currency_text_view);
        TextView tvExpenseCatName = view.findViewById(R.id.expense_category_name_text_view);

        // Populate views with extracted values
        tvExpenseValue.setText(Utils.formatToCurrency(cursor.getFloat(cursor.getColumnIndexOrThrow(ExpensesContract.Expenses.VALUE))));
        tvExpenseCatName.setText(cursor.getString(cursor.getColumnIndexOrThrow(ExpensesContract.Categories.NAME)));
        tvExpenseCurrency.setText(mCurrency);
    }
}
