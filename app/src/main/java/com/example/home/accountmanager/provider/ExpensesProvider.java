package com.example.home.accountmanager.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.example.home.accountmanager.model.ExpensesContract;
import com.example.home.accountmanager.model.ExpensesContract.Categories;
import com.example.home.accountmanager.model.ExpensesContract.Expenses;
import com.example.home.accountmanager.util.database.ExpenseDbHelper;

import static com.example.home.accountmanager.util.database.ExpenseDbHelper.CATEGORIES_TABLE_NAME;
import static com.example.home.accountmanager.util.database.ExpenseDbHelper.EXPENSES_TABLE_NAME;


public class ExpensesProvider extends ContentProvider {
    public static final int EXPENSE = 1001;
    public static final int EXPENSE_ID = 1101;

    public static final int CATEGORY = 2001;
    public static final int CATEGORY_ID = 2101;

    public static final int EXPENSE_WITH_CATEGORY = 3001;
    public static final int EXPENSE_WITH_CATEGORY_DATE = 3101;
    public static final int EXPENSE_WITH_CATEGORY_DATE_RANGE = 3201;
    public static final int EXPENSE_WITH_CATEGORY_SUM_DATE = 3301;
    public static final int EXPENSE_WITH_CATEGORY_SUM_DATE_RANGE = 3401;

