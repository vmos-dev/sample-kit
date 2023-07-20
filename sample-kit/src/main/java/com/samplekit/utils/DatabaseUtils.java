package com.samplekit.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Set;

public class DatabaseUtils {

    public static JSONObject toJSONObject(Cursor cursor) {
        JSONObject respItem = new JSONObject();
        // 赋值每列
        final String[] columnNames = cursor.getColumnNames();
        for (String columnName : columnNames) {
            final int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex != -1) {
                final int type = cursor.getType(columnIndex);
                try {
                    if (Cursor.FIELD_TYPE_STRING == type) {
                        respItem.putOpt(columnName, cursor.getString(columnIndex));
                    } else if (Cursor.FIELD_TYPE_INTEGER == type) {
                        respItem.putOpt(columnName, cursor.getLong(columnIndex));
                    } else if (Cursor.FIELD_TYPE_FLOAT == type) {
                        respItem.putOpt(columnName, cursor.getFloat(columnIndex));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return respItem;
    }

    public static JSONArray toJsonArray(Cursor cursor) {
        try {
            if (cursor != null) {
                JSONArray jsonArray = new JSONArray();
                while (cursor.moveToNext()) {
                    jsonArray.put(toJSONObject(cursor));
                }
                return jsonArray;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getObject(Cursor cursor, int columnIndex) {
        final int type = cursor.getType(columnIndex);
        if (Cursor.FIELD_TYPE_STRING == type) {
            return cursor.getString(columnIndex);
        } else if (Cursor.FIELD_TYPE_INTEGER == type) {
            final long value = cursor.getLong(columnIndex);
            return (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) ? (long) value : (int) value;
        } else if (Cursor.FIELD_TYPE_FLOAT == type) {
            return cursor.getFloat(columnIndex);
        } else if (Cursor.FIELD_TYPE_BLOB == type) {
            return cursor.getBlob(columnIndex);
        } else if (Cursor.FIELD_TYPE_NULL == type) {
            return null;
        }
        return null;
    }

    /**
     * 实现可能存在问题 未验证
     * @param cursor
     * @return
     */
    @Deprecated
    public static Cursor clone(Cursor cursor) {
        if (cursor != null) {
            try {
                final MatrixCursor newCursor = new MatrixCursor(cursor.getColumnNames(), cursor.getCount());
                while (cursor.moveToNext()) {
                    final Object[] columnValues = new Object[cursor.getColumnCount()];
                    for (int i = 0; i < columnValues.length; i++) {
                        columnValues[i] = getObject(cursor, i);
                    }
                    newCursor.addRow(columnValues);
                }
                return newCursor;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //
                cursor.requery();
            }
        }
        return null;
    }

    public static ContentValues clone(ContentValues values) {
        if (values != null) {
            final ContentValues newValues = new ContentValues();
            newValues.putAll(values);
            return newValues;
        }
        return null;
    }

    public static String toPrintString(Cursor cursor) {
        return toPrintString("", cursor);
    }

    public static String toPrintString(String uri, Cursor cursor) {
        final StringBuilder sb = new StringBuilder(uri);
        if (cursor == null) {
            sb.append(" null");
        } else {
            while (cursor.moveToNext()) {
                final int columnCount = cursor.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    sb.append("\n").append(cursor.getColumnName(i))
                            .append(" = ")
                            .append(cursor.getString(i));
                }
            }
        }
        return sb.toString();
    }

    public static String toPrintString(ContentValues values) {
        return toPrintString("", values);
    }

    public static String toPrintString(String uri, ContentValues values) {
        final StringBuilder sb = new StringBuilder(uri);
        final Set<String> keySet = values.keySet();
        for (String key : keySet) {
            sb.append("\n").append(key)
                    .append(" = ")
                    .append(values.get(key));
        }
        return sb.toString();
    }
}