    private SQLiteOpenHelper mDbHelper;
    private SQLiteDatabase mDatabase;

    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        mUriMatcher.addURI(ExpensesContract.AUTHORITY, "expenses", EXPENSE);
        mUriMatcher.addURI(ExpensesContract.AUTHORITY, "expenses/#", EXPENSE_ID);
        mUriMatcher.addURI(ExpensesContract.AUTHORITY, "categories", CATEGORY);
        mUriMatcher.addURI(ExpensesContract.AUTHORITY, "categories/#", CATEGORY_ID);
        mUriMatcher.addURI(ExpensesContract.AUTHORITY, "expensesWithCategories",
                EXPENSE_WITH_CATEGORY);
        mUriMatcher.addURI(ExpensesContract.AUTHORITY, "expensesWithCategories/date",
                EXPENSE_WITH_CATEGORY_DATE);
        mUriMatcher.addURI(ExpensesContract.AUTHORITY, "expensesWithCategories/dateRange",
                EXPENSE_WITH_CATEGORY_DATE_RANGE);
        mUriMatcher.addURI(ExpensesContract.AUTHORITY, "expensesWithCategories/date/sum",
                EXPENSE_WITH_CATEGORY_SUM_DATE);
        mUriMatcher.addURI(ExpensesContract.AUTHORITY, "expensesWithCategories/dateRange/sum",
                EXPENSE_WITH_CATEGORY_SUM_DATE_RANGE);
    }

    /*
     * SELECT expenses._id, expenses.value, categories.name, expenses.date
     * FROM expenses JOIN categories
     * ON expenses.category_id = categories._id
     */
    private static final String SELECT_EXPENSES_WITH_CATEGORIES_QUERY =
            "SELECT " + EXPENSES_TABLE_NAME + "." + ExpensesContract.Expenses._ID + ", " +
                    EXPENSES_TABLE_NAME + "." + Expenses.VALUE + ", " +
                    CATEGORIES_TABLE_NAME + "." + Categories.NAME + ", " +
                    EXPENSES_TABLE_NAME + "." + Expenses.DATE + " FROM " +
                    EXPENSES_TABLE_NAME + " JOIN " + CATEGORIES_TABLE_NAME + " ON " +
                    EXPENSES_TABLE_NAME + "." + Expenses.CATEGORY_ID + " = " +
                    CATEGORIES_TABLE_NAME + "." + Categories._ID;

    /**
     * <p>
     * Initializes the provider.
     * </p>
     *
     * <i>Note</i>: provider is not created until a
     * {@link android.content.ContentResolver ContentResolver} object tries to access it.
     *
     * @return <code>true</code> if the provider was successfully loaded, <code>false</code> otherwise
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new ExpenseDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        String table;
        String rawQuery;
        mDatabase = mDbHelper.getReadableDatabase();
        switch (mUriMatcher.match(uri)) {
            // The incoming URI is for all of categories
            case CATEGORY:
                table = CATEGORIES_TABLE_NAME;
                sortOrder = (sortOrder == null || sortOrder.isEmpty())
                        ? Categories.DEFAULT_SORT_ORDER
                        : sortOrder;
                break;

            // The incoming URI is for a single row from categories
            case CATEGORY_ID:
                table = CATEGORIES_TABLE_NAME;
                // Defines selection criteria for the row to query
                selection = Categories._ID + " = ?";
                selectionArgs = new String[]{ uri.getLastPathSegment() };
                break;

            // The incoming URI is for all of expenses
            case EXPENSE:
                table = EXPENSES_TABLE_NAME;
                sortOrder = (sortOrder == null || sortOrder.isEmpty())
                        ? Expenses.DEFAULT_SORT_ORDER
                        : sortOrder;
                break;

            // The incoming URI is for a single row from expenses
            case EXPENSE_ID:
                table = EXPENSES_TABLE_NAME;
                // Defines selection criteria for the row to query
                selection = Expenses._ID + " = ?";
                selectionArgs = new String[]{ uri.getLastPathSegment() };
                break;

            // The incoming URI is for all expenses with categories
            case EXPENSE_WITH_CATEGORY:
                /*
                 * SELECT expenses._id, expenses.value, categories.name, expenses.date
                 * FROM expenses JOIN categories
                 * ON expenses.category_id = categories._id
                 */
                return mDatabase.rawQuery(SELECT_EXPENSES_WITH_CATEGORIES_QUERY, null);

            // The incoming URI is for the expenses with categories for a specific date
            case EXPENSE_WITH_CATEGORY_DATE:
                /*
                 * SELECT expenses._id, expenses.value, categories.name, expenses.date
                 * FROM expenses JOIN categories
                 * ON expenses.category_id = categories._id
                 * WHERE expense.date = ?
                 */
                rawQuery =
                        SELECT_EXPENSES_WITH_CATEGORIES_QUERY + " WHERE " +
                                EXPENSES_TABLE_NAME + "." + Expenses.DATE + " = ?";

                return mDatabase.rawQuery(rawQuery, selectionArgs);

            // The incoming URI is for the expense values sum for a specific date range
            case EXPENSE_WITH_CATEGORY_SUM_DATE:
                /*
                 * SELECT SUM(expenses.value) as values_sum
                 * FROM expenses WHERE expenses.date = ?
                 */
                rawQuery =
                        "SELECT SUM(" + EXPENSES_TABLE_NAME + "." + Expenses.VALUE + ") as " +
                                Expenses.VALUES_SUM + " FROM " + EXPENSES_TABLE_NAME +
                                " WHERE " + EXPENSES_TABLE_NAME + "." + Expenses.DATE + " = ?";

                return mDatabase.rawQuery(rawQuery, selectionArgs);

            // The incoming URI is for the expenses with categories for a specific date range
            case EXPENSE_WITH_CATEGORY_DATE_RANGE:
                /*
                 * SELECT expenses._id, expenses.value, categories.name, expenses.date
                 * FROM expenses JOIN categories
                 * ON expenses.category_id = categories._id
                 * WHERE expense.date BETWEEN ? AND ?
                 */
                rawQuery =
                        SELECT_EXPENSES_WITH_CATEGORIES_QUERY + " WHERE " +
                                EXPENSES_TABLE_NAME + "." + Expenses.DATE + " BETWEEN ? AND ?";

                return mDatabase.rawQuery(rawQuery, selectionArgs);

            // The incoming URI is for the expense values sum for a specific date range
            case EXPENSE_WITH_CATEGORY_SUM_DATE_RANGE:
                /*
                 * SELECT SUM(expenses.value) as values_sum
                 * FROM expenses WHERE expense.date BETWEEN ? AND ?
                 */
                rawQuery =
                        "SELECT SUM(" + EXPENSES_TABLE_NAME + "." + Expenses.VALUE + ") as " +
                                Expenses.VALUES_SUM + " FROM " + EXPENSES_TABLE_NAME +
                                " WHERE " + EXPENSES_TABLE_NAME + "." + Expenses.DATE + " BETWEEN ? AND ?";

                return mDatabase.rawQuery(rawQuery, selectionArgs);

            default:
                throw new IllegalArgumentException("Unknown Uri provided.");
        }

        cursor = mDatabase.query(
                table,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String table;
        Uri contentUri;
        switch (mUriMatcher.match(uri)) {
            // The incoming URI is for all of categories
            case CATEGORY:
                table = CATEGORIES_TABLE_NAME;
                contentUri = Categories.CONTENT_URI;
                break;
            // The incoming URI is for all of expenses
            case EXPENSE:
                table = EXPENSES_TABLE_NAME;
                contentUri = Expenses.CONTENT_URI;
                break;
            // The incoming URI is for a single row from categories
            case CATEGORY_ID:
                // The incoming URI is for a single row from expenses
            case EXPENSE_ID:
                throw new UnsupportedOperationException("Inserting rows with specified IDs is forbidden.");
            case EXPENSE_WITH_CATEGORY:
            case EXPENSE_WITH_CATEGORY_DATE:
            case EXPENSE_WITH_CATEGORY_DATE_RANGE:
            case EXPENSE_WITH_CATEGORY_SUM_DATE:
            case EXPENSE_WITH_CATEGORY_SUM_DATE_RANGE:
                throw new UnsupportedOperationException("Modifying joined results is forbidden.");
            default:
                throw new IllegalArgumentException("Unknown Uri provided.");
        }

        mDatabase = mDbHelper.getWritableDatabase();

        long newRowID = mDatabase.insert(
                table,
                null,
                values
        );

        Uri newItemUri = ContentUris.withAppendedId(contentUri, newRowID);

        return (newRowID < 1) ? null : newItemUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String table;
        switch (mUriMatcher.match(uri)) {
            // The incoming URI is for a single row from categories
            case CATEGORY_ID:
                table = CATEGORIES_TABLE_NAME;
                // Defines selection criteria for the row to delete
                selection = Categories._ID + " = ?";
                selectionArgs = new String[]{ uri.getLastPathSegment() };
                break;
            // The incoming URI is for all of expenses
            case EXPENSE:
                table = EXPENSES_TABLE_NAME;
                break;
            // The incoming URI is for a single row from expenses
            case EXPENSE_ID:
                table = EXPENSES_TABLE_NAME;
                // Defines selection criteria for the row to delete
                selection = Expenses._ID + " = ?";
                selectionArgs = new String[]{ uri.getLastPathSegment() };
                break;
            // The incoming URI is for all of categories
            case CATEGORY:
                throw new UnsupportedOperationException("Removing multiple rows from the table is forbidden.");
            case EXPENSE_WITH_CATEGORY:
            case EXPENSE_WITH_CATEGORY_DATE:
            case EXPENSE_WITH_CATEGORY_DATE_RANGE:
            case EXPENSE_WITH_CATEGORY_SUM_DATE:
            case EXPENSE_WITH_CATEGORY_SUM_DATE_RANGE:
                throw new UnsupportedOperationException("Modifying joined results is forbidden.");
            default:
                throw new IllegalArgumentException("Unknown Uri provided.");
        }

        mDatabase = mDbHelper.getWritableDatabase();

        return mDatabase.delete(
                table,
                selection,
                selectionArgs
        );
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String table;
        switch (mUriMatcher.match(uri)) {
            // The incoming URI is for a single row from categories
            case CATEGORY_ID:
                table = CATEGORIES_TABLE_NAME;
                // Defines selection criteria for the row to delete
                selection = Categories._ID + " = ?";
                selectionArgs = new String[]{ uri.getLastPathSegment() };
                break;
            // The incoming URI is for a single row from expenses
            case EXPENSE_ID:
                table = EXPENSES_TABLE_NAME;
                // Defines selection criteria for the row to delete
                selection = Expenses._ID + " = ?";
                selectionArgs = new String[]{ uri.getLastPathSegment() };
                break;
            // The incoming URI is for all of categories
            case CATEGORY:
                // The incoming URI is for all of expenses
            case EXPENSE:
                throw new UnsupportedOperationException("Updating multiple table rows is forbidden.");
            case EXPENSE_WITH_CATEGORY:
            case EXPENSE_WITH_CATEGORY_DATE:
            case EXPENSE_WITH_CATEGORY_DATE_RANGE:
            case EXPENSE_WITH_CATEGORY_SUM_DATE:
            case EXPENSE_WITH_CATEGORY_SUM_DATE_RANGE:
                throw new UnsupportedOperationException("Modifying joined results is forbidden.");
            default:
                throw new IllegalArgumentException("Unknown Uri provided.");
        }

        mDatabase = mDbHelper.getWritableDatabase();

        return mDatabase.update(
                table,
                values,
                selection,
                selectionArgs
        );
    }

    @Override
    public String getType(Uri uri) {
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case CATEGORY:
                return ExpensesContract.Categories.CONTENT_TYPE;
            case CATEGORY_ID:
                return ExpensesContract.Categories.CONTENT_ITEM_TYPE;
            case EXPENSE:
                return ExpensesContract.Expenses.CONTENT_TYPE;
            case EXPENSE_ID:
                return ExpensesContract.Expenses.CONTENT_ITEM_TYPE;
            case EXPENSE_WITH_CATEGORY:
            case EXPENSE_WITH_CATEGORY_DATE:
            case EXPENSE_WITH_CATEGORY_DATE_RANGE:
            case EXPENSE_WITH_CATEGORY_SUM_DATE:
            case EXPENSE_WITH_CATEGORY_SUM_DATE_RANGE:
                return ExpensesContract.ExpensesWithCategories.CONTENT_TYPE;
            default:
                return null;
        }
    }
}
